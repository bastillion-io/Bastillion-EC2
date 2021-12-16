/**
 *    Copyright (C) 2018 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO to manage Amazon Resource Name for client API calls
 */
public class IAMRoleDB {

    private static Logger log = LoggerFactory.getLogger(IAMRoleDB.class);


    private IAMRoleDB() {
    }


    /**
     * returns Amazon Resource Name
     *
     * @return  arn amazon resource name
     */
    public static String getIAMRole() {


        String arn = null;

        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from iam_role");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                arn = EncryptionUtil.decrypt(rs.getString("arn"));

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);


        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return arn;

    }

    /**
     * updates Amazon Resource Name
     *
     * @param arn amazon resource name
     */
    public static void updateIAMRole(String arn) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //update
            PreparedStatement stmt = con.prepareStatement("update iam_role set arn = ?");
            stmt.setString(1, EncryptionUtil.encrypt(arn));
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

    }

    /**
     * inserts Amazon Resource Name
     *
     * @param arn amazon resource name
     */
    public static void insertIAMRole(String arn) {

        //get db connection
        Connection con = DBUtils.getConn();

        try {
            //insert
            PreparedStatement stmt = con.prepareStatement("insert into iam_role (arn) values(?)");
            stmt.setString(1, EncryptionUtil.encrypt(arn.trim()));
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * insert or updated an ARN
     *
     * @param arn amazon resource name
     */
    public static void saveIAMRole(String arn) {

        String arnTmp = getIAMRole();
        if (arnTmp!= null) {
            updateIAMRole(arn);
        } else {
            insertIAMRole(arn);
        }

    }


}
