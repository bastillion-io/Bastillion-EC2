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
package com.ec2box.manage.control;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.ec2box.manage.db.AWSCredDB;
import com.ec2box.manage.db.EC2KeyDB;
import com.ec2box.manage.model.AWSCred;
import com.ec2box.manage.model.EC2Key;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.AWSClientConfig;
import com.google.gson.Gson;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Action to import private key for EC2 instances
 */
public class EC2KeyKtrl extends BaseKontroller {

    public static final String REQUIRED = "Required";
    @Model(name = "ec2RegionMap")
    public static Map<String, String> ec2RegionMap = new LinkedHashMap<>();
    private static Logger log = LoggerFactory.getLogger(EC2KeyKtrl.class);
    @Model(name = "ec2Key")
    EC2Key ec2Key;
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "awsCredList")
    List<AWSCred> awsCredList = new ArrayList<>();

    static {
        if(ec2RegionMap.isEmpty()) {
            for (AWSCred awsCred : AWSCredDB.getAWSCredList()) {
                //set  AWS credentials for service
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());
                AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                        .withRegion(Regions.DEFAULT_REGION)
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .withClientConfiguration(AWSClientConfig.getClientConfig()).build();

                DescribeRegionsResult regionResponse = service.describeRegions();
                for (Region region : regionResponse.getRegions()) {
                    ec2RegionMap.put(region.getEndpoint(), region.getRegionName());
                }
            }
        }
    }


    public EC2KeyKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/viewEC2Keys", method = MethodType.GET)
    public String viewEC2Keys() {

        awsCredList = AWSCredDB.getAWSCredList();
        for (AWSCred awsCred : awsCredList) {
            //set  AWS credentials for service
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());

            //create service
            AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                    .withRegion(Regions.DEFAULT_REGION)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withClientConfiguration(AWSClientConfig.getClientConfig()).build();

            DescribeRegionsResult regionResponse = service.describeRegions();
            for (Region region : regionResponse.getRegions()) {
                ec2RegionMap.put(region.getEndpoint(), region.getRegionName());
            }
        }

        sortedSet = EC2KeyDB.getEC2KeySet(sortedSet);

        return "/manage/view_ec2_keys.html";

    }

    /**
     * returns keypairs as a json string
     */
    @Kontrol(path = "/manage/getKeyPairJSON", method = MethodType.GET)
    public String getKeyPairJSON() {

        AWSCred awsCred = AWSCredDB.getAWSCred(ec2Key.getAwsCredId());

        //set  AWS credentials for service
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());
        AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withClientConfiguration(AWSClientConfig.getClientConfig())
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ec2Key.getEc2Region(), ec2RegionMap.get(ec2Key.getEc2Region()))).build();

        DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();

        DescribeKeyPairsResult describeKeyPairsResult = service.describeKeyPairs(describeKeyPairsRequest);

        List<KeyPairInfo> keyPairInfoList = describeKeyPairsResult.getKeyPairs();
        String json = new Gson().toJson(keyPairInfoList);
        try {
            getResponse().getOutputStream().write(json.getBytes());
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }

        return null;
    }

    @Kontrol(path = "/manage/submitEC2Key", method = MethodType.POST)
    public String submitEC2Key() {

        String retVal = "redirect:/manage/viewEC2Keys.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();

        try {

            //get AWS credentials from DB
            AWSCred awsCred = AWSCredDB.getAWSCred(ec2Key.getAwsCredId());

            //set  AWS credentials for service
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());

            //create service
            AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withClientConfiguration(AWSClientConfig.getClientConfig())
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ec2Key.getEc2Region(), ec2RegionMap.get(ec2Key.getEc2Region()))).build();

            //create key pair request
            CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
            createKeyPairRequest.withKeyName(ec2Key.getKeyNm());

            //call service
            CreateKeyPairResult createKeyPairResult = service.createKeyPair(createKeyPairRequest);
            //get key pair result
            KeyPair keyPair = createKeyPairResult.getKeyPair();

            //set private key
            String privateKey = keyPair.getKeyMaterial();
            ec2Key.setPrivateKey(privateKey);

            //add to db
            EC2KeyDB.saveEC2Key(ec2Key);

        } catch (AmazonServiceException ex) {
            addError(ex.getMessage());
            retVal = "/manage/view_ec2_keys.html";
        }

        return retVal;

    }

    @Kontrol(path = "/manage/importEC2Key", method = MethodType.POST)
    public String importEC2Key() {


        String retVal = "redirect:/manage/viewEC2Keys.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();

        try {
            //get AWS credentials from DB
            AWSCred awsCred = AWSCredDB.getAWSCred(ec2Key.getAwsCredId());

            //set  AWS credentials for service
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());

            //create service
            AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withClientConfiguration(AWSClientConfig.getClientConfig())
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ec2Key.getEc2Region(), ec2RegionMap.get(ec2Key.getEc2Region()))).build();

            //describe key pair request
            DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();
            describeKeyPairsRequest.setKeyNames(Arrays.asList(ec2Key.getKeyNm()));

            //call service
            DescribeKeyPairsResult describeKeyPairsResult = service.describeKeyPairs(describeKeyPairsRequest);


            if (describeKeyPairsResult != null && describeKeyPairsResult.getKeyPairs().size() > 0) {
                //add to db
                EC2KeyDB.saveEC2Key(ec2Key);
            } else {
                addError("Imported key does not exist on AWS");
                retVal = "/manage/view_ec2_keys.html";
            }

        } catch (AmazonServiceException ex) {
            addError(ex.getMessage());
            retVal = "/manage/view_ec2_keys.html";

        }


        return retVal;


    }

    @Kontrol(path = "/manage/deleteEC2Key", method = MethodType.GET)
    public String deleteEC2Key() {
        EC2KeyDB.deleteEC2Key(ec2Key.getId());
        return "redirect:/manage/viewEC2Keys.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }


    /**
     * Validates fields for importing an ec2 key
     */
    @Validate(input = "/manage/view_ec2_keys.html")
    public void validateImportEC2Key() {

        if (ec2Key.getAwsCredId() == null) {
            addFieldError("ec2Key.awsCredId", REQUIRED);
        }
        if (ec2Key.getEc2Region() == null ||
                ec2Key.getEc2Region().trim().equals("")) {
            addFieldError("ec2Key.ec2Region", REQUIRED);
        }
        if (ec2Key.getKeyNm() == null ||
                ec2Key.getKeyNm().trim().equals("")) {
            addFieldError("ec2Key.keyNm", REQUIRED);
        }
        if (ec2Key.getPrivateKey() == null ||
                ec2Key.getPrivateKey().trim().equals("")) {
            addFieldError("ec2Key.privateKey", REQUIRED);
        }
        if (hasErrors()) {

            sortedSet = EC2KeyDB.getEC2KeySet(sortedSet);
        }
    }


    /**
     * Validates fields for credential submit
     */
    public void validateSubmitEC2Key() {
        if (ec2Key.getEc2Region() == null ||
                ec2Key.getEc2Region().trim().equals("")) {
            addFieldError("ec2Key.ec2Region", REQUIRED);
        }
        if (ec2Key.getKeyNm() == null ||
                ec2Key.getKeyNm().trim().equals("")) {
            addFieldError("ec2Key.keyNm", REQUIRED);
        }
        if (hasErrors()) {

            sortedSet = EC2KeyDB.getEC2KeySet(sortedSet);
        }

    }


}
