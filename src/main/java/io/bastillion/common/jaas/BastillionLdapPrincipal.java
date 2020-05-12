package io.bastillion.common.jaas;

import java.security.Principal;
import java.util.List;
import javax.naming.directory.Attributes;

/**
 * The principal found during LDAP authentication.
 * @author <a href="mailto:wilson@engeweb.com.br">Wilson Horstmeyer Bogado</a>
 */
public class BastillionLdapPrincipal implements Principal {
    private final String dn;
    private final Attributes attributes;
    private final List<String> roles;

    /**
     * Constructs a principal with the given DN and attributes.
     * @param dn The principal's DN.
     * @param roles The principal's roles.
     * @param attributes The principal's LDAP attributes.
     */
    public BastillionLdapPrincipal(String dn, Attributes attributes, List<String> roles) {
        this.dn = dn;
        this.attributes = attributes;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return dn;
    }

    public String getDn() {
        return dn;
    }

    public Attributes getAttributes() {
        return attributes;
    }
    
    public List<String> getRoles() {
        return roles;
    }
            
}
