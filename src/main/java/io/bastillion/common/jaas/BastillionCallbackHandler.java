/**
 *    Copyright (C) 2020 Loophole, LLC
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
