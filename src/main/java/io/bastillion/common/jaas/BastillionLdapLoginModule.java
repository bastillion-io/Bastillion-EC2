package io.bastillion.common.jaas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.eclipse.jetty.jaas.spi.LdapLoginModule;
import static org.eclipse.jetty.jaas.spi.LdapLoginModule.convertCredentialLdapToJetty;
import org.eclipse.jetty.jaas.spi.UserInfo;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bastillion login module.
 * @author <a href="mailto:wilson@engeweb.com.br">Wilson Horstmeyer Bogado</a>
 */
public class BastillionLdapLoginModule extends LdapLoginModule {
    private static final Logger log = LoggerFactory.getLogger(BastillionLdapLoginModule.class);
    private DirContext rootContext;
    private String userBaseDn;
    private String userObjectClass = "inetOrgPerson";
    private String userIdAttribute = "cn";
    private final String userPasswordAttribute = "userPassword";
    private String roleBaseDn;
    private String roleObjectClass = "groupOfUniqueNames";
    private String roleMemberAttribute = "uniqueMember";
    private String roleNameAttribute = "roleName";
    private boolean forceBindingLogin = false;

    private Map<String, ?> options;
    private BastillionPrincipal principal;
    private BastillionLdapPrincipal ldapPrincipal;
    
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        try {
            rootContext = new InitialDirContext(getEnvironment());
        } catch (NamingException ex) {
            throw new IllegalStateException("Unable to establish root context", ex);
        }
        this.options = options;
        this.userBaseDn = getOption("userBaseDn", null);
        this.roleBaseDn = getOption("roleBaseDn", this.roleBaseDn);
        this.roleObjectClass = getOption("roleObjectClass", this.roleObjectClass);
        this.roleMemberAttribute = getOption("roleMemberAttribute", this.roleMemberAttribute);
        this.roleNameAttribute = getOption("roleNameAttribute", this.roleNameAttribute);
        this.userObjectClass = getOption("userObjectClass", this.userObjectClass);
        this.userIdAttribute = getOption("userIdAttribute", this.userIdAttribute);
        this.forceBindingLogin = Boolean.valueOf(getOption("forceBindingLogin", "false"));
    }
    
    public String getOption(String key, String defaultValue) {
        Object value = options.get(key);
        return value == null ? defaultValue : value.toString();
    }

    public String getOption(String key) {
        return getOption(key, null);
    }
    
    public DirContext getRootContext() {
        return rootContext;
    }

    private String getUserCredentials(Attributes attributes) throws LoginException {
        String ldapCredential = null;

        Attribute attribute = attributes.get(userPasswordAttribute);
        if (attribute != null) {
            try {
                byte[] value = (byte[]) attribute.get();

                ldapCredential = new String(value);
            } catch (NamingException e) {
                log.debug("no password available under attribute: " + userPasswordAttribute);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("user cred is: " + ldapCredential);
        }
        return ldapCredential;
    }
    
    @Override
    public UserInfo getUserInfo(String username) throws Exception {
        Attributes attributes = getUserAttributes(username);
        String pwdCredential = getUserCredentials(attributes);
        Credential credential = null;
        if (pwdCredential != null) {
            pwdCredential = convertCredentialLdapToJetty(pwdCredential);
            credential = Credential.getCredential(pwdCredential);
        }
        return new LDAPUserInfo(username, credential, attributes);
    }

    public Attributes getUserAttributes(String username) throws LoginException {
        SearchResult result = findUser(username);
        Attributes attributes = result.getAttributes();
        return attributes;
    }
    
    private SearchResult findUser(String username) throws LoginException {
        String filter = "(&(objectClass={0})({1}={2}))";

        if (log.isDebugEnabled()) {
            log.debug("Searching for user " + username + " with filter: \'" + filter + "\'" + " from base dn: " + userBaseDn);
        }

        Object[] filterArguments = new Object[]{
            userObjectClass,
            userIdAttribute,
            username
        };

        return findUser(rootContext, filter, filterArguments);
    }

    private SearchResult findUser(DirContext dirContext, String filter, Object[] filterArguments) throws LoginException {
        SearchControls ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results;
        try {
            results = dirContext.search(userBaseDn, filter, filterArguments, ctls);
        } catch (NamingException ex) {
            throw new FailedLoginException(ex.getMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("Found user?: " + results.hasMoreElements());
        }

        if (!results.hasMoreElements()) {
            throw new FailedLoginException("User not found.");
        }

        SearchResult searchResult = results.nextElement();
        if (results.hasMoreElements()) {
            throw new FailedLoginException("Search result contains ambiguous entries");
        }

        return searchResult;
    }

    private List<String> getUserRoles(DirContext dirContext, String roleMemberValue) throws NamingException {
        List<String> roleList = new ArrayList<>();

        if (dirContext == null || roleBaseDn == null || roleMemberAttribute == null || roleObjectClass == null) {
            return roleList;
        }

        SearchControls ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setReturningAttributes(new String[]{roleNameAttribute});

        String filter = "(&(objectClass={0})({1}={2}))";
        Object[] filterArguments = {roleObjectClass, roleMemberAttribute, roleMemberValue};
        NamingEnumeration<SearchResult> results = dirContext.search(roleBaseDn, filter, filterArguments, ctls);

        if (log.isDebugEnabled()) {
            log.debug("Found user roles?: " + results.hasMoreElements());
        }

        while (results.hasMoreElements()) {
            SearchResult result = results.nextElement();

            Attributes attributes = result.getAttributes();

            if (attributes == null) {
                continue;
            }

            Attribute roleAttribute = attributes.get(roleNameAttribute);

            if (roleAttribute == null) {
                continue;
            }

            NamingEnumeration<?> roles = roleAttribute.getAll();
            while (roles.hasMore()) {
                roleList.add(roles.next().toString());
            }
        }

        return roleList;
    }
    
    @Override
    public boolean login() throws LoginException {
        boolean authd = super.login();
        if (authd) {
            String username = getCurrentUser().getUserName();
            SearchResult sr = findUser(username);
            String dn = sr.getNameInNamespace();
            Attributes attrs = sr.getAttributes();
            principal = new BastillionPrincipal(username);
            List<String> roles;
            try {
                String roleAttrValue = "memberUid".equals(roleMemberAttribute) ? username : dn;
                roles = getUserRoles(rootContext, roleAttrValue);
            } catch (NamingException ex) {
                LoginException le = new LoginException("Could not get principal's roles");
                le.initCause(ex);
                throw le;
            }
            ldapPrincipal = new BastillionLdapPrincipal(dn, attrs, roles);
        }
        return authd;
    }

    @Override
    public boolean commit() throws LoginException {
        boolean commited = super.commit();
        if (commited) {
            getSubject().getPrincipals().add(principal);
            getSubject().getPrincipals().add(ldapPrincipal);
        }
        try {
            rootContext.close();
        } catch (NamingException ex) {
            log.warn("Could not close root context", ex);
        }
        return commited;
    }

}
