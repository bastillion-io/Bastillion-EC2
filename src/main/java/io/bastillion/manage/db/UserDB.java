/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;
import org.apache.commons.lang3.StringUtils;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * DAO class to manage users
 */
public class UserDB {

    public static final String PASSWORD = "password";
    public static final String FIRST_NM = "first_nm";
    public static final String LAST_NM = "last_nm";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String USER_TYPE = "user_type";
    public static final String AUTH_TYPE = "auth_type";
    public static final String PROFILE_ID = "profile_id";

    private UserDB() {
    }

    /**
     * returns users based on sort order defined
     *
     * @param sortedSet object that defines sort order
     * @return sorted user list
     */
    public static SortedSet getUserSet(SortedSet sortedSet) throws SQLException, GeneralSecurityException {

        ArrayList<User> userList = new ArrayList<>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from  users " + orderBy;

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setFirstNm(rs.getString(FIRST_NM));
            user.setLastNm(rs.getString(LAST_NM));
            user.setEmail(rs.getString(EMAIL));
            user.setUsername(rs.getString(USERNAME));
            user.setPassword(rs.getString(PASSWORD));
            user.setAuthType(rs.getString(AUTH_TYPE));
            user.setUserType(rs.getString(USER_TYPE));
            userList.add(user);

        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        sortedSet.setItemList(userList);
        return sortedSet;
    }

    /**
     * returns all admin users based on sort order defined
     *
     * @param sortedSet object that defines sort order
     * @return sorted user list
     * @profileId check if user is apart of given profile
     */
    public static SortedSet getAdminUserSet(SortedSet sortedSet, Long profileId) throws SQLException, GeneralSecurityException {

        ArrayList<User> userList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select u.*, m.profile_id from users u left join user_map  m on m.user_id = u.id and m.profile_id = ? where u.user_type like '" + User.ADMINISTRATOR + "'" + orderBy;

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setLong(1, profileId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setFirstNm(rs.getString(FIRST_NM));
            user.setLastNm(rs.getString(LAST_NM));
            user.setEmail(rs.getString(EMAIL));
            user.setUsername(rs.getString(USERNAME));
            user.setPassword(rs.getString(PASSWORD));
            user.setAuthType(rs.getString(AUTH_TYPE));
            user.setUserType(rs.getString(USER_TYPE));
            user.setChecked(profileId.equals(rs.getLong(PROFILE_ID)));
            userList.add(user);

        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        sortedSet.setItemList(userList);
        return sortedSet;
    }


    /**
     * returns user base on id
     *
     * @param userId user id
     * @return user object
     */
    public static User getUser(Long userId) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        User user = getUser(con, userId);
        DBUtils.closeConn(con);

        return user;
    }

    /**
     * returns user base on id
     *
     * @param con    DB connection
     * @param userId user id
     * @return user object
     */
    public static User getUser(Connection con, Long userId) throws SQLException {

        User user = null;
        PreparedStatement stmt = con.prepareStatement("select * from  users where id=?");
        stmt.setLong(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            user = new User();
            user.setId(rs.getLong("id"));
            user.setFirstNm(rs.getString(FIRST_NM));
            user.setLastNm(rs.getString(LAST_NM));
            user.setEmail(rs.getString(EMAIL));
            user.setUsername(rs.getString(USERNAME));
            user.setPassword(rs.getString(PASSWORD));
            user.setAuthType(rs.getString(AUTH_TYPE));
            user.setUserType(rs.getString(USER_TYPE));
            user.setSalt(rs.getString("salt"));
            user.setProfileList(UserProfileDB.getProfilesByUser(con, userId));
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);

        return user;
    }

    /**
     * inserts new user
     *
     * @param user user object
     */
    public static Long insertUser(User user) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        Long userId = insertUser(con, user);
        DBUtils.closeConn(con);

        return userId;
    }

    /**
     * inserts new user
     *
     * @param con  DB connection
     * @param user user object
     */
    public static Long insertUser(Connection con, User user) throws SQLException, NoSuchAlgorithmException {

        Long userId = null;

        PreparedStatement stmt = con.prepareStatement("insert into users (first_nm, last_nm, email, username, auth_type, user_type, password, salt) values (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, user.getFirstNm());
        stmt.setString(2, user.getLastNm());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getUsername());
        stmt.setString(5, user.getAuthType());
        stmt.setString(6, user.getUserType());
        if (StringUtils.isNotEmpty(user.getPassword())) {
            String salt = EncryptionUtil.generateSalt();
            stmt.setString(7, EncryptionUtil.hash(user.getPassword() + salt));
            stmt.setString(8, salt);
        } else {
            stmt.setString(7, null);
            stmt.setString(8, null);
        }
        stmt.execute();
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs != null && rs.next()) {
            userId = rs.getLong(1);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);

        return userId;
    }

    /**
     * updates existing user
     *
     * @param user user object
     */
    public static void updateUserNoCredentials(User user) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("update users set first_nm=?, last_nm=?, email=?, username=?, user_type=? where id=?");
        stmt.setString(1, user.getFirstNm());
        stmt.setString(2, user.getLastNm());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getUsername());
        stmt.setString(5, user.getUserType());
        stmt.setLong(6, user.getId());
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * updates existing user
     *
     * @param user user object
     */
    public static void updateUserCredentials(User user) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        String salt = EncryptionUtil.generateSalt();
        PreparedStatement stmt = con.prepareStatement("update users set first_nm=?, last_nm=?, email=?, username=?, user_type=?, password=?, salt=? where id=?");
        stmt.setString(1, user.getFirstNm());
        stmt.setString(2, user.getLastNm());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getUsername());
        stmt.setString(5, user.getUserType());
        stmt.setString(6, EncryptionUtil.hash(user.getPassword() + salt));
        stmt.setString(7, salt);
        stmt.setLong(8, user.getId());
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * deletes user
     *
     * @param userId user id
     */
    public static void deleteUser(Long userId) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("delete from users where id=?");
        stmt.setLong(1, userId);
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * resets shared secret for user
     *
     * @param userId user id
     */
    public static void resetSharedSecret(Long userId) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("update users set otp_secret=null where id=?");
        stmt.setLong(1, userId);
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * checks to see if username is unique while ignoring current user
     *
     * @param userId   user id
     * @param username username
     * @return true false indicator
     */
    public static boolean isUnique(Long userId, String username) throws SQLException, GeneralSecurityException {

        boolean isUnique = true;
        if (userId == null) {
            userId = -99L;
        }

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select * from users where lower(username) like lower(?) and id != ?");
        stmt.setString(1, username);
        stmt.setLong(2, userId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            isUnique = false;
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return isUnique;
    }

}
