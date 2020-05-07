package io.bastillion.common.jaas;

import io.bastillion.manage.model.Auth;
import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.eclipse.jetty.jaas.callback.ObjectCallback;

/**
 * The callback handler.
 * @author <a href="mailto:wilson@engeweb.com.br">Wilson Horstmeyer Bogado</a>
 */
public class BastillionCallbackHandler implements CallbackHandler {
    
    private final Auth auth;

    public BastillionCallbackHandler(Auth auth) {
        this.auth = auth;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback: callbacks) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(auth.getUsername());
            } else if (callback instanceof ObjectCallback) {
                ((ObjectCallback) callback).setObject(auth.getPassword().toCharArray());
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(auth.getPassword().toCharArray());
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
}
