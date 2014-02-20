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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.ec2box.common.util.AuthUtil;
import com.ec2box.manage.db.*;
import com.ec2box.manage.model.*;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to manage systems
 */
public class SystemAction extends ActionSupport implements ServletRequestAware {

    SortedSet sortedSet = new SortedSet();
    HostSystem hostSystem = new HostSystem();
    Script script = null;
    HttpServletRequest servletRequest;
    List<String> instanceIdList = new ArrayList<String>();

    @Action(value = "/admin/viewSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp")
            }
    )
    public String viewSystems() {

        Long userId = AuthUtil.getUserId(servletRequest.getSession());


        List<String> ec2RegionList = EC2KeyDB.getEC2Regions();


        try {
            //get AWS credentials from DB
            for (AWSCred awsCred : AWSCredDB.getAWSCredList()) {

                if (awsCred != null) {
                    //set  AWS credentials for service
                    BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());


                    for (String ec2Region : ec2RegionList) {
                        //create service

                        AmazonEC2 service = new AmazonEC2Client(awsCredentials);
                        service.setEndpoint(ec2Region);


                        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
                        //only return systems that have keys set
                        for (EC2Key ec2Key : EC2KeyDB.getEC2KeyByRegion(ec2Region, awsCred.getId())) {

                            List<String> valueList= new ArrayList<String>();
                            valueList.add(ec2Key.getKeyNm());

                            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

                            Filter filter = new Filter("key-name", valueList);

                            describeInstancesRequest.withFilters(filter);
                            DescribeInstancesResult describeInstancesResult = service.describeInstances(describeInstancesRequest);


                            for (Reservation res : describeInstancesResult.getReservations()) {
                                for (Instance instance : res.getInstances()) {

                                    HostSystem hostSystem = new HostSystem();
                                    hostSystem.setInstanceId(instance.getInstanceId());
                                    hostSystem.setHost(instance.getPublicDnsName());
                                    hostSystem.setKeyId(ec2Key.getId());
                                    hostSystem.setEc2Region(ec2Region);
                                    hostSystem.setState(instance.getState().getName());
                                    for (Tag tag : instance.getTags()) {
                                        if ("Name".equals(tag.getKey())) {
                                            hostSystem.setDisplayNm(tag.getValue());
                                        }
                                    }

                                    instanceIdList.add(hostSystem.getInstanceId());
                                    hostSystemList.add(hostSystem);
                                }
                            }
                        }

                        //set ec2 systems
                        SystemDB.setSystems(hostSystemList);

                        sortedSet = SystemDB.getSystemSet(sortedSet, instanceIdList);


                    }
                }
            }

        } catch (AmazonServiceException ex) {
            addActionError(ex.getMessage());

        }


        if (script != null && script.getId() != null) {
            script = ScriptDB.getScript(script.getId(), userId);
        }


        return SUCCESS;
    }

    @Action(value = "/admin/saveSystem",
            results = {
                    @Result(name = "input", location = "/admin/view_systems.jsp"),
                    @Result(name = "success", location = "/admin/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}&script.id=${script.id}", type = "redirect")
            }
    )
    public String saveSystem() {

        if (hostSystem.getId() != null && hostSystem.getPort()!=null
           && hostSystem.getUser()!=null && !hostSystem.getUser().trim().equals("")) {
            SystemDB.updateSystem(hostSystem);
        }
        return SUCCESS;
    }




    public HostSystem getHostSystem() {
        return hostSystem;
    }

    public void setHostSystem(HostSystem hostSystem) {
        this.hostSystem = hostSystem;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public List<String> getInstanceIdList() {
        return instanceIdList;
    }

    public void setInstanceIdList(List<String> instanceIdList) {
        this.instanceIdList = instanceIdList;
    }
}
