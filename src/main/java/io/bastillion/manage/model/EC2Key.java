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
package io.bastillion.manage.model;

/**
 * Value object that contains information on private key
 */
public class EC2Key {
    Long id;
    String keyNm;
    String privateKey=null;
    String ec2Region;
    Long awsCredId;
    String accessKey;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyNm() {
        return keyNm;
    }

    public void setKeyNm(String keyNm) {
        this.keyNm = keyNm;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getEc2Region() {
        return ec2Region;
    }

    public void setEc2Region(String ec2Region) {
        this.ec2Region = ec2Region;
    }

    public Long getAwsCredId() {
        return awsCredId;
    }

    public void setAwsCredId(Long awsCredId) { this.awsCredId = awsCredId; }

    public String getAccessKey() { return accessKey; }

    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
}
