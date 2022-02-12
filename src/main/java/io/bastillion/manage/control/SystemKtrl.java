/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import io.bastillion.common.util.AppConfig;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.DefaultRegionDB;
import io.bastillion.manage.db.ScriptDB;
import io.bastillion.manage.db.SystemDB;
import io.bastillion.manage.db.UserProfileDB;
import io.bastillion.manage.model.*;
import io.bastillion.manage.model.*;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.AWSClientConfig;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.*;

/**
 * Action to manage systems
 */
public class SystemKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(SystemKtrl.class);

    public static final String FILTER_BY_ALARM_STATE = "alarm_state";
    public static final String FILTER_BY_INSTANCE_STATUS = "instance_status";
    public static final String FILTER_BY_SYSTEM_STATUS = "system_status";
    public static final String FILTER_BY_INSTANCE_STATE = "instance_state";
    public static final String FILTER_BY_SECURITY_GROUP = "security_group";
    public static final String FILTER_BY_TAG = "tag";
    @Model(name = "alarmStateMap")
    static Map<String, String> alarmStateMap = AppConfig.getMapProperties("alarmState");
    @Model(name = "systemStatusMap")
    static Map<String, String> systemStatusMap = AppConfig.getMapProperties("systemStatus");
    @Model(name = "instanceStatusMap")
    static Map<String, String> instanceStatusMap = AppConfig.getMapProperties("instanceStatus");
    @Model(name = "instanceStateMap")
    static Map<String, String> instanceStateMap = AppConfig.getMapProperties("instanceState");
    @Model(name = "regionMap")
    static Map<String, String> regionMap = new LinkedHashMap<>();
    @Model(name = "region")
    static String region;

    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "hostSystem")
    HostSystem hostSystem = new HostSystem();
    @Model(name = "showStatus")
    Boolean showStatus = false;
    @Model(name = "script")
    Script script = new Script();


    static {
        try {
            region = DefaultRegionDB.getRegion();
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
    }
    public SystemKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }


    @Kontrol(path = "/admin/viewSystems", method = MethodType.GET)
    public String viewSystems() throws ServletException {

        try {
            Long userId = AuthUtil.getUserId(getRequest().getSession());
            String userType = AuthUtil.getUserType(getRequest().getSession());

            List<String> instanceIdList = new ArrayList<>();

            //default instance state
            if (sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATE) == null) {
                sortedSet.getFilterMap().put(FILTER_BY_INSTANCE_STATE, AppConfig.getProperty("defaultInstanceState"));
            }
            AmazonEC2 service = AmazonEC2ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(AWSClientConfig.getCredentials()))
                    .withRegion(Regions.DEFAULT_REGION)
                    .withClientConfiguration(AWSClientConfig.getClientConfig())
                    .build();

            DescribeRegionsResult regionResponse = service.describeRegions();

            //get regions with application key set
            for (Region region : regionResponse.getRegions()) {
                regionMap.put(region.getRegionName(), region.getRegionName().toUpperCase());
            }

            Map<String, HostSystem> hostSystemList = new HashMap<>();

            //if user profile has been set or user is a manager
            List<Profile> profileList = UserProfileDB.getProfilesByUser(userId);
            if (!profileList.isEmpty() || Auth.MANAGER.equals(userType)) {
                //set tags for profile
                List<String> profileTags = new ArrayList<>();
                for (Profile profile : profileList) {
                    profileTags.add(profile.getTag());
                }
                Map<String, List<String>> profileTagMap = parseTags(profileTags);

                //set tags from input filters
                Map<String, List<String>> filterTags = fetchInputFilterTags(userType, profileTagMap);

                //parse out security group list in format group[,group]
                List<String> securityGroupList = new ArrayList<>();
                if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_SECURITY_GROUP))) {
                    securityGroupList = Arrays.asList(sortedSet.getFilterMap().get(FILTER_BY_SECURITY_GROUP).split(","));
                }

                // show selected region
                if (region != null && !region.trim().equals("")) {
                    DefaultRegionDB.saveRegion(region);
                    //create service
                    service = AmazonEC2ClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(AWSClientConfig.getCredentials()))
                            .withClientConfiguration(AWSClientConfig.getClientConfig())
                            .withRegion(Regions.fromName(region))
                            .build();


                    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

                    //instance state filter
                    if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATE))) {
                        List<String> instanceStateList = new ArrayList<>();
                        instanceStateList.add(sortedSet.getFilterMap().get(FILTER_BY_INSTANCE_STATE));
                        Filter instanceStateFilter = new Filter("instance-state-name", instanceStateList);
                        describeInstancesRequest.withFilters(instanceStateFilter);
                    }

                    if (!securityGroupList.isEmpty()) {
                        Filter groupFilter = new Filter("group-name", securityGroupList);
                        describeInstancesRequest.withFilters(groupFilter);
                    }
                    //set name value pair for tag filter
                    List<String> tagList = new ArrayList<String>();

                    //add profile tags to filter list if not manager
                    if (!Auth.MANAGER.equals(userType)) {
                        addTagsToDescribeInstanceRequest(profileTagMap, describeInstancesRequest, tagList);
                    }

                    //add all additional filter tags provided by the user
                    addTagsToDescribeInstanceRequest(filterTags, describeInstancesRequest, tagList);

                    if (!tagList.isEmpty()) {
                        Filter tagFilter = new Filter("tag-key", tagList);
                        describeInstancesRequest.withFilters(tagFilter);
                    }

                    DescribeInstancesResult describeInstancesResult = service.describeInstances(describeInstancesRequest);

                    for (Reservation res : describeInstancesResult.getReservations()) {
                        for (Instance instance : res.getInstances()) {

                            HostSystem hostSystem = new HostSystem();
                            hostSystem.setInstance(instance.getInstanceId());

                            // (optionally) use private_ip configured, otherwise
                            // check for public dns if doesn't exist set to ip or pvt dns
                            if ("true".equals(AppConfig.getProperty("useEC2PvtIP")) && StringUtils.isNotEmpty(instance.getPrivateIpAddress())) {
                                hostSystem.setHost(instance.getPrivateIpAddress());
                            } else if ("true".equals(AppConfig.getProperty("useEC2PvtDNS")) && StringUtils.isNotEmpty(instance.getPrivateDnsName())) {
                                hostSystem.setHost(instance.getPrivateDnsName());
                            } else if (StringUtils.isNotEmpty(instance.getPublicDnsName())) {
                                hostSystem.setHost(instance.getPublicDnsName());
                            } else if (StringUtils.isNotEmpty(instance.getPublicIpAddress())) {
                                hostSystem.setHost(instance.getPublicIpAddress());
                            } else {
                                hostSystem.setHost(instance.getPrivateIpAddress());
                            }

                            hostSystem.setEc2Region(region);
                            hostSystem.setState(instance.getState().getName());
                            for (Tag tag : instance.getTags()) {
                                if ("Name".equals(tag.getKey())) {
                                    hostSystem.setDisplayNm(tag.getValue());
                                } else if (AppConfig.getProperty("userTagName").equals(tag.getKey())) {
                                    hostSystem.setUser(tag.getValue());
                                }
                            }
                            //if no display name set to host
                            if (StringUtils.isEmpty(hostSystem.getDisplayNm())) {
                                hostSystem.setDisplayNm(hostSystem.getHost());
                            }
                            instanceIdList.add(hostSystem.getInstance());
                            hostSystemList.put(hostSystem.getInstance(), hostSystem);
                        }
                    }

                    if (instanceIdList.size() > 0) {
                        //set instance id list to check permissions when creating sessions
                        getRequest().getSession().setAttribute("instanceIdList", new ArrayList<>(instanceIdList));
                        if (showStatus) {
                            //make service call 100 instances at a time b/c of AWS limitation
                            int i = 0;
                            List<String> idCallList = new ArrayList<>();
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
                            AmazonCloudWatch cloudWatchClient = AmazonCloudWatchClientBuilder.standard()
                                    .withCredentials(new AWSStaticCredentialsProvider(AWSClientConfig.getCredentials()))
                                    .withRegion(Regions.fromName(region))
                                    .withClientConfiguration(AWSClientConfig.getClientConfig()).build();
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
                                            if (StringUtils.isEmpty(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE))
                                                    || "ALARM".equals(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE)) && hostSystem.getMonitorAlarm() > 0
                                                    || ("INSUFFICIENT_DATA".equals(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE)) && hostSystem.getMonitorInsufficientData() > 0)
                                                    || ("OK".equals(sortedSet.getFilterMap().get(FILTER_BY_ALARM_STATE)) && hostSystem.getMonitorOk() > 0 && hostSystem.getMonitorInsufficientData() <= 0 && hostSystem.getMonitorAlarm() <= 0)) {
                                                hostSystemList.put(hostSystem.getInstance(), hostSystem);
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
                sortedSet = SystemDB.getSystemSet(sortedSet, new ArrayList<>(hostSystemList.keySet()));

            }

            if (script != null && script.getId() != null) {
                script = ScriptDB.getScript(script.getId(), userId);
            }
        } catch (AWSSecurityTokenServiceException ex) {
            log.error(ex.toString(), ex);
            return "redirect:/manage/viewIAMRole.ktrl";
        } catch(SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return "/admin/view_systems.html";
    }

    private Map<String, List<String>> fetchInputFilterTags(String userType, Map<String, List<String>> profileTagMap) {
        Map<String, List<String>> filterTags = new HashMap<>();
        if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_TAG))) {
            //if manager allow any filter
            if (Auth.MANAGER.equals(userType)) {
                filterTags.putAll(parseTags(Arrays.asList(sortedSet.getFilterMap().get(FILTER_BY_TAG))));
                //check against profile
            } else {
                Map<String, List<String>> tmpMap = parseTags(Arrays.asList(sortedSet.getFilterMap().get(FILTER_BY_TAG)));
                for (Map.Entry<String, List<String>> entry : tmpMap.entrySet()) {
                    String name = entry.getKey();
                    //if profile tags does not have the filtered tag add to filters and it would be ANDed with profile tags.
                    if (profileTagMap.get(name) == null && entry.getValue() != null) {
                        filterTags.put(name, entry.getValue());
                    }

                    //if profile tags have the filtered tag add to filters only if values are contained in allowed list of the user.
                    if (profileTagMap.get(name) != null && entry.getValue() != null && profileTagMap.get(name).containsAll(entry.getValue())) {
                        filterTags.put(name, entry.getValue());
                    }
                }
            }
        }
        return filterTags;
    }

    private void addTagsToDescribeInstanceRequest(Map<String, List<String>> profileTagMap, DescribeInstancesRequest describeInstancesRequest, List<String> tagList) {
        for (String tag : profileTagMap.keySet()) {
            if (profileTagMap.get(tag) != null) {
                Filter tagValueFilter = new Filter("tag:" + tag, profileTagMap.get(tag));
                describeInstancesRequest.withFilters(tagValueFilter);
            } else {
                tagList.add(tag);
            }
        }
    }

    @Kontrol(path = "/admin/saveSystem", method = MethodType.POST)
    public String saveSystem() throws ServletException {

        String retVal = "redirect:/admin/viewSystems.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
        if (hostSystem.getId() != null && hostSystem.getPort() != null
                && hostSystem.getUser() != null && !hostSystem.getUser().trim().equals("")) {
            try {
                SystemDB.updateSystem(hostSystem);
            } catch (SQLException | GeneralSecurityException ex) {
                log.error(ex.toString(), ex);
                throw new ServletException(ex.toString(), ex);
            }

        }
        if (script != null && script.getId() != null) {
            retVal = retVal + "&script.id=" + script.getId();
        }
        return retVal;

    }

    /**
     * Parse out tags in format tag-name[=value[,tag-name[=value]]
     *
     * @param tags list of unparsed tags
     * @return map of tags
     */
    private Map<String, List<String>> parseTags(List<String> tags) {
        Map<String, List<String>> tagMap = new HashMap<>();
        for (String tag : tags) {
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


}
