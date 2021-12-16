/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * DAO used to manage systems
 */
public class SystemDB {


    private static Logger log = LoggerFactory.getLogger(SystemDB.class);

    public static final String DISPLAY_NM = "display_nm";
    public static final String USER = "user";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String INSTANCE_ID = "instance_id";
    public static final String REGION = "region";
    public static final String STATE = "state";
    public static final String INSTANCE_STATUS = "instance_status";
    public static final String SYSTEM_STATUS = "system_status";
    public static final String M_ALARM = "m_alarm";
    public static final String M_INSUFFICIENT_DATA = "m_insufficient_data";
    public static final String M_OK = "m_ok";
    public static final String ID = "id";

    public static final String SORT_BY_NAME = DISPLAY_NM;
    public static final String SORT_BY_INSTANCE_ID = INSTANCE_ID;
    public static final String SORT_BY_USER = USER;
    public static final String SORT_BY_HOST = HOST;
    public static final String SORT_BY_PORT = PORT;
    public static final String SORT_BY_STATE = STATE;
    public static final String SORT_BY_INSTANCE_STATUS = INSTANCE_STATUS;
    public static final String SORT_BY_SYSTEM_STATUS = SYSTEM_STATUS;
    public static final String SORT_BY_ALARMS= "alarms";
    public static final String SORT_BY_REGION = REGION;

    private SystemDB() {
    }


    /**
     * method to do order by based on the sorted set object for systems. only selects instance IDs in provided list.
     *
     * @param sortedSet      sorted set object
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet) {
        List<HostSystem> hostSystemList = new ArrayList<>();


            String orderBy = "";
            if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
                orderBy = " order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
            }
            StringBuilder sqlBuilder = new StringBuilder("select *, CONCAT_WS('-',m_alarm,m_insufficient_data,m_ok) as alarms from  system");
            sqlBuilder.append(orderBy);

            Connection con = null;
            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement(sqlBuilder.toString());

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    HostSystem hostSystem = new HostSystem();
                    hostSystem.setId(rs.getLong(ID));
                    hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
                    hostSystem.setInstance(rs.getString(INSTANCE_ID));
                    hostSystem.setUser(rs.getString(USER));
                    hostSystem.setHost(rs.getString(HOST));
                    hostSystem.setPort(rs.getInt(PORT));
                    hostSystem.setEc2Region(rs.getString(REGION));
                    hostSystem.setState(rs.getString(STATE));
                    hostSystem.setInstanceStatus(rs.getString(INSTANCE_STATUS));
                    hostSystem.setSystemStatus(rs.getString(SYSTEM_STATUS));
                    hostSystem.setMonitorAlarm(rs.getInt(M_ALARM));
                    hostSystem.setMonitorInsufficientData(rs.getInt(M_INSUFFICIENT_DATA));
                    hostSystem.setMonitorOk(rs.getInt(M_OK));
                    hostSystemList.add(hostSystem);
                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            finally {
                DBUtils.closeConn(con);
            }

            sortedSet.setItemList(hostSystemList);
        return sortedSet;

    }

    /**
     * method to do order by based on the sorted set object for systems. only selects instance IDs in provided list.
     *
     * @param sortedSet      sorted set object
     * @param instanceIdList instance ids to select
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet, List<String> instanceIdList) {
        List<HostSystem> hostSystemList = new ArrayList<>();

        if (instanceIdList != null && !instanceIdList.isEmpty()) {

            String orderBy = "";
            if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
                orderBy = " order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
            }
            StringBuilder sqlBuilder = new StringBuilder("select *, CONCAT_WS('-',m_alarm,m_insufficient_data,m_ok) as alarms from  system  where instance_id in ( ");
            for (int i = 0; i < instanceIdList.size(); i++) {
                if (i == instanceIdList.size() - 1) sqlBuilder.append("'").append(instanceIdList.get(i)).append("') ");
                else sqlBuilder.append("'").append(instanceIdList.get(i)).append("', ");
            }

            sqlBuilder.append(orderBy);

            Connection con = null;
            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement(sqlBuilder.toString());

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    HostSystem hostSystem = new HostSystem();
                    hostSystem.setId(rs.getLong(ID));
                    hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
                    hostSystem.setInstance(rs.getString(INSTANCE_ID));
                    hostSystem.setUser(rs.getString(USER));
                    hostSystem.setHost(rs.getString(HOST));
                    hostSystem.setPort(rs.getInt(PORT));
                    hostSystem.setEc2Region(rs.getString(REGION));
                    hostSystem.setState(rs.getString(STATE));
                    hostSystem.setInstanceStatus(rs.getString(INSTANCE_STATUS));
                    hostSystem.setSystemStatus(rs.getString(SYSTEM_STATUS));
                    hostSystem.setMonitorAlarm(rs.getInt(M_ALARM));
                    hostSystem.setMonitorInsufficientData(rs.getInt(M_INSUFFICIENT_DATA));
                    hostSystem.setMonitorOk(rs.getInt(M_OK));
                    hostSystemList.add(hostSystem);
                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            finally {
                DBUtils.closeConn(con);
            }

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
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

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
                hostSystem.setId(rs.getLong(ID));
                hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
                hostSystem.setInstance(rs.getString(INSTANCE_ID));
                hostSystem.setUser(rs.getString(USER));
                hostSystem.setHost(rs.getString(HOST));
                hostSystem.setPort(rs.getInt(PORT));
                hostSystem.setEc2Region(rs.getString(REGION));
                hostSystem.setState(rs.getString(STATE));
                hostSystem.setMonitorAlarm(rs.getInt(M_ALARM));
                hostSystem.setMonitorInsufficientData(rs.getInt(M_INSUFFICIENT_DATA));
                hostSystem.setMonitorOk(rs.getInt(M_OK));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
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
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

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
                hostSystem.setId(rs.getLong(ID));
                hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
                hostSystem.setInstance(rs.getString(INSTANCE_ID));
                hostSystem.setUser(rs.getString(USER));
                hostSystem.setHost(rs.getString(HOST));
                hostSystem.setPort(rs.getInt(PORT));
                hostSystem.setEc2Region(rs.getString(REGION));
                hostSystem.setState(rs.getString(STATE));
                hostSystem.setMonitorAlarm(rs.getInt(M_ALARM));
                hostSystem.setMonitorInsufficientData(rs.getInt(M_INSUFFICIENT_DATA));
                hostSystem.setMonitorOk(rs.getInt(M_OK));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
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

            PreparedStatement stmt = con.prepareStatement("insert into system (display_nm, user, host, port, instance_id, region, state, instance_status, system_status, m_alarm, m_insufficient_data, m_ok) values (?,?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getInstance());
            stmt.setString(6, hostSystem.getEc2Region());
            stmt.setString(7, hostSystem.getState());
            stmt.setString(8, hostSystem.getInstanceStatus());
            stmt.setString(9, hostSystem.getSystemStatus());
            stmt.setInt(10, hostSystem.getMonitorAlarm());
            stmt.setInt(11, hostSystem.getMonitorInsufficientData());
            stmt.setInt(12, hostSystem.getMonitorOk());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
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
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

    }

    /**
     * updates host system record
     *
     * @param con        DB connection object
     * @param hostSystem host system object
     */
    public static void updateSystem(Connection con, HostSystem hostSystem) {


        try {

            PreparedStatement stmt = con.prepareStatement("update system set display_nm=?, user=?, host=?, port=?, instance_id=?,  region=?, state=?, instance_status=?, system_status=?, m_alarm=?, m_insufficient_data=?, m_ok=?  where id=?");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getInstance());
            stmt.setString(6, hostSystem.getEc2Region());
            stmt.setString(7, hostSystem.getState());
            stmt.setString(8, hostSystem.getInstanceStatus());
            stmt.setString(9, hostSystem.getSystemStatus());
            stmt.setInt(10, hostSystem.getMonitorAlarm());
            stmt.setInt(11, hostSystem.getMonitorInsufficientData());
            stmt.setInt(12, hostSystem.getMonitorOk());
            stmt.setLong(13, hostSystem.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

    }


    /**
     * set host systems for region based on list
     *
     * @param hostSystemList list of host system object
     */
    public static void setSystems(Collection<HostSystem> hostSystemList) {
        Connection con = null;
        try {
            con = DBUtils.getConn();

            //insert new host systems
            for (HostSystem hostSystem : hostSystemList) {
                HostSystem hostSystemTmp = getSystem(con, hostSystem.getInstance());
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
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

    }


    /**
     * returns list of systems by system instance id
     *
     * @param instanceIdList system instance id
     * @return system
     */
    public static List<HostSystem> getSystems(List<String> instanceIdList) {

        Connection con = null;

        List<HostSystem> hostSystemList = new ArrayList<>();
        if(instanceIdList != null) {
            for (String instanceId : instanceIdList) {

                try {
                    con = DBUtils.getConn();

                    hostSystemList.add(getSystem(con, instanceId));


                } catch (Exception e) {
                    log.error(e.toString(), e);
                } finally {
                    DBUtils.closeConn(con);
                }

            }
        }

        return hostSystemList;
    }





}
