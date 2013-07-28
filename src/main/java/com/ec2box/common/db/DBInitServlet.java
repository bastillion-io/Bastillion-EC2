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
package com.ec2box.common.db;

import com.ec2box.manage.util.DBUtils;
import com.ec2box.manage.util.EncryptionUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initial startup task.  Creates an SQLite DB and generates
 * the system public/private key pair if none exists
 */
@WebServlet(name = "DBInitServlet",
        urlPatterns = {"/config"},
        loadOnStartup = 1)
public class DBInitServlet extends javax.servlet.http.HttpServlet {

    /**
     * task init method that created DB and generated public/private keys
     *
     * @param config task config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);


        try {
            Connection connection = DBUtils.getConn();
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from information_schema.tables where upper(table_name) = 'ADMIN' and table_schema='PUBLIC'");
            if (rs == null || !rs.next()) {
                statement.executeUpdate("create table if not exists admin (id INTEGER PRIMARY KEY AUTO_INCREMENT, username varchar unique not null, password varchar not null, auth_token varchar)");
                statement.executeUpdate("create table if not exists aws_credentials(admin_id INTEGER PRIMARY KEY, access_key varchar not null, secret_key varchar not null, foreign key (admin_id) references admin(id) on delete cascade)");
                statement.executeUpdate("create table if not exists ec2_keys(id INTEGER PRIMARY KEY AUTO_INCREMENT, admin_id INTEGER, key_nm varchar not null, ec2_region varchar not null, foreign key (admin_id) references admin(id) on delete cascade)");
                statement.executeUpdate("create table if not exists system (id INTEGER PRIMARY KEY AUTO_INCREMENT, admin_id INTEGER, display_nm varchar not null, instance_id varchar not null, user varchar not null, host varchar not null, port INTEGER not null, key_nm varchar, region varchar not null, state varchar, foreign key (admin_id) references admin(id) on delete cascade)");
                statement.executeUpdate("create table if not exists status (id INTEGER, status_cd varchar not null default 'INITIAL', foreign key (id) references system(id) on delete cascade)");
                statement.executeUpdate("create table if not exists scripts (id INTEGER PRIMARY KEY AUTO_INCREMENT, admin_id INTEGER, display_nm varchar not null, script varchar not null, foreign key (admin_id) references admin(id) on delete cascade)");

                //insert default admin user
                statement.executeUpdate("insert into admin (username, password) values('admin', '" + EncryptionUtil.hash("changeme") + "')");

            }

            DBUtils.closeRs(rs);
            DBUtils.closeStmt(statement);
            DBUtils.closeConn(connection);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
