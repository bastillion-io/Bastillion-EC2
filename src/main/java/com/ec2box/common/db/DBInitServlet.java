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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.ec2box.manage.model.Auth;
import com.ec2box.manage.util.DBUtils;
import com.ec2box.manage.util.EncryptionUtil;

/**
 * Initial startup task.  Creates an H2 DB.
 */
@WebServlet(name = "DBInitServlet",
        urlPatterns = {"/config"},
        loadOnStartup = 1)
public class DBInitServlet extends javax.servlet.http.HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 123456457L;

	/**
     * task init method that created DB
     *
     * @param config task config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);


        Connection connection = null;
        Statement statement = null;
        try {
            connection = DBUtils.getConn();
            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from information_schema.tables where upper(table_name) = 'USERS' and table_schema='PUBLIC'");
            if (rs == null || !rs.next()) {
                statement.executeUpdate("create table if not exists users (id INTEGER PRIMARY KEY AUTO_INCREMENT, first_nm varchar, last_nm varchar, email varchar, username varchar not null, password varchar, timeToExpiry INTEGER, expiryTime TIMESTAMP, auth_token varchar, enabled boolean not null default true, user_type varchar not null default '" + Auth.ADMINISTRATOR + "')");
                statement.executeUpdate("create table if not exists aws_credentials (id INTEGER PRIMARY KEY AUTO_INCREMENT, access_key varchar not null, secret_key varchar not null)");
                statement.executeUpdate("create table if not exists ec2_keys (id INTEGER PRIMARY KEY AUTO_INCREMENT, key_nm varchar not null, ec2_region varchar not null, private_key varchar not null, aws_cred_id INTEGER, foreign key (aws_cred_id) references aws_credentials(id) on delete cascade)");
                statement.executeUpdate("create table if not exists system (id INTEGER PRIMARY KEY AUTO_INCREMENT, display_nm varchar, instance_id varchar not null, user varchar not null, host varchar, port INTEGER not null, key_id INTEGER, region varchar not null, state varchar, instance_status varchar, system_status varchar, m_alarm INTEGER default 0, m_insufficient_data INTEGER default 0, m_ok INTEGER default 0, foreign key (key_id) references ec2_keys(id) on delete cascade)");
                statement.executeUpdate("create table if not exists profiles (id INTEGER PRIMARY KEY AUTO_INCREMENT, nm varchar not null, tag varchar not null)");
                statement.executeUpdate("create table if not exists user_map (user_id INTEGER, profile_id INTEGER, foreign key (user_id) references users(id) on delete cascade, foreign key (profile_id) references profiles(id) on delete cascade, primary key (user_id, profile_id))");

                statement.executeUpdate("create table if not exists status (id INTEGER, user_id INTEGER, status_cd varchar not null default 'INITIAL', foreign key (id) references system(id) on delete cascade, foreign key (user_id) references users(id) on delete cascade)");
                statement.executeUpdate("create table if not exists scripts (id INTEGER PRIMARY KEY AUTO_INCREMENT, user_id INTEGER, display_nm varchar not null, script varchar not null, foreign key (user_id) references users(id) on delete cascade)");

                statement.executeUpdate("create table if not exists session_log (id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id INTEGER, session_tm timestamp default CURRENT_TIMESTAMP, foreign key (user_id) references users(id) on delete cascade )");
                statement.executeUpdate("create table if not exists terminal_log (session_id BIGINT, system_id INTEGER, output varchar not null, log_tm timestamp default CURRENT_TIMESTAMP, foreign key (session_id) references session_log(id) on delete cascade, foreign key (system_id) references system(id) on delete cascade)");

                //insert default admin user
                statement.executeUpdate("insert into users (username, password, user_type, expiryTime) values('admin', '" + EncryptionUtil.hash("changeme") + "','"+ Auth.MANAGER+"',DATEADD('YEAR', 100, CURRENT_TIMESTAMP()))");

            }

            DBUtils.closeRs(rs);
            

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        DBUtils.closeStmt(statement);
        DBUtils.closeConn(connection);

    }

}
