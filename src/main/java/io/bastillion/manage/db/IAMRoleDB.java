/**
 *    Copyright (C) 2018 Loophole, LLC
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
