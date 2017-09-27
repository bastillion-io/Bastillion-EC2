/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ec2box.manage.model;

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
