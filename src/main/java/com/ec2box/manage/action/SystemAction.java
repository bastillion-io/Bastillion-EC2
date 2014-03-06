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
import com.ec2box.common.util.AppConfigLkup;
import com.ec2box.common.util.AuthUtil;
import com.ec2box.manage.db.*;
import com.ec2box.manage.model.*;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.AWSClientConfig;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Action to manage systems
 */
public class SystemAction extends ActionSupport implements ServletRequestAware {

    SortedSet sortedSet = new SortedSet();
    HostSystem hostSystem = new HostSystem();
    Script script = null;
    HttpServletRequest servletRequest;
    String tag = null;
    String securityGroup = null;

    @Action(value = "/admin/viewSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp")
            }
    )
    public String viewSystems() {

        Long userId = AuthUtil.getUserId(servletRequest.getSession());


        List<String> ec2RegionList = EC2KeyDB.getEC2Regions();


        try {
            Map<String, HostSystem> hostSystemList = new HashMap<String, HostSystem>();


            Map<String, String> tagMap = new HashMap<>();
            List<String> tagList = new ArrayList<>();

            //parse out tags in format tag-name[=value[,tag-name[=value]]
            if (StringUtils.isNotEmpty(tag)) {
                String[] tagArr1 = tag.split(",");
                if (tagArr1.length > 0) {
                    for (String tag1 : tagArr1) {
                        String[] tagArr2 = tag1.split("=");
                        if (tagArr2.length > 1) {
                            tagMap.put(tag1.split("=")[0], tag1.split("=")[1]);
                        } else {
                            tagList.add(tag1);
                        }
                    }
                }

            }

            //parse out security group list in format group[,group]
            List<String> securityGroupList = new ArrayList<>();
            if (StringUtils.isNotEmpty(securityGroup)) {
                securityGroupList = Arrays.asList(securityGroup.split(","));
            }


            //get AWS credentials from DB
            for (AWSCred awsCred : AWSCredDB.getAWSCredList()) {

                if (awsCred != null) {
                    //set  AWS credentials for service
                    BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());

                    for (String ec2Region : ec2RegionList) {
                        //create service

                        AmazonEC2 service = new AmazonEC2Client(awsCredentials, AWSClientConfig.getClientConfig());
                        service.setEndpoint(ec2Region);


                        //only return systems that have keys set
                        List<String> keyValueList = new ArrayList<String>();
                        for (EC2Key ec2Key : EC2KeyDB.getEC2KeyByRegion(ec2Region, awsCred.getId())) {
                            keyValueList.add(ec2Key.getKeyNm());
                        }

                        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

                        Filter keyNmFilter = new Filter("key-name", keyValueList);
                        describeInstancesRequest.withFilters(keyNmFilter);

                        if (tagList.size() > 0) {
                            Filter tagFilter = new Filter("tag-key", tagList);
                            describeInstancesRequest.withFilters(tagFilter);
                        }

                        if (securityGroupList.size() > 0) {
                            Filter groupFilter = new Filter("group-name", securityGroupList);
                            describeInstancesRequest.withFilters(groupFilter);
                        }
                        //set name value pair for tag filter
                        for (String tag : tagMap.keySet()) {
                            Filter tagValueFilter = new Filter("tag:" + tag, Arrays.asList(tagMap.get(tag)));
                            describeInstancesRequest.withFilters(tagValueFilter);
                        }


                        DescribeInstancesResult describeInstancesResult = service.describeInstances(describeInstancesRequest);


                        List<String> instanceIdList = new ArrayList<String>();
                        for (Reservation res : describeInstancesResult.getReservations()) {
                            for (Instance instance : res.getInstances()) {


                                HostSystem hostSystem = new HostSystem();
                                hostSystem.setInstanceId(instance.getInstanceId());

                                //check for public dns if doesn't exist set to ip or pvt dns
                                if (!"true".equals(AppConfigLkup.getProperty("useEC2PvtDNS")) && StringUtils.isNotEmpty(instance.getPublicDnsName())) {
                                    hostSystem.setHost(instance.getPublicDnsName());
                                } else if (StringUtils.isNotEmpty(instance.getPrivateDnsName())) {
                                    hostSystem.setHost(instance.getPrivateDnsName());
                                } else {
                                    for (InstanceNetworkInterface networkInterface : instance.getNetworkInterfaces()) {
                                        hostSystem.setHost(networkInterface.getPrivateDnsName());
                                    }
                                }


                                hostSystem.setKeyId(EC2KeyDB.getEC2KeyByNmRegion(instance.getKeyName(), ec2Region, awsCred.getId()).getId());
                                hostSystem.setEc2Region(ec2Region);
                                hostSystem.setState(instance.getState().getName());
                                for (Tag tag : instance.getTags()) {
                                    if ("Name".equals(tag.getKey())) {
                                        hostSystem.setDisplayNm(tag.getValue());
                                    }
                                }
                                instanceIdList.add(hostSystem.getInstanceId());
                                hostSystemList.put(hostSystem.getInstanceId(), hostSystem);
                            }
                        }

                        if (instanceIdList.size() > 0) {
                            //get status for host systems
                            DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
                            describeInstanceStatusRequest.withInstanceIds(instanceIdList);
                            DescribeInstanceStatusResult describeInstanceStatusResult = service.describeInstanceStatus(describeInstanceStatusRequest);

                            for (InstanceStatus instanceStatus : describeInstanceStatusResult.getInstanceStatuses()) {

                                HostSystem hostSystem = hostSystemList.remove(instanceStatus.getInstanceId());
                                hostSystem.setSystemStatus(instanceStatus.getSystemStatus().getStatus());
                                hostSystem.setInstanceStatus(instanceStatus.getInstanceStatus().getStatus());
                                hostSystemList.put(hostSystem.getInstanceId(), hostSystem);
                            }

                        }
                    }

                }
            }

            //set ec2 systems
            SystemDB.setSystems(hostSystemList.values());
            sortedSet = SystemDB.getSystemSet(sortedSet, new ArrayList<String>(hostSystemList.keySet()));

        } catch (AmazonServiceException ex) {
            ex.printStackTrace();
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

        if (hostSystem.getId() != null && hostSystem.getPort() != null
                && hostSystem.getUser() != null && !hostSystem.getUser().trim().equals("")) {
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(String securityGroup) {
        this.securityGroup = securityGroup;
    }
}
