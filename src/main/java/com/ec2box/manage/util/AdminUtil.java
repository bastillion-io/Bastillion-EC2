/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ec2box.manage.util;

import com.ec2box.manage.db.AdminDB;
import com.ec2box.manage.model.Login;

import javax.servlet.http.HttpServletRequest;


public class AdminUtil {
    public static Long getAdminId(HttpServletRequest servletRequest) {

        Long adminId = null;
        String authToken = EncryptionUtil.decrypt(CookieUtil.get(servletRequest, "authToken"));
        Login login = AdminDB.getAdminLogin(authToken);
        if (login != null) {
            adminId = login.getId();
        }


        return adminId;
    }
}
