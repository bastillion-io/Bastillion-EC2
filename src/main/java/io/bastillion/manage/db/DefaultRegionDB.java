/**
 * Copyright (C) 2018 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.util.DBUtils;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO to set default region
 */
public class DefaultRegionDB {

    private DefaultRegionDB() {
    }


    /**
     * returns default region
     *
     * @return region default region
     */
    public static String getRegion() throws SQLException, GeneralSecurityException {

        String region = null;

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select * from default_region");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            region = rs.getString("region");
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return region;

    }

    /**
     * updates default region
     *
     * @param region default region
     */
    public static void updateRegion(String region) throws SQLException, GeneralSecurityException {

        //get db connection
        Connection con = DBUtils.getConn();

        //update
        PreparedStatement stmt = con.prepareStatement("update default_region set region = ?");
        stmt.setString(1, region);
        stmt.execute();

        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

    }

    /**
     * inserts default region
     *
     * @param region default region
     */
    public static void insertRegion(String region) throws SQLException, GeneralSecurityException {

        //get db connection
        Connection con = DBUtils.getConn();
        //insert
        PreparedStatement stmt = con.prepareStatement("insert into default_region (region) values(?)");
        stmt.setString(1, region);
        stmt.execute();

        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * insert or updated an region
     *
     * @param region default region
     */
    public static void saveRegion(String region) throws SQLException, GeneralSecurityException {

        String regionTmp = getRegion();
        if (regionTmp != null) {
            updateRegion(region);
        } else {
            insertRegion(region);
        }
    }

}
