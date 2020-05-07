/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bastillion.common.jaas;

import java.util.List;
import org.eclipse.jetty.jaas.spi.UserInfo;
import org.eclipse.jetty.util.security.Credential;

/**
 *
 * @author whbog
 */
public class BastillionUserInfo extends UserInfo {

    public BastillionUserInfo(String userName, Credential credential, List<String> roleNames) {
        super(userName, credential, roleNames);
    }

    public BastillionUserInfo(String userName, Credential credential) {
        super(userName, credential);
    }
    
    
}
