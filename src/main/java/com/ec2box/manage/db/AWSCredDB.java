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
package com.ec2box.manage.db;

import com.ec2box.manage.model.AWSCred;
import com.ec2box.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO to manage amazon credentials (access and secret key)
 */
public class AWSCredDB {
    /**
     * check auth token and set amazon credentials
     *
     * @param adminId admin id
     */
    public static AWSCred getAWSCred(Long adminId) {

        AWSCred awsCred = null;

        Connection con =null;
        try {
            con = DBUtils.getConn();


                PreparedStatement stmt = con.prepareStatement("select * from aws_credentials where admin_id=?");
                stmt.setLong(1, adminId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    awsCred = new AWSCred();
                    awsCred.setAccessKey(rs.getString("access_key"));
                    awsCred.setSecretKey(rs.getString("secret_key"));

                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //close db connection
        DBUtils.closeConn(con);

        return awsCred;


    }

    /**
     * check auth token and set amazon credentials
     *
     * @param adminId admin id
     * @param awsCred AWS access and secret key
     */
    public static void setAWSCred(Long adminId, AWSCred awsCred) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //delete
            PreparedStatement stmt = con.prepareStatement("delete from aws_credentials where admin_id=?");
            stmt.setLong(1, adminId);
            stmt.execute();

            //insert
            stmt = con.prepareStatement("insert into aws_credentials (admin_id, access_key, secret_key) values(?,?,?)");
            stmt.setLong(1, adminId);
            stmt.setString(2, awsCred.getAccessKey());
            stmt.setString(3, awsCred.getSecretKey());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //close db connection
        DBUtils.closeConn(con);


    }




}
