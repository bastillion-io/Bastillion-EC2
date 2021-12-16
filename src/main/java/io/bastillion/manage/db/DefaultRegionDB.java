/**
 * Copyright (C) 2018 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.util.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO to set default region
 */
public class DefaultRegionDB {

    private static Logger log = LoggerFactory.getLogger(DefaultRegionDB.class);


    private DefaultRegionDB() {
    }


    /**
     * returns default region
     *
     * @return region default region
     */
    public static String getRegion() {


        String region = null;

        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from default_region");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                region = rs.getString("region");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);


        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        } finally {
            DBUtils.closeConn(con);
        }

        return region;

    }

    /**
     * updates default region
     *
     * @param region default region
     */
    public static void updateRegion(String region) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //update
            PreparedStatement stmt = con.prepareStatement("update default_region set region = ?");
            stmt.setString(1, region);
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBUtils.closeConn(con);
        }

    }

    /**
     * inserts default region
     *
     * @param region default region
     */
    public static void insertRegion(String region) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //insert
            PreparedStatement stmt = con.prepareStatement("insert into default_region (region) values(?)");
            stmt.setString(1, region);
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * insert or updated an region
     *
     * @param region default region
     */
    public static void saveRegion(String region) {

        String regionTmp = getRegion();
        if (regionTmp != null) {
            updateRegion(region);
        } else {
            insertRegion(region);
        }

    }

}
