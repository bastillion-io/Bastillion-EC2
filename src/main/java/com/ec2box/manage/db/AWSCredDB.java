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
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.DBUtils;
import com.ec2box.manage.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO to manage amazon credentials (access and secret key)
 */
public class AWSCredDB {

    private static Logger log = LoggerFactory.getLogger(AWSCredDB.class);

    public static final String ACCESS_KEY = "access_key";
    public static final String SECRET_KEY = "secret_key";

    private AWSCredDB() {
    }


    /**
     * returns list of all amazon credentials
     *
     * @param sortedSet object that defines sort order
     * @return sorted aws credential list
     */
    public static SortedSet getAWSCredSet(SortedSet sortedSet) {


        List<AWSCred> awsCredList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from aws_credentials " + orderBy;


        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                AWSCred awsCred = new AWSCred();
                awsCred.setId(rs.getLong("id"));
                awsCred.setAccessKey(rs.getString(ACCESS_KEY));
                //awsCred.setSecretKey(EncryptionUtil.decrypt(rs.getString("secret_key")));
                awsCredList.add(awsCred);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);


        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        finally {
            DBUtils.closeConn(con);
        }

        sortedSet.setItemList(awsCredList);

        return sortedSet;

    }


    /**
     * returns list of all amazon credentials
     *
     * @return  aws credential list
     */
    public static List<AWSCred> getAWSCredList() {


        List<AWSCred> awsCredList = new ArrayList<>();


        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from aws_credentials");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                AWSCred awsCred = new AWSCred();
                awsCred.setId(rs.getLong("id"));
                awsCred.setAccessKey(rs.getString(ACCESS_KEY));
                awsCred.setSecretKey(EncryptionUtil.decrypt(rs.getString(SECRET_KEY)));
                awsCredList.add(awsCred);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);


        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return awsCredList;

    }
    /**
     * returns amazon credentials
     *
     * @param accessKey aws cred access key
     * @return aws credential
     */
    public static AWSCred getAWSCred(String accessKey) {


        AWSCred awsCred = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from aws_credentials where access_key like ?");
            stmt.setString(1, accessKey);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                awsCred = new AWSCred();
                awsCred.setId(rs.getLong("id"));
                awsCred.setAccessKey(rs.getString(ACCESS_KEY));
                awsCred.setSecretKey(EncryptionUtil.decrypt(rs.getString(SECRET_KEY)));

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);


        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return awsCred;
    }

    /**
     * returns amazon credentials
     *
     * @param id aws cred id
     * @return aws credential
     */
    public static AWSCred getAWSCred(Long id) {


        AWSCred awsCred = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from aws_credentials where id=?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                awsCred = new AWSCred();
                awsCred.setId(rs.getLong("id"));
                awsCred.setAccessKey(rs.getString("access_key"));
                awsCred.setSecretKey(EncryptionUtil.decrypt(rs.getString(SECRET_KEY)));

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);


        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return awsCred;

    }

    /**
     * updates AWS credentials
     *
     * @param awsCred AWS access and secret key
     */
    public static void updateAWSCred(AWSCred awsCred) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //update
            PreparedStatement stmt = con.prepareStatement("update aws_credentials set access_key=?, secret_key=? where id=?");
            stmt.setString(1, awsCred.getAccessKey().trim());
            stmt.setString(2, EncryptionUtil.encrypt(awsCred.getSecretKey().trim()));
            stmt.setLong(3, awsCred.getId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DBUtils.closeConn(con);
        }

    }

    /**
     * delete AWS credentials
     *
     * @param id AWS id
     */
    public static void deleteAWSCred(Long id) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //delete
            PreparedStatement stmt = con.prepareStatement("delete from aws_credentials where id=?");
            stmt.setLong(1, id);
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * inserts AWS credentials
     *
     * @param awsCred AWS access and secret key
     */
    public static void insertAWSCred(AWSCred awsCred) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //insert
            PreparedStatement stmt = con.prepareStatement("insert into aws_credentials (access_key, secret_key) values(?,?)");
            stmt.setString(1, awsCred.getAccessKey().trim());
            stmt.setString(2, EncryptionUtil.encrypt(awsCred.getSecretKey().trim()));
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * insert or updated based on access key
     *
     * @param awsCred AWS access and secret key
     */
    public static void saveAWSCred(AWSCred awsCred) {

        AWSCred awsCredTmp =getAWSCred(awsCred.getAccessKey());
        if (awsCredTmp!= null) {
            awsCred.setId(awsCredTmp.getId());
            updateAWSCred(awsCred);
        } else {
            insertAWSCred(awsCred);
        }

    }


}
