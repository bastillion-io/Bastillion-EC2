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

import com.ec2box.common.util.AppConfigLkup;
import com.ec2box.manage.db.EC2KeyDB;
import com.ec2box.manage.db.EC2RegionDB;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.AdminUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


public class EC2RegionAction extends ActionSupport  implements ServletRequestAware {


    Map ec2RegionMap= AppConfigLkup.getMapProperties("ec2Regions");
    List<String> ec2Region;
    HttpServletRequest servletRequest;

    @Action(value = "/manage/setEC2Region",
            results = {
                    @Result(name = "success", location = "/manage/set_ec2_region.jsp")
            }
    )
    public String setEC2Region() {

        Long adminId=AdminUtil.getAdminId(servletRequest);
        SortedSet sortedSet=EC2KeyDB.getEC2KeySet(adminId, new SortedSet());
        //check to see if keys have been imported
        if(sortedSet!=null && sortedSet.getItemList()!=null && sortedSet.getItemList().size()>0){
            ec2Region= EC2RegionDB.getEC2Regions(adminId);
        }else{
            addActionMessage("EC2 regions not available. Import EC2 Keys <a href=\"viewEC2Keys.action\">here</a>");
        }

        return SUCCESS;

    }

    @Action(value = "/manage/submitEC2Region",
            results = {
                    @Result(name = "success", location = "/manage/viewSystems.action", type = "redirect")
            }
    )
    public String submitEC2Region() {

        EC2RegionDB.setRegion(AdminUtil.getAdminId(servletRequest),ec2Region);
        return SUCCESS;

    }

    public Map getEc2RegionMap() {
        return ec2RegionMap;
    }

    public void setEc2RegionMap(Map ec2RegionMap) {
        this.ec2RegionMap = ec2RegionMap;
    }

    public List<String> getEc2Region() {
        return ec2Region;
    }

    public void setEc2Region(List<String> ec2Region) {
        this.ec2Region = ec2Region;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
