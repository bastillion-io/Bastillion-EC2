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

import com.ec2box.manage.model.EC2Key;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.DBUtils;
import com.ec2box.manage.util.EncryptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class to manage private keys for AWS servers
 */
public class EC2KeyDB {


    public static final String KEY_NM = "key_nm";
    public static final String EC2_REGION = "ec2_region";
    public static final String ACCESS_KEY = "access_key";
    public static final String AWS_CRED_ID = "aws_cred_id";

    private EC2KeyDB() {
    }

    /**
     * returns private key information for user
     *
     * @param sortedSet object that defines sort order
     * @return sorted identity list
     */
    public static SortedSet getEC2KeySet( SortedSet sortedSet) {

        List<EC2Key> ec2KeyList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_keys, aws_credentials where ec2_keys.aws_cred_id=aws_credentials.id "+ orderBy);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EC2Key ec2Key = new EC2Key();
                ec2Key.setId(rs.getLong("id"));
                ec2Key.setKeyNm(rs.getString(KEY_NM));
                ec2Key.setEc2Region(rs.getString(EC2_REGION));
                ec2Key.setAwsCredId(rs.getLong(AWS_CRED_ID));
                ec2Key.setAccessKey(rs.getString(ACCESS_KEY));
                ec2KeyList.add(ec2Key);
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        sortedSet.setItemList(ec2KeyList);


        return sortedSet;

    }






    /**
     * returns private keys information
     *
     * @param ec2KeyId ec2 key id
     * @return key information
     */
    public static EC2Key getEC2Key(Long ec2KeyId) {
        EC2Key ec2Key=null;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_keys where id = ?");
            stmt.setLong(1, ec2KeyId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                ec2Key = new EC2Key();
                ec2Key.setId(rs.getLong("id"));
                ec2Key.setKeyNm(rs.getString(KEY_NM));
                ec2Key.setEc2Region(rs.getString(EC2_REGION));
                ec2Key.setAwsCredId(rs.getLong(AWS_CRED_ID));
                ec2Key.setPrivateKey(EncryptionUtil.decrypt(rs.getString("private_key")));


            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return ec2Key;

    }


    /**
     * returns private keys information for region and user
     *
     * @param keyNm
     * @param ec2Region ec2 region
     * @param awsCredId aws cred id
     * @return key information
     */
    public static EC2Key getEC2KeyByNmRegion(String keyNm, String ec2Region, Long awsCredId) {
        EC2Key ec2Key=null;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_keys where key_nm like ? and ec2_region like ? and aws_cred_id=?");
            stmt.setString(1, keyNm);
            stmt.setString(2, ec2Region);
            stmt.setLong(3, awsCredId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ec2Key = new EC2Key();
                ec2Key.setId(rs.getLong("id"));
                ec2Key.setKeyNm(rs.getString(KEY_NM));
                ec2Key.setEc2Region(rs.getString(EC2_REGION));
                ec2Key.setAwsCredId(rs.getLong(AWS_CRED_ID));
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return ec2Key;

    }

    /**
     * returns private keys information for region and user
     *
     * @param ec2Region ec2 region
     * @param awsCredId aws cred id
     * @return key information
     */
    public static List<EC2Key> getEC2KeyByRegion(String ec2Region, Long awsCredId) {
        List<EC2Key> ec2KeyList = new ArrayList<>();

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_keys where ec2_region like ? and aws_cred_id=?");
            stmt.setString(1, ec2Region);
            stmt.setLong(2, awsCredId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EC2Key ec2Key = new EC2Key();
                ec2Key.setId(rs.getLong("id"));
                ec2Key.setKeyNm(rs.getString(KEY_NM));
                ec2Key.setEc2Region(rs.getString(EC2_REGION));
                ec2Key.setAwsCredId(rs.getLong(AWS_CRED_ID));
                ec2KeyList.add(ec2Key);
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return ec2KeyList;

    }

    /**
     * inserts private key information for user
     *
     * @param ec2Key private key information
     */
    public static Long insertEC2Key(EC2Key ec2Key) {

        Connection con = null;
        Long ec2KeyId=null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("insert into ec2_keys (key_nm, ec2_region, private_key, aws_cred_id) values (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, ec2Key.getKeyNm());
            stmt.setString(2, ec2Key.getEc2Region());
            stmt.setString(3, EncryptionUtil.encrypt(ec2Key.getPrivateKey().trim()));
            stmt.setLong(4, ec2Key.getAwsCredId());
            stmt.execute();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs != null && rs.next()) {
                ec2KeyId = rs.getLong(1);
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return ec2KeyId;


    }

    /**
     * updates private key information for user
     *
     * @param ec2Key private key information
     */
    public static void updateEC2Key(EC2Key ec2Key) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("update ec2_keys set key_nm=?, ec2_region=?, private_key=?, aws_cred_id=? where id=?");
            stmt.setString(1, ec2Key.getKeyNm());
            stmt.setString(2, ec2Key.getEc2Region());
            stmt.setString(3, EncryptionUtil.encrypt(ec2Key.getPrivateKey().trim()));
            stmt.setLong(4, ec2Key.getAwsCredId());
            stmt.setLong(5, ec2Key.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }





    /**
     * saves private key information for user
     *
     * @param ec2Key private key information
     */
    public static void saveEC2Key(EC2Key ec2Key) {


        //get id for key if exists
        EC2Key ec2KeyTmp = getEC2KeyByNmRegion(ec2Key.getKeyNm(), ec2Key.getEc2Region(), ec2Key.getAwsCredId());
        if(ec2KeyTmp!=null){
            ec2Key.setId(ec2KeyTmp.getId());
            updateEC2Key(ec2Key);
        //else insert if it doesn't exist
        }else{
            insertEC2Key(ec2Key);
        }




    }




    /**
     * deletes private key information for user
     *
     * @param identityId db generated id for private key
     */
    public static void deleteEC2Key(Long identityId) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from ec2_keys where id=?");
            stmt.setLong(1, identityId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }
    /**
     * returns the EC2 region
     *
     * @return region set
     */
    public static List<String> getEC2Regions() {

        List<String> ec2RegionList = new ArrayList<>();

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select distinct ec2_region from ec2_keys");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ec2RegionList.add(rs.getString(EC2_REGION));

            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return ec2RegionList;

    }
}
