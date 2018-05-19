/**
 *    Copyright (C) 2013 Loophole, LLC
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
package com.ec2box.manage.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.ec2box.common.util.AppConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * return client configuration for AWS service calls
 */
public class AWSClientConfig {

    private static ClientConfiguration config = new ClientConfiguration();

    /**
     * set config info based on AppConfig
     */
    static {
        String awsProtocol= AppConfig.getProperty("awsProtocol");
        String awsProxyHost = AppConfig.getProperty("awsProxyHost");
        String awsProxyPort = AppConfig.getProperty("awsProxyPort");
        String awsProxyUser = AppConfig.getProperty("awsProxyUser");
        String awsProxyPassword = AppConfig.getProperty("awsProxyPassword");

        if("http".equals(awsProtocol)){
            config.setProtocol(Protocol.HTTP);
        }
        else {
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
     * @return client configuration information
     */
    public static ClientConfiguration getClientConfig() {

        return config;

    }
}
