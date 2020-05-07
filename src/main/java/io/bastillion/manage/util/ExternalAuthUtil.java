/**
 *    Copyright (C) 2017 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
 */
package io.bastillion.manage.util;

import io.bastillion.common.jaas.BastillionCallbackHandler;
import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;
import io.bastillion.common.jaas.BastillionLdapPrincipal;
import io.bastillion.common.jaas.BastillionPrincipal;
import io.bastillion.manage.db.UserProfileDB;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * External authentication utility for JAAS
 */
public class ExternalAuthUtil {

    private static final Logger log = LoggerFactory.getLogger(ExternalAuthUtil.class);

    public static final boolean externalAuthEnabled = StringUtils.isNotEmpty(AppConfig.getProperty("jaasModule"));
    private static final String JAAS_CONF = "jaas.conf";
    private static final String JAAS_MODULE = AppConfig.getProperty("jaasModule");


    static {
        if (externalAuthEnabled) {
            System.setProperty("java.security.auth.login.config", AppConfig.CONFIG_DIR + "/" + JAAS_CONF);
        }
    }

    private ExternalAuthUtil() {

    }
    
    private static BastillionPrincipal getPrincipal(Subject subject) throws LoginException {
        BastillionPrincipal p = null;
        for (BastillionPrincipal principal: subject.getPrincipals(BastillionPrincipal.class)) {
            if (p == null && principal.getClass() == BastillionPrincipal.class) {
                p = principal;
            } else if (p != null && principal.getClass() == BastillionPrincipal.class) {
                throw new LoginException("More than 1 principal found");
            }
        }
        return p;
    }
    
    private static User createUser(Subject subject) throws LoginException, NamingException {
        User user = new User();
        user.setUserType(User.ADMINISTRATOR);
        user.setUsername(getPrincipal(subject).getName());

        String givenName = null;
        String sn = null;
        String displayName = null;
        String cn = null;
        String email = null;
        for (BastillionLdapPrincipal principal: subject.getPrincipals(BastillionLdapPrincipal.class)) {
            Attributes attrs = principal.getAttributes();
            if (givenName == null) givenName = (String) attrs.get("givenName").get();
            if (sn == null) sn = (String) attrs.get("sn").get();
            if (displayName == null) displayName = (String) attrs.get("displayName").get();
            if (cn == null) cn = (String) attrs.get("cn").get();
            if (email == null) email = (String) attrs.get("mail").get();
        }
        // set attributes from ldap
        if (StringUtils.isNotEmpty(givenName) && StringUtils.isNotEmpty(sn)) {
            user.setFirstNm(givenName);
            user.setLastNm(sn);
        } else if (StringUtils.isNotEmpty(displayName) && displayName.contains(" ")) {
            String[] name = displayName.split(" ");
            if (name.length > 1) {
                user.setFirstNm(name[0]);
                user.setLastNm(name[name.length - 1]);
            }
        } else if (StringUtils.isNotEmpty(cn) && cn.contains(" ")) {
            String[] name = cn.split(" ");
            if (name.length > 1) {
                user.setFirstNm(name[0]);
                user.setLastNm(name[name.length - 1]);
            }
        }

        //set email
        if (StringUtils.isNotEmpty(email)) {
            user.setEmail(email);
        } else if (user.getUsername().contains("@")) {
            user.setEmail(user.getUsername());
        }
        return user;
    }

    private static List<String> getUserRoles(Subject subject) {
        List<String> roles = new ArrayList<>();
        for (BastillionLdapPrincipal principal: subject.getPrincipals(BastillionLdapPrincipal.class)) {
            roles.addAll(principal.getRoles());
        }
        return roles;
    }
    
    /**
     * external auth login method
     *
     * @param auth authentication credentials
     * @return auth token if success
     */
    public static String login(final Auth auth) {
        Connection con = null;
        String authToken = null;

        if (externalAuthEnabled && auth != null && StringUtils.isNotEmpty(auth.getUsername()) && StringUtils.isNotEmpty(auth.getPassword())) {
            try {
                //create login context
                LoginContext loginContext = new LoginContext(JAAS_MODULE, new BastillionCallbackHandler(auth));

                //will throw exception if login fail
                loginContext.login();

                Subject subject = loginContext.getSubject();
                con = DBUtils.getConn();
                User user = AuthDB.getUserByUID(con, auth.getUsername());
                if (user == null) {
                    user = createUser(subject);
                    user.setId(UserDB.insertUser(con, user));
                }
                authToken = UUID.randomUUID().toString();
                user.setAuthToken(authToken);
                user.setAuthType(Auth.AUTH_EXTERNAL);
                //set auth token
                AuthDB.updateLogin(con, user);
                List<String> userRoles = getUserRoles(subject);
                UserProfileDB.assignProfilesToUser(con, user.getId(), userRoles, userRoles);

            } catch (LoginException le) {
                log.debug(le.toString(), le);
            } catch (Exception e) {
                log.error(e.toString(), e);
            } finally {
                DBUtils.closeConn(con);
            }
        }
        return authToken;
    }

    /**
     * returns all possible roles for a user
     *
     * @param dirContext        ldap directory context
     * @param roleBaseDn        base dn for roles
     * @param roleNameAttribute role name
     * @param roleObjectClass   role object class
     * @return all roles under base dn
     */
    private static List<String> getAllRoles(DirContext dirContext, String roleBaseDn, String roleNameAttribute, String roleObjectClass) throws NamingException {
        List<String> allRoles = new ArrayList<>();
        SearchControls ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setReturningAttributes(new String[]{roleNameAttribute});

        String filter = "(objectClass={0})";
        Object[] filterArguments = {roleObjectClass};
        NamingEnumeration<SearchResult> results = dirContext.search(roleBaseDn, filter, filterArguments, ctls);

        while (results.hasMoreElements()) {
            SearchResult result = results.nextElement();

            Attributes attributes = result.getAttributes();

            if (attributes != null) {
                Attribute roleAttribute = attributes.get(roleNameAttribute);

                if (roleAttribute != null) {

                    NamingEnumeration<?> roles = roleAttribute.getAll();
                    while (roles.hasMore()) {
                        allRoles.add(roles.next().toString());
                    }
                }
            }
        }
        return allRoles;
    }
}
