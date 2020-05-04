package io.bastillion.common.jaas;

import java.util.Map;
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
 *
 * @author whbog
 */
public class BastillionLdapLoginModule extends LdapLoginModule {
    private static final Logger log = LoggerFactory.getLogger(BastillionLdapLoginModule.class);
    private DirContext rootContext;
    private String userBaseDn;
    private String userObjectClass = "inetOrgPerson";
    private String userIdAttribute = "cn";
    private final String userPasswordAttribute = "userPassword";

    private Map<String, ?> options;
    
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
        this.userObjectClass = getOption("userObjectClass", userObjectClass);
        this.userIdAttribute = getOption("userIdAttribute", userIdAttribute);
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

}
