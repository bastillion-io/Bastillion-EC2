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
import com.ec2box.manage.model.SystemStatus;
import com.ec2box.manage.util.DBUtils;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO used to generate a list of public keys and systems associated
 * with them based on system profiles and users assigned to the profiles.
 */
public class SystemStatusDB {









    /**
     * set the initial status for selected systems
     *
     * @param systemSelectIds systems ids to set initial status
     */
    public static List<SystemStatus> setInitialSystemStatus(List<Long> systemSelectIds) {
        Connection con = null;
        List<SystemStatus> systemStatusList = new ArrayList<SystemStatus>();
        try {
            con = DBUtils.getConn();

            //deletes all old systems
            deleteAllSystemStatus(con);


            for (Long hostSystemId : systemSelectIds) {

                HostSystem hostSystem= SystemDB.getSystem(con, hostSystemId);

                SystemStatus systemStatus = new SystemStatus();
                systemStatus.setId(hostSystem.getId());
                systemStatus.setStatusCd(SystemStatus.INITIAL_STATUS);

                //insert new status
                insertSystemStatus(con, systemStatus);

                //get update status list
                systemStatusList = getAllSystemStatus(con);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatusList;
    }

    /**
     * deletes all records from status table
     *
     * @param con DB connection object
     */
    private static void deleteAllSystemStatus(Connection con) {

        try {

            PreparedStatement stmt = con.prepareStatement("delete from status");
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * inserts into the status table to keep track of key placement status
     *
     * @param con                DB connection object
     * @param systemStatus systems for authorized_keys replacement
     */
    private static void insertSystemStatus(Connection con, SystemStatus systemStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("insert into status (id,status_cd) values (?,?)");
            stmt.setLong(1, systemStatus.getId());
            stmt.setString(2, systemStatus.getStatusCd());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates the status table to keep track of key placement status
     *
     * @param systemStatus systems for authorized_keys replacement
     */
    public static void updateSystemStatus(SystemStatus systemStatus) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            updateSystemStatus(con, systemStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


    /**
     * updates the status table to keep track of key placement status
     *
     * @param con                DB connection
     * @param systemStatus systems for authorized_keys replacement
     */
    public static void updateSystemStatus(Connection con, SystemStatus systemStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("update status set status_cd=? where id=?");
            stmt.setString(1, systemStatus.getStatusCd());
            stmt.setLong(2, systemStatus.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * returns all key placement statuses
     */
    public static List<SystemStatus> getAllSystemStatus() {

        List<SystemStatus> systemStatusList = new ArrayList<SystemStatus>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            systemStatusList = getAllSystemStatus(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatusList;

    }

    /**
     * returns all key placement statuses
     *
     * @param con DB connection object
     */
    private static List<SystemStatus> getAllSystemStatus(Connection con) {

        List<SystemStatus> systemStatusList = new ArrayList<SystemStatus>();
        try {

            PreparedStatement stmt = con.prepareStatement("select * from status");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SystemStatus systemStatus = new SystemStatus();
                systemStatus.setId(rs.getLong("id"));
                systemStatus.setStatusCd(rs.getString("status_cd"));
                systemStatus.setHostSystem(SystemDB.getSystem(con, systemStatus.getId()));
                systemStatusList.add(systemStatus);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return systemStatusList;

    }


    /**
     * returns key placement status of system
     *
     * @param systemId system id
     */
    public static SystemStatus getSystemStatus(Long systemId) {

        Connection con = null;
        SystemStatus systemStatus = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from status where id=?");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                systemStatus = new SystemStatus();
                systemStatus.setId(rs.getLong("id"));
                systemStatus.setStatusCd(rs.getString("status_cd"));
                systemStatus.setHostSystem(SystemDB.getSystem(con, systemStatus.getId()));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatus;


    }


    /**
     * returns the first system that authorized keys has not been tried
     *
     * @return systemStatus systems for authorized_keys replacement
     */
    public static SystemStatus getNextPendingSystem() {

        SystemStatus systemStatus = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from status where status_cd like ? or status_cd like ? or status_cd like ?");
            stmt.setString(1,SystemStatus.INITIAL_STATUS);
            stmt.setString(2,SystemStatus.AUTH_FAIL_STATUS);
            stmt.setString(3,SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                systemStatus = new SystemStatus();
                systemStatus.setId(rs.getLong("id"));
                systemStatus.setStatusCd(rs.getString("status_cd"));
                systemStatus.setHostSystem(SystemDB.getSystem(con, systemStatus.getId()));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatus;

    }

}
