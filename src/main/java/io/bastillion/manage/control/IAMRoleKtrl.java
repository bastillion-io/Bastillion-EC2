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
