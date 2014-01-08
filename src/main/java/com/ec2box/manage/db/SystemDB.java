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

    public static final String SORT_BY_NAME = "display_nm";
    public static final String SORT_BY_USER = "user";
    public static final String SORT_BY_HOST = "host";
    public static final String SORT_BY_PORT = "port";
    public static final String SORT_BY_INSTANCE_ID = "instance_id";
    public static final String SORT_BY_REGION = "region";
    public static final String SORT_BY_STATE = "state";


    /**
     * method to do order by based on the sorted set object for systems. only selects instance IDs in provided list.
     *
     * @param sortedSet      sorted set object
     * @param instanceIdList instance ids to select
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet, List<String> instanceIdList) {
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

        if (!instanceIdList.isEmpty()) {

            String orderBy = "";
            if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
                orderBy = " order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
            }
            String sql = "select * from  system  where instance_id in ( ";
            for (int i = 0; i < instanceIdList.size(); i++) {
                if (i == instanceIdList.size() - 1) sql = sql + "'" + instanceIdList.get(i) + "') ";
                else sql = sql + "'" + instanceIdList.get(i) + "', ";
            }

            sql = sql + orderBy;

            Connection con = null;
            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement(sql);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    HostSystem hostSystem = new HostSystem();
                    hostSystem.setId(rs.getLong("id"));
                    hostSystem.setDisplayNm(rs.getString("display_nm"));
                    hostSystem.setInstanceId(rs.getString("instance_id"));
                    hostSystem.setUser(rs.getString("user"));
                    hostSystem.setHost(rs.getString("host"));
                    hostSystem.setPort(rs.getInt("port"));
                    hostSystem.setKeyId(rs.getLong("key_id"));
                    hostSystem.setEc2Region(rs.getString("region"));
                    hostSystem.setState(rs.getString("state"));
                    hostSystemList.add(hostSystem);
                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
            DBUtils.closeConn(con);


            sortedSet.setItemList(hostSystemList);
        }
        return sortedSet;

    }


    /**
     * returns system by id
     *
     * @param id system id
     * @return system
     */
    public static HostSystem getSystem(Long id) {

        HostSystem hostSystem = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            getSystem(con, id);


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystem;
    }


    /**
     * returns system by id
     *
     * @param con DB connection
     * @param id  system id
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
                hostSystem.setKeyId(rs.getLong("key_id"));
                hostSystem.setEc2Region(rs.getString("region"));
                hostSystem.setState(rs.getString("state"));
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
     *
     * @param instanceId system instance id
     * @return system
     */
    public static HostSystem getSystem(String instanceId) {

        HostSystem hostSystem = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            hostSystem = getSystem(con, instanceId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystem;
    }

    /**
     * returns system by system instance id
     *
     * @param con        DB connection
     * @param instanceId system instance id
     * @return system
     */
    public static HostSystem getSystem(Connection con, String instanceId) {

        HostSystem hostSystem = null;


        try {

            PreparedStatement stmt = con.prepareStatement("select * from  system where instance_id like ?");
            stmt.setString(1, instanceId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setDisplayNm(rs.getString("display_nm"));
                hostSystem.setInstanceId(rs.getString("instance_id"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setKeyId(rs.getLong("key_id"));
                hostSystem.setEc2Region(rs.getString("region"));
                hostSystem.setState(rs.getString("state"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hostSystem;
    }


    /**
     * inserts host system into DB
     *
     * @param con        DB connection object
     * @param hostSystem host system object
     */
    public static void insertSystem(Connection con, HostSystem hostSystem) {

        try {

            PreparedStatement stmt = con.prepareStatement("insert into system (display_nm, user, host, port, instance_id, key_id, region, state) values (?,?,?,?,?,?,?,?)");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getInstanceId());
            stmt.setLong(6, hostSystem.getKeyId());
            stmt.setString(7, hostSystem.getEc2Region());
            stmt.setString(8, hostSystem.getState());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * updates host system record
     *
     * @param hostSystem host system object
     */
    public static void updateSystem(HostSystem hostSystem) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            updateSystem(con, hostSystem);


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }

    /**
     * updates host system record
     *
     * @param con        DB connection object
     * @param hostSystem host system object
     */
    public static void updateSystem(Connection con, HostSystem hostSystem) {


        try {

            PreparedStatement stmt = con.prepareStatement("update system set display_nm=?, user=?, host=?, port=?, instance_id=?, key_id=?, region=?, state=?  where id=?");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getInstanceId());
            stmt.setLong(6, hostSystem.getKeyId());
            stmt.setString(7, hostSystem.getEc2Region());
            stmt.setString(8, hostSystem.getState());
            stmt.setLong(9, hostSystem.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * set host systems for region based on list
     *
     * @param hostSystemList list of host system object
     */
    public static void setSystems(List<HostSystem> hostSystemList) {
        Connection con = null;
        try {
            con = DBUtils.getConn();


            //insert new host systems
            for (HostSystem hostSystem : hostSystemList) {
                HostSystem hostSystemTmp = getSystem(con, hostSystem.getInstanceId());
                if (hostSystemTmp == null) {
                    insertSystem(con, hostSystem);
                } else {
                    hostSystem.setId(hostSystemTmp.getId());
                    hostSystem.setUser(hostSystemTmp.getUser());
                    hostSystem.setPort(hostSystemTmp.getPort());
                    updateSystem(con, hostSystem);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


}
