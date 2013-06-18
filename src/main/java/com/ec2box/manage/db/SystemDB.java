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

import com.ec2box.manage.model.HostSystem;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO used to manage systems
 */
public class SystemDB {

    public static final String SORT_BY_NAME="display_nm";
    public static final String SORT_BY_USER="user";
    public static final String SORT_BY_HOST="host";
    public static final String SORT_BY_INSTANCE_ID="instance_id";
    public static final String SORT_BY_REGION="region";
    public static final String SORT_BY_STATE="state";


    /**
     * method to do order by based on the sorted set object for systems
     * @param sortedSet sorted set object
     * @param adminId admin id
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet, Long adminId){
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

        String orderBy="";
        if(sortedSet.getOrderByField()!=null && !sortedSet.getOrderByField().trim().equals("")){
            orderBy="order by " + sortedSet.getOrderByField()+ " " + sortedSet.getOrderByDirection();
        }
        String sql="select s.* from  system s, ec2_region r where s.region=r.region and s.admin_id= ?" +orderBy;

        Connection con=null;
        try {
            con=DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1,adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HostSystem hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setDisplayNm(rs.getString("display_nm"));
                hostSystem.setInstanceId(rs.getString("instance_id"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setKeyNm(rs.getString("key_nm"));
                hostSystem.setEc2Region(rs.getString("region"));
                hostSystem.setState(rs.getString("state"));
                hostSystem.setAdminId(rs.getLong("admin_id"));
                hostSystemList.add(hostSystem);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        sortedSet.setItemList(hostSystemList);
        return sortedSet;

    }



    /**
     * returns system by id
     * @param id system id
     * @return system
     */
    public static HostSystem getSystem(Long id) {

        HostSystem hostSystem = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            getSystem(con, id );


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystem;
    }


    /**
     * returns system by id
     * @param con DB connection
     * @param id system id
     * @return system
     */
    public static HostSystem getSystem(Connection con, Long id) {

        HostSystem hostSystem = null;


        try {

            PreparedStatement stmt = con.prepareStatement("select * from  system where id=?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setDisplayNm(rs.getString("display_nm"));
                hostSystem.setInstanceId(rs.getString("instance_id"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setKeyNm(rs.getString("key_nm"));
                hostSystem.setEc2Region(rs.getString("region"));
                hostSystem.setState(rs.getString("state"));
                hostSystem.setAdminId(rs.getLong("admin_id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hostSystem;
    }



    /**
     * returns system by system instance id
     * @param instanceId system instance id
     * @param adminId admin id
     * @return system
     */
    public static HostSystem getSystem(String instanceId, Long adminId) {

        HostSystem hostSystem = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from  system where instance_id=? and admin_id =?");
            stmt.setString(1, instanceId);
            stmt.setLong(2, adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setDisplayNm(rs.getString("display_nm"));
                hostSystem.setInstanceId(rs.getString("instance_id"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setKeyNm(rs.getString("key_nm"));
                hostSystem.setEc2Region(rs.getString("region"));
                hostSystem.setState(rs.getString("state"));
                hostSystem.setAdminId(rs.getLong("admin_id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystem;
    }


    /**
     * inserts host system into DB
     * @param con DB connection object
     * @param hostSystem host system object
     */
    public static void insertSystem(Connection con, HostSystem hostSystem) {



        try {

            PreparedStatement stmt = con.prepareStatement("insert into system (display_nm, user, host, port, instance_id, key_nm, region, state, admin_id) values (?,?,?,?,?,?,?,?,?)");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getInstanceId());
            stmt.setString(6, hostSystem.getKeyNm());
            stmt.setString(7, hostSystem.getEc2Region());
            stmt.setString(8, hostSystem.getState());
            stmt.setLong(9, hostSystem.getAdminId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * updates host system record
     * @param hostSystem host system object
     */
    public static void updateSystem(HostSystem hostSystem) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("update system set display_nm=?, user=?, host=?, port=?, instance_id=?, key_nm=?, region=?, state=?  where id=? and admin_id=?");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getInstanceId());
            stmt.setString(6, hostSystem.getKeyNm());
            stmt.setString(7, hostSystem.getEc2Region());
            stmt.setString(8, hostSystem.getState());
            stmt.setLong(9, hostSystem.getId());
            stmt.setLong(10, hostSystem.getAdminId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * deletes host system
     * @param hostSystemId host system id
     * @param adminId admin id
     */
    public static void deleteSystem(Long hostSystemId, Long adminId) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("delete from system where id=? and admin_id=?");
            stmt.setLong(1, hostSystemId);
            stmt.setLong(2, adminId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * set host systems for region based on list
     * @param hostSystemList list of host system object
     * @param adminId admin id
     * @param ec2Region ec2 region
     */
    public static void setSystems(List<HostSystem> hostSystemList, String ec2Region, Long adminId){
        Connection con = null;
        try {
            con = DBUtils.getConn();

            //delete all systems for region
            PreparedStatement stmt = con.prepareStatement("delete from system where region like ? and admin_id=?");
            stmt.setString(1, ec2Region);
            stmt.setLong(2, adminId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

            //insert new host systems
            for(HostSystem hostSystem:hostSystemList){
                insertSystem(con, hostSystem);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


}
