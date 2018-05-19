/**
 *    Copyright (C) 2013 Loophole, LLC
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
    public static final String SORT_BY_ACCESS_KEY = ACCESS_KEY;

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
            log.error(e.toString(), e);
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
            log.error(e.toString(), e);
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
            log.error(e.toString(), e);
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
