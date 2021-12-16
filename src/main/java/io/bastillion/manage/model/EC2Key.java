/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
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
