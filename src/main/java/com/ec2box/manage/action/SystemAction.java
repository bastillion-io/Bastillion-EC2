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
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.ec2box.common.util.AppConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Action to manage systems
 */
public class SystemAction extends ActionSupport implements ServletRequestAware {

    private static Logger log = LoggerFactory.getLogger(SystemAction.class);

    SortedSet sortedSet = new SortedSet();
    HostSystem hostSystem = new HostSystem();
    boolean showStatus=false;
    Script script = null;
    HttpServletRequest servletRequest;
    static Map<String, String> alarmStateMap = AppConfig.getMapProperties("alarmState");
    static Map<String, String> systemStatusMap = AppConfig.getMapProperties("systemStatus");
    static Map<String, String> instanceStatusMap = AppConfig.getMapProperties("instanceStatus");
    static Map<String, String> instanceStateMap = AppConfig.getMapProperties("instanceState");
    
    public static final String FILTER_BY_ALARM_STATE = "alarm_state";
    public static final String FILTER_BY_INSTANCE_STATUS= "instance_status";
    public static final String FILTER_BY_SYSTEM_STATUS= "system_status";
    public static final String FILTER_BY_INSTANCE_STATE= "instance_state";
    public static final String FILTER_BY_SECURITY_GROUP= "security_group";
    public static final String FILTER_BY_TAG= "tag";
    

    @Action(value = "/admin/viewSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp")
            }
    )
    public String viewSystems() {

        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        String userType = AuthUtil.getUserType(servletRequest.getSession());

        List<String> ec2RegionList = EC2KeyDB.getEC2Regions();
        List<String> instanceIdList = new ArrayList<String>();
        
        //default instance state
        if(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATE) == null){
            sortedSet.getFilterMap().put(FILTER_BY_INSTANCE_STATE,AppConfig.getProperty("defaultInstanceState"));  
        }

        try {
            Map<String, HostSystem> hostSystemList = new HashMap<String, HostSystem>();

            //if user profile has been set or user is a manager
            List<Profile> profileList = UserProfileDB.getProfilesByUser(userId);
            if (profileList.size() > 0 || Auth.MANAGER.equals(userType)) {
                //set tags for profile
                List<String> profileTags = new ArrayList<>();
                for (Profile profile : profileList) {
                    profileTags.add(profile.getTag());
                }
                Map<String,List<String>> profileTagMap = parseTags(profileTags);

                //set tag from input filter
                Map<String,List<String>> tagMap = profileTagMap;
                if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_TAG))) {
                    //if manager allow any filter
                    Map<String,List<String>> filterTags = new HashMap<>();
                    if(Auth.MANAGER.equals(userType)) {
                        filterTags.putAll(parseTags(Arrays.asList(sortedSet.getFilterMap().get(FILTER_BY_TAG))));
                    //check against profile
                    } else {
                        Map<String,List<String>> tmpMap = parseTags(Arrays.asList(sortedSet.getFilterMap().get(FILTER_BY_TAG)));
                        for(String name : tmpMap.keySet()) {
                            if(profileTagMap.get(name).containsAll(tmpMap.get(name))) {
                                filterTags.put(name,tmpMap.get(name));
                            }
                        }
                    }
                    if(filterTags.size() > 0){
                        tagMap = filterTags;
                    }
                }

                //parse out security group list in format group[,group]
                List<String> securityGroupList = new ArrayList<>();
                if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_SECURITY_GROUP))) {
                    securityGroupList = Arrays.asList(sortedSet.getFilterMap().get(FILTER_BY_SECURITY_GROUP).split(","));
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

                            //instance state filter
                            if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATE))) {
                                List<String> instanceStateList = new ArrayList<String>();
                                instanceStateList.add(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATE));
                                Filter instanceStateFilter = new Filter("instance-state-name", instanceStateList);
                                describeInstancesRequest.withFilters(instanceStateFilter);
                            }

                            if (securityGroupList.size() > 0) {
                                Filter groupFilter = new Filter("group-name", securityGroupList);
                                describeInstancesRequest.withFilters(groupFilter);
                            }
                            //set name value pair for tag filter
                            List<String> tagList = new ArrayList<String>();
                            for (String tag : tagMap.keySet()) {
                                if(tagMap.get(tag) != null) {
                                    Filter tagValueFilter = new Filter("tag:" + tag, tagMap.get(tag));
                                    describeInstancesRequest.withFilters(tagValueFilter);
                                } else {
                                    tagList.add(tag);
                                }
                            }
                            if (tagList.size() > 0) {
                                Filter tagFilter = new Filter("tag-key", tagList);
                                describeInstancesRequest.withFilters(tagFilter);
                            }

                            DescribeInstancesResult describeInstancesResult = service.describeInstances(describeInstancesRequest);

                            for (Reservation res : describeInstancesResult.getReservations()) {
                                for (Instance instance : res.getInstances()) {


                                    HostSystem hostSystem = new HostSystem();
                                    hostSystem.setInstance(instance.getInstanceId());

                                    //check for public dns if doesn't exist set to ip or pvt dns
                                    if (!"true".equals(AppConfig.getProperty("useEC2PvtDNS")) && StringUtils.isNotEmpty(instance.getPublicDnsName())) {
                                        hostSystem.setHost(instance.getPublicDnsName());
                                    } else if (!"true".equals(AppConfig.getProperty("useEC2PvtDNS")) && StringUtils.isNotEmpty(instance.getPublicIpAddress())) {
                                        hostSystem.setHost(instance.getPublicIpAddress());
                                    } else if (StringUtils.isNotEmpty(instance.getPrivateDnsName())) {
                                        hostSystem.setHost(instance.getPrivateDnsName());
                                    } else {
                                        hostSystem.setHost(instance.getPrivateIpAddress());
                                    }


                                    hostSystem.setKeyId(EC2KeyDB.getEC2KeyByNmRegion(instance.getKeyName(), ec2Region, awsCred.getId()).getId());
                                    hostSystem.setEc2Region(ec2Region);
                                    hostSystem.setState(instance.getState().getName());
                                    for (Tag tag : instance.getTags()) {
                                        if ("Name".equals(tag.getKey())) {
                                            hostSystem.setDisplayNm(tag.getValue());
                                        }
                                    }
                                    instanceIdList.add(hostSystem.getInstance());
                                    hostSystemList.put(hostSystem.getInstance(), hostSystem);
                                }
                            }

                            if (instanceIdList.size() > 0) {
                                //set instance id list to check permissions when creating sessions
                                servletRequest.getSession().setAttribute("instanceIdList", new ArrayList<String>(instanceIdList));
                                if(showStatus) {
                                    //make service call 100 instances at a time b/c of AWS limitation
                                    int i = 0;
                                    List<String> idCallList = new ArrayList<String>();
                                    while (!instanceIdList.isEmpty()) {
                                        idCallList.add(instanceIdList.remove(0));
                                        i++;
                                        //when i eq 100 make call
                                        if (i >= 100 || instanceIdList.isEmpty()) {

                                            //get status for host systems
                                            DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
                                            describeInstanceStatusRequest.withInstanceIds(idCallList);
                                            DescribeInstanceStatusResult describeInstanceStatusResult = service.describeInstanceStatus(describeInstanceStatusRequest);

                                            for (InstanceStatus instanceStatus : describeInstanceStatusResult.getInstanceStatuses()) {

                                                HostSystem hostSystem = hostSystemList.remove(instanceStatus.getInstanceId());
                                                hostSystem.setSystemStatus(instanceStatus.getSystemStatus().getStatus());
                                                hostSystem.setInstanceStatus(instanceStatus.getInstanceStatus().getStatus());

                                                //check and filter by instance or system status
                                                if ((StringUtils.isEmpty(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATUS)) && StringUtils.isEmpty(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM_STATUS)))
                                                        || (hostSystem.getInstanceStatus().equals(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATUS)) && StringUtils.isEmpty(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM_STATUS)))
                                                        || (hostSystem.getInstanceStatus().equals(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM_STATUS)) && StringUtils.isEmpty(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATUS)))
                                                        || (hostSystem.getInstanceStatus().equals(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM_STATUS)) && hostSystem.getInstanceStatus().equals(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATUS)))
                                                        ) {
                                                    hostSystemList.put(hostSystem.getInstance(), hostSystem);
                                                }
                                            }

                                            //start over
                                            i = 0;
                                            //clear list
                                            idCallList.clear();
                                        }

                                    }


                                    //check alarms for ec2 instances
                                    AmazonCloudWatchClient cloudWatchClient = new AmazonCloudWatchClient(awsCredentials, AWSClientConfig.getClientConfig());
                                    cloudWatchClient.setEndpoint(ec2Region.replace("ec2", "monitoring"));


                                    DescribeAlarmsResult describeAlarmsResult = cloudWatchClient.describeAlarms();

                                    for (MetricAlarm metricAlarm : describeAlarmsResult.getMetricAlarms()) {

                                        for (Dimension dim : metricAlarm.getDimensions()) {

                                            if (dim.getName().equals("InstanceId")) {
                                                HostSystem hostSystem = hostSystemList.remove(dim.getValue());
                                                if (hostSystem != null) {
                                                    if ("ALARM".equals(metricAlarm.getStateValue())) {
                                                        hostSystem.setMonitorAlarm(hostSystem.getMonitorAlarm() + 1);
                                                    } else if ("INSUFFICIENT_DATA".equals(metricAlarm.getStateValue())) {
                                                        hostSystem.setMonitorInsufficientData(hostSystem.getMonitorInsufficientData() + 1);
                                                    } else {
                                                        hostSystem.setMonitorOk(hostSystem.getMonitorOk() + 1);
                                                    }
                                                    //check and filter by alarm state
                                                    if (StringUtils.isEmpty(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE))) {
                                                        hostSystemList.put(hostSystem.getInstance(), hostSystem);
                                                    } else if ("ALARM".equals(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE)) && hostSystem.getMonitorAlarm() > 0) {
                                                        hostSystemList.put(hostSystem.getInstance(), hostSystem);
                                                    } else if ("INSUFFICIENT_DATA".equals(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE)) && hostSystem.getMonitorInsufficientData() > 0) {
                                                        hostSystemList.put(hostSystem.getInstance(), hostSystem);
                                                    } else if ("OK".equals(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE)) && hostSystem.getMonitorOk() > 0 && hostSystem.getMonitorInsufficientData() <= 0 && hostSystem.getMonitorAlarm() <= 0) {
                                                        hostSystemList.put(hostSystem.getInstance(), hostSystem);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //set ec2 systems
                SystemDB.setSystems(hostSystemList.values());
                sortedSet = SystemDB.getSystemSet(sortedSet, new ArrayList<String>(hostSystemList.keySet()));

            }
        } catch (
                AmazonServiceException ex
                )

        {
            log.error(ex.toString(), ex);
        }


        if (script != null && script.getId() != null)

        {
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

	/**
     *  Parse out tags in format tag-name[=value[,tag-name[=value]]
     *
     * @param tags list of unparsed tags
     * @return map of tags
     */
    private Map<String,List<String>> parseTags(List<String> tags) {
        Map<String,List<String>> tagMap = new HashMap<>();
        for(String tag : tags) {
            String[] tagArr1 = tag.split(",");
            if (tagArr1.length > 0) {
                for (String tag1 : tagArr1) {
                    String[] tagArr2 = tag1.split("=");
                    if (tagArr2.length > 1) {
                        String tagNm = tag1.split("=")[0];
                        String tagVal = tag1.split("=")[1];
                        if (tagMap.get(tagNm) != null && tagMap.get(tagNm).size() > 0) {
                            tagMap.get(tagNm).add(tagVal);
                        } else {
                            tagMap.put(tagNm, new LinkedList<String>());
                            tagMap.get(tagNm).add(tagVal);
                        }
                    } else {
                        tagMap.put(tag1, null);
                    }
                }
            }
        }
        return tagMap;
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

    public static Map<String, String> getAlarmStateMap() {
        return alarmStateMap;
    }

    public static void setAlarmStateMap(Map<String, String> alarmStateMap) {
        SystemAction.alarmStateMap = alarmStateMap;
    }

    public static Map<String, String> getSystemStatusMap() {
        return systemStatusMap;
    }

    public static void setSystemStatusMap(Map<String, String> systemStatusMap) {
        SystemAction.systemStatusMap = systemStatusMap;
    }

    public static Map<String, String> getInstanceStatusMap() {
        return instanceStatusMap;
    }

    public static void setInstanceStatusMap(Map<String, String> instanceStatusMap) {
        SystemAction.instanceStatusMap = instanceStatusMap;
    }


    public static Map<String, String> getInstanceStateMap() {
        return instanceStateMap;
    }

    public static void setInstanceStateMap(Map<String, String> instanceStateMap) {
        SystemAction.instanceStateMap = instanceStateMap;
    }

    public boolean isShowStatus() {
        return showStatus;
    }

    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }
}
