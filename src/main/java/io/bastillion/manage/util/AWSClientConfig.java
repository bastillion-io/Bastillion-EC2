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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * return client configuration for AWS service calls
 */
public class AWSClientConfig {

    private static final Logger log = LoggerFactory.getLogger(AWSClientConfig.class);

    private static final ClientConfiguration config = new ClientConfiguration();

    private static String accessKey = "5ij8Zolfv+cZbgBRWop8cklIaqPxO4G/xb2PoM1zydo=";
    private static String secretKey = "tMbPt2cQEwNvy90Dn/I55r/ajVAO+otM7+g4yImM3qBpkYyy5uvNXXZeuWXNm2ok";
    private static String sessionToken;
    private static Calendar time = Calendar.getInstance();

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
    public static BasicSessionCredentials getCredentials() throws GeneralSecurityException, SQLException {
        return getCredentials(null);
    }

    /**
     * Get AWS temporary credentials using a delegate for ARN
     *
     * @param arn Amazon Resource Name
     * @return BasicSessionCredentials
     */
    public static BasicSessionCredentials getCredentials(String arn) throws GeneralSecurityException, SQLException {
        if (arn == null || arn.trim().equals("")) {
            arn = IAMRoleDB.getIAMRole();
        }

        if (accessKey == null || time == null || time.before(Calendar.getInstance())) {

            BasicAWSCredentials longTermCredentials = new BasicAWSCredentials(EncryptionUtil.decryptStatic(accessKey),
                    EncryptionUtil.decryptStatic(secretKey));

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

        return new BasicSessionCredentials(accessKey, secretKey, sessionToken);
    }
}
