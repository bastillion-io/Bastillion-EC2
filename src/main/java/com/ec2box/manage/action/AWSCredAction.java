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
package com.ec2box.manage.action;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.ec2box.manage.db.AWSCredDB;
import com.ec2box.manage.model.AWSCred;
import com.ec2box.manage.util.AdminUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;


public class AWSCredAction extends ActionSupport implements ServletRequestAware {

    HttpServletRequest servletRequest;
    AWSCred awsCred;


    @Action(value = "/manage/setAWSCred",
            results = {
                    @Result(name = "success", location = "/manage/set_aws_cred.jsp")
            }
    )
    public String setAWSCred() {
        awsCred = AWSCredDB.getAWSCred(AdminUtil.getAdminId(servletRequest));
        return SUCCESS;

    }

    @Action(value = "/manage/submitAWSCred",
            results = {
                    @Result(name = "success", location = "/manage/viewEC2Keys.action", type = "redirect"),
                    @Result(name = "input", location = "/manage/set_aws_cred.jsp")
            }
    )

    public String submitAWSCred() {
        AWSCredDB.setAWSCred(AdminUtil.getAdminId(servletRequest), awsCred);
        return SUCCESS;

    }


    /**
     * Validates fields for credential submit
     */
    public void validateSubmitAWSCred() {
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
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());
                AmazonEC2 service = new AmazonEC2Client(awsCredentials);
                service.describeKeyPairs();
            } catch (Exception ex) {
                addActionError("Invalid Credentials");
            }
        }
    }


    public AWSCred getAwsCred() {
        return awsCred;
    }

    public void setAwsCred(AWSCred awsCred) {
        this.awsCred = awsCred;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
