package io.bastillion.common.jaas;

import java.util.List;
import javax.naming.directory.Attributes;

/**
 * The principal found during LDAP authentication.
 * @author <a href="mailto:wilson@engeweb.com.br">Wilson Horstmeyer Bogado</a>
 */
public class BastillionLdapPrincipal extends BastillionPrincipal {
    private final Attributes attributes;
    private final List<String> roles;

    /**
     * Constructs a principal with the given DN and attributes.
     * @param name The principal's DN.
     * @param roles The principal's roles.
     * @param attributes The principal's LDAP attributes.
     */
    public BastillionLdapPrincipal(String name, Attributes attributes, List<String> roles) {
        super(name);
        this.attributes = attributes;
        this.roles = roles;
    }

    public Attributes getAttributes() {
        return attributes;
    }
    
    public List<String> getRoles() {
        return roles;
    }
            
}
