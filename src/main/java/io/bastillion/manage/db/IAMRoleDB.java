/**
 * Copyright (C) 2018 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO to manage Amazon Resource Name for client API calls
 */
public class IAMRoleDB {

    private IAMRoleDB() {
    }

    /**
     * returns Amazon Resource Name
     *
     * @return arn amazon resource name
     */
    public static String getIAMRole() throws SQLException, GeneralSecurityException {

        String arn = null;

        Connection con = DBUtils.getConn();

        PreparedStatement stmt = con.prepareStatement("select * from iam_role");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            arn = EncryptionUtil.decrypt(rs.getString("arn"));

        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return arn;

    }

    /**
     * updates Amazon Resource Name
     *
     * @param arn amazon resource name
     */
    public static void updateIAMRole(String arn) throws SQLException, GeneralSecurityException {

        //get db connection
        Connection con = DBUtils.getConn();

        //update
        PreparedStatement stmt = con.prepareStatement("update iam_role set arn = ?");
        stmt.setString(1, EncryptionUtil.encrypt(arn));
        stmt.execute();

        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * inserts Amazon Resource Name
     *
     * @param arn amazon resource name
     */
    public static void insertIAMRole(String arn) throws SQLException, GeneralSecurityException {

        //get db connection
        Connection con = DBUtils.getConn();

        PreparedStatement stmt = con.prepareStatement("insert into iam_role (arn) values(?)");
        stmt.setString(1, EncryptionUtil.encrypt(arn.trim()));
        stmt.execute();

        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * insert or updated an ARN
     *
     * @param arn amazon resource name
     */
    public static void saveIAMRole(String arn) throws SQLException, GeneralSecurityException {

        String arnTmp = getIAMRole();
        if (arnTmp != null) {
            updateIAMRole(arn);
        } else {
            insertIAMRole(arn);
        }

    }

}
