/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the GNU Affero General Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 */
package io.bastillion.manage.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
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

            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(longTermCredentials)).build();

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
