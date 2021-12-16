/**
 * Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.db.IAMRoleDB;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

/**
 * return client configuration for AWS service calls
 */
public class AWSClientConfig {

    private static ClientConfiguration config = new ClientConfiguration();

    private static String accessKey;
    private static String secretKey;
    private static String sessionToken;
    private static Calendar time = Calendar.getInstance();;

    static {
        if (!AppConfig.isPropertyEncrypted("accessKey")) {
            AppConfig.encryptProperty("accessKey", AppConfig.getProperty("accessKey"));
        }
        if (!AppConfig.isPropertyEncrypted("secretKey")) {
            AppConfig.encryptProperty("secretKey", AppConfig.getProperty("secretKey"));
        }
    }

    /**
     * set config info based on AppConfig
     */
    static {
        String awsProtocol = AppConfig.getProperty("awsProtocol");
        String awsProxyHost = AppConfig.getProperty("awsProxyHost");
        String awsProxyPort = AppConfig.getProperty("awsProxyPort");
        String awsProxyUser = AppConfig.getProperty("awsProxyUser");
        String awsProxyPassword = AppConfig.getProperty("awsProxyPassword");

        if ("http".equals(awsProtocol)) {
            config.setProtocol(Protocol.HTTP);
        } else {
            config.setProtocol(Protocol.HTTPS);
        }
        if (StringUtils.isNotEmpty(awsProxyHost)) {
            config.setProxyHost(awsProxyHost);
        }
        if (StringUtils.isNotEmpty(awsProxyPort)) {
            config.setProxyPort(Integer.parseInt(awsProxyPort));
        }
        if (StringUtils.isNotEmpty(awsProxyUser)) {
            config.setProxyUsername(awsProxyUser);
        }
        if (StringUtils.isNotEmpty(awsProxyPassword)) {
            config.setProxyPassword(awsProxyPassword);
        }

    }

    private AWSClientConfig() {
    }

    /**
     * return configuration information for AWS client
     *
     * @return client configuration information
     */
    public static ClientConfiguration getClientConfig() {

        return config;

    }

    /**
     * Get AWS temporary credentials using a delegate for ARN
     *
     * @return BasicSessionCredentials
     */
    public static BasicSessionCredentials getCredentials() {
        return getCredentials(null);
    }

    /**
     * Get AWS temporary credentials using a delegate for ARN
     *
     * @param arn Amazon Resource Name
     * @return BasicSessionCredentials
     */
    public static BasicSessionCredentials getCredentials(String arn) {
        if (arn == null || arn.trim().equals("")) {
            arn = IAMRoleDB.getIAMRole();
        }

        if (accessKey == null || time == null || time.before(Calendar.getInstance())) {

            BasicAWSCredentials longTermCredentials = new BasicAWSCredentials(AppConfig.decryptProperty("accessKey"), AppConfig.decryptProperty("secretKey"));

            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).withCredentials(new AWSStaticCredentialsProvider(longTermCredentials)).build();

            AssumeRoleRequest assumeRequest = new AssumeRoleRequest()
                    .withRoleArn(arn)
                    .withRoleSessionName("Bastillion-EC2");

            AssumeRoleResult assumeResult =
                    stsClient.assumeRole(assumeRequest);

            accessKey = assumeResult.getCredentials().getAccessKeyId();
            secretKey = assumeResult.getCredentials().getSecretAccessKey();
            sessionToken = assumeResult.getCredentials().getSessionToken();

            //refresh access keys in 10 mins
            time = Calendar.getInstance();
            time.add(Calendar.MINUTE,10);

        }
        BasicSessionCredentials awsCredentials =
                new BasicSessionCredentials(accessKey, secretKey, sessionToken);

        return awsCredentials;
    }
}
