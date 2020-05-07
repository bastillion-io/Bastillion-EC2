package io.bastillion.common.jaas;

import java.security.Principal;

/**
 * The principal used for Bastillion authentication.
 * @author <a href="mailto:wilson@engeweb.com.br">Wilson Horstmeyer Bogado</a>
 */
public class BastillionPrincipal implements Principal {
    private final String name;
    
    public BastillionPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }    
}
