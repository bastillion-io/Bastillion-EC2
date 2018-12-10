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
package io.bastillion.manage.db;

import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.util.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO used to keep track of the status when uploading files or connectiong to instances to systems
 */
public class SystemStatusDB {

    private static Logger log = LoggerFactory.getLogger(SystemStatusDB.class);

    private SystemStatusDB() {
    }
    public static final String STATUS_CD = "status_cd";

    /**
     * set the initial status for selected systems
     *
     * @param systemSelectIds systems ids to set initial status
     * @param userId user id
     */
    public static void setInitialSystemStatus(List<Long> systemSelectIds, Long userId, HttpServletRequest servletRequest) {
        Connection con = null;
        try {
            con = DBUtils.getConn();

            if (!Auth.MANAGER.equals(AuthUtil.getUserType(servletRequest.getSession()))) {

                //make sure selected instance id is host the user has permission to.
                List<String> instanceIdList= (List<String>)servletRequest.getSession().getAttribute("instanceIdList");
                List<Long> systemIdList = new ArrayList<>();
                for (HostSystem hostSystem : SystemDB.getSystems(instanceIdList)) {
                    if (systemSelectIds.contains(hostSystem.getId())) {
                        systemIdList.add(hostSystem.getId());
                    }
                }
                systemSelectIds = systemIdList;

            }
            //deletes all old systems
            deleteAllSystemStatus(con, userId);


            for (Long hostSystemId : systemSelectIds) {

                HostSystem hostSystem= new HostSystem();
                hostSystem.setId(hostSystemId);
                hostSystem.setStatusCd(HostSystem.INITIAL_STATUS);

                //insert new status
                insertSystemStatus(con, hostSystem, userId);


            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * deletes all records from status table
     *
     * @param con DB connection object
     * @param userId user id
     */
    private static void deleteAllSystemStatus(Connection con, Long userId) {

        try {

            PreparedStatement stmt = con.prepareStatement("delete from status where user_id=?");
            stmt.setLong(1,userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

    }


    /**
     * inserts into the status table to keep track of status
     *
     * @param con DB connection object
     * @param hostSystem systems
     * @param userId user id
     *
     */
    private static void insertSystemStatus(Connection con, HostSystem hostSystem, Long userId) {

        try {

            PreparedStatement stmt = con.prepareStatement("insert into status (id, status_cd, user_id) values (?,?,?)");
            stmt.setLong(1, hostSystem.getId());
            stmt.setString(2, hostSystem.getStatusCd());
            stmt.setLong(3, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }


    }

    /**
     * updates the status table to keep track of status
     *
     * @param hostSystem systems
     * @param userId user id
     */
    public static void updateSystemStatus(HostSystem hostSystem, Long userId) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            updateSystemStatus(con, hostSystem, userId);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

    }


    /**
     * updates the status table to keep track of system status
     *
     * @param con DB connection
     * @param hostSystem systems
     * @param userId user id
     */
    public static void updateSystemStatus(Connection con, HostSystem hostSystem, Long userId) {

        try {

            PreparedStatement stmt = con.prepareStatement("update status set status_cd=? where id=? and user_id=?");
            stmt.setString(1, hostSystem.getStatusCd());
            stmt.setLong(2, hostSystem.getId());
            stmt.setLong(3, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }


    }

    /**
     * returns all statuses
     *
     * @param userId user id
     */
    public static List<HostSystem> getAllSystemStatus(Long userId) {

        List<HostSystem> hostSystemList = new ArrayList<>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            hostSystemList = getAllSystemStatus(con, userId);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
        return hostSystemList;

    }

    /**
     * returns all statuses
     *
     * @param con DB connection object
     * @param userId user id
     */
    private static List<HostSystem> getAllSystemStatus(Connection con, Long userId) {

        List<HostSystem> hostSystemList = new ArrayList<>();
        try {

            PreparedStatement stmt = con.prepareStatement("select * from status where user_id=? order by id asc");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HostSystem hostSystem = SystemDB.getSystem(con, rs.getLong("id"));
                hostSystem.setStatusCd(rs.getString(STATUS_CD));
                hostSystemList.add(hostSystem);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return hostSystemList;

    }


    /**
     * returns status of system
     *
     * @param systemId system id
     * @param userId user id
     */
    public static HostSystem getSystemStatus(Long systemId, Long userId) {

        Connection con = null;
        HostSystem hostSystem= null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from status where id=? and user_id=?");
            stmt.setLong(1, systemId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hostSystem= SystemDB.getSystem(con, rs.getLong("id"));
                hostSystem.setStatusCd(rs.getString(STATUS_CD));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
        return hostSystem;
    }


    /**
     * returns the first system that has not been tried
     *
     * @param userId user id
     * @return systemStatus systems
     */
    public static HostSystem getNextPendingSystem(Long userId) {

        HostSystem hostSystem= null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from status where (status_cd like ? or status_cd like ? or status_cd like ?) and user_id=? order by id asc");
            stmt.setString(1,HostSystem.INITIAL_STATUS);
            stmt.setString(2,HostSystem.AUTH_FAIL_STATUS);
            stmt.setString(3,HostSystem.PUBLIC_KEY_FAIL_STATUS);
            stmt.setLong(4,userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                hostSystem=SystemDB.getSystem(con, rs.getLong("id"));
                hostSystem.setStatusCd(rs.getString(STATUS_CD));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
        return hostSystem;

    }

}
