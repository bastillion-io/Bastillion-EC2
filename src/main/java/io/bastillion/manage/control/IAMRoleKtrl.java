/**
 * Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import io.bastillion.manage.db.IAMRoleDB;
import io.bastillion.manage.db.PrivateKeyDB;
import io.bastillion.manage.util.AWSClientConfig;
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
public class IAMRoleKtrl extends BaseKontroller {

    @Model(name = "publicKey")
    static String publicKey = PrivateKeyDB.getApplicationKey().getPublicKey();
    @Model(name = "arn")
    String arn;

    public IAMRoleKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/viewIAMRole", method = MethodType.GET)
    public String viewIAMRole() {
        arn = IAMRoleDB.getIAMRole();
        return "/manage/set_iam_role.html";

    }

    @Kontrol(path = "/manage/saveIAMRole", method = MethodType.POST)
    public String saveIAMRole() {
        IAMRoleDB.saveIAMRole(arn);
        getRequest().setAttribute("success", true);
        return "/manage/set_iam_role.html";
    }


    @Validate(input = "/manage/set_iam_role.html")
    public void validateSaveIAMRole() {
        if (arn == null ||
                arn.trim().equals("")) {
            addFieldError("arn", "Required");
        }
        if (!this.hasErrors()) {
            try {
                //check if credential are valid
                AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(AWSClientConfig.getCredentials(arn)))
                        .withRegion(Regions.DEFAULT_REGION)
                        .withClientConfiguration(AWSClientConfig.getClientConfig()).build();

               service.describeKeyPairs();

            } catch (Exception ex) {
                ex.printStackTrace();
                addError("Amazon Resource Name configuration failed");
            }
        }
    }


}
