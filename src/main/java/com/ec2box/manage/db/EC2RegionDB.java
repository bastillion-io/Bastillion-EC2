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

import com.ec2box.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class to manage EC2 Regions for admin
 */
public class EC2RegionDB {


    /**
     * returns the EC2 region set for the administrator
     *
     * @param adminId id of the admin user
     * @return region set for administrator
     */
    public static List<String> getEC2Regions(Long adminId) {

        List<String> ec2RegionList = new ArrayList<String>();

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_region where admin_id=?");
            stmt.setLong(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ec2RegionList.add(rs.getString("region"));

            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return ec2RegionList;

    }

    /**
     * sets the EC2 region for admin user
     *
     * @param adminId       id of the admin user
     * @param ec2RegionList region to set for admin user
     */
    public static void setRegion(Long adminId, List<String> ec2RegionList) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            //delete region
            PreparedStatement stmt = con.prepareStatement("delete from ec2_region where admin_id=?");
            stmt.setLong(1, adminId);
            stmt.execute();

            //insert new region
            for (String ec2Region : ec2RegionList) {
                stmt = con.prepareStatement("insert into ec2_region (admin_id, region) values (?,?)");
                stmt.setLong(1, adminId);
                stmt.setString(2, ec2Region);
                stmt.execute();
                DBUtils.closeStmt(stmt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }


}
