/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

/**
 * Value object that contains login information
 */
public class Auth {
    public static final String ADMINISTRATOR="A";
    public static final String MANAGER="M";

    public static final String AUTH_BASIC="BASIC";
    public static final String AUTH_EXTERNAL="EXTERNAL";

    Long id;
    String username;
    String password;
    String passwordConfirm;
    String prevPassword;
    String authToken;
    String otpSecret;
    Long otpToken;
    String salt;
    String userType=ADMINISTRATOR;
    String authType=AUTH_BASIC;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getPrevPassword() {
        return prevPassword;
    }

    public void setPrevPassword(String prevPassword) {
        this.prevPassword = prevPassword;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getOtpSecret() {
        return otpSecret;
    }

    public void setOtpSecret(String otpSecret) {
        this.otpSecret = otpSecret;
    }

    public Long getOtpToken() {
        return otpToken;
    }

    public void setOtpToken(Long otpToken) {
        this.otpToken = otpToken;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }
}
