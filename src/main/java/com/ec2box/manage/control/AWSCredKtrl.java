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
package com.ec2box.manage.control;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.ec2box.manage.db.AWSCredDB;
import com.ec2box.manage.model.AWSCred;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.AWSClientConfig;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action to set aws credentials
 */
public class AWSCredKtrl extends BaseKontroller {

    @Model(name = "awsCred")
    AWSCred awsCred;
    @Model(name = "sortedSet")
    SortedSet sortedSet= new SortedSet();



    public AWSCredKtrl (HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/viewAWSCred", method = MethodType.GET)
    public String viewAWSCred() {
        sortedSet = AWSCredDB.getAWSCredSet(sortedSet);
        return "/manage/view_aws_cred.html";

    }

    @Kontrol(path = "/manage/saveAWSCred", method = MethodType.POST)
    public String saveAWSCred() {
        AWSCredDB.saveAWSCred(awsCred);
        return "redirect:/manage/viewAWSCred.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }


    @Kontrol(path = "/manage/deleteAWSCred", method = MethodType.GET)
    public String deleteAWSCred() {
        AWSCredDB.deleteAWSCred(awsCred.getId());
        return "redirect:/manage/viewAWSCred.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }

    /**
     * Validates fields for credential submit
     */
    @Validate(input = "/manage/view_aws_cred.html")
    public void validateSaveAWSCred() {
        if (awsCred.getAccessKey() == null ||
                awsCred.getAccessKey().trim().equals("")) {
            addFieldError("awsCred.accessKey", "Required");
        }
        if (awsCred.getSecretKey() == null ||
                awsCred.getSecretKey().trim().equals("")) {
            addFieldError("awsCred.secretKey", "Required");
        }
        if (!this.hasErrors()) {
            try {
                //check if credential are valid
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());
                AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                        .withRegion(Regions.DEFAULT_REGION)
                        .withClientConfiguration(AWSClientConfig.getClientConfig()).build();

                service.describeKeyPairs();
            } catch (Exception ex) {
                ex.printStackTrace();
                addError("Invalid Credentials");
            }
        }
        if(this.hasErrors()){
            sortedSet = AWSCredDB.getAWSCredSet(sortedSet);
        }
    }


}
