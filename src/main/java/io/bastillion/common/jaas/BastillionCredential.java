package io.bastillion.common.jaas;

/**
 * Credentials used for Bastillion authentication.
 * @author <a href="mailto:wilson@engeweb.com.br">Wilson Horstmeyer Bogado</a>
 */
public class BastillionCredential {
    private final String username;
    private final String password;

    public BastillionCredential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
