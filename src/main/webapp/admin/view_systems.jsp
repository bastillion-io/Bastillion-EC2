<%
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
%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="../_res/inc/header.jsp"/>
    <script type="text/javascript">
        $(document).ready(function () {

            //open edit dialog
            $(".edit_btn").click(function () {
                //get dialog id to open
                var id = $(this).attr('id').replace("edit_btn_", "");
                $("#edit_dialog_" + id).modal();

            });
            $("#script_btn").click(function () {
                $("#script_dialog").modal();
            });

            //submit edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('.modal').find('form').submit();
            });


            $(".select_frm_btn").button().click(function () {
                $("#select_frm").submit();
            });
            //select all check boxes
            $("#select_frm_systemSelectAll").click(function () {

                if ($(this).is(':checked')) {
                    $(".systemSelect").attr('checked', true);
                } else {
                    $(".systemSelect").attr('checked', false);
                }
            });
            $(".sort,.sortAsc,.sortDesc").click(function () {
                var id = $(this).attr('id');

                if ($('#viewSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'desc');
                } else {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSystems_sortedSet_orderByField').attr('value', id);
                $("#viewSystems").submit();

            });
            <s:if test="sortedSet.orderByField!=null && sortedSet.orderByField!=''">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

        });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function () {
                <s:if test="hostSystem.id>0">
                $("#edit_dialog_<s:property value="hostSystem.id"/>").modal();
                </s:if>
            });
        </script>
    </s:if>

    <title>EC2Box - Manage Instances</title>
</head>
<body>

<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">

    <div class="system_container">


        <s:if test="script!=null && script.id!=null">
            <h3>Execute Script on Instances</h3>
        </s:if>
        <s:else>
            <h3>Composite SSH Terminals</h3>
        </s:else>


        <s:if test="script!=null && script.id!=null">
            <p>Run <b> <a data-toggle="modal" data-target="#script_dialog"><s:property
                    value="script.displayNm"/></a></b> on the selected systems below</p>

            <div id="script_dialog" class="modal fade">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <h4 class="modal-title">View Script: <s:property value="script.displayNm"/></h4>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <pre><s:property value="script.script"/></pre>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary cancel_btn" data-dismiss="modal">Close</button>

                        </div>
                    </div>
                </div>
            </div>

        </s:if>
        <s:else>
            <div>Select the instances below to generate composite SSH sessions in multiple terminals<br/>
                <span class="note">(Select on the user field to change the instance username and other properties)</span>
            </div>
        </s:else>


        <table>

            <tr>
                <td class="align_left"><br/>
                    <s:form action="viewSystems" id="showStatusForm" theme="simple">
                        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:if test="script!=null && script.id!=null">
                            <s:hidden name="script.id"/>
                        </s:if>
                        <s:if test="showStatus">
                            <s:hidden name="showStatus" value="false"/>
                            <s:submit cssClass="btn btn-danger" value="Disable Status"/>
                        </s:if>
                        <s:else>
                            <s:hidden name="showStatus" value="true"/>
                            <s:submit cssClass="btn btn-success" value="Show Status"/>
                        </s:else>
                    </s:form>
                </td>
                <td><br/> |</td>


                <td>
                    <s:form action="viewSystems" theme="simple">
                        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:if test="script!=null && script.id!=null">
                            <s:hidden name="script.id"/>
                        </s:if>
                        <s:hidden name="showStatus"/>
                        <table>
                            <tr>

                                <td>
                                    <label>Tag</label><br/><s:textfield
                                        name="sortedSet.filterMap['%{@com.ec2box.manage.action.SystemAction@FILTER_BY_TAG}']"
                                        placeholder="tag-name[=value[,tag-name[=value]]"
                                        theme="simple" size="25"/></td>

                                <td><label>Security Group</label><br/><s:textfield
                                        name="sortedSet.filterMap['%{@com.ec2box.manage.action.SystemAction@FILTER_BY_SECURITY_GROUP}']"
                                        placeholder="group[,group]"
                                        theme="simple" size="10"/></td>
                                <td>
                                    <label>Current State</label><br/><s:select
                                        name="sortedSet.filterMap['%{@com.ec2box.manage.action.SystemAction@FILTER_BY_INSTANCE_STATE}']"
                                        list="instanceStateMap"
                                        theme="simple" headerKey="" headerValue="-Any-"/>
                                </td>
                                <s:if test="showStatus">
                                    <td>
                                        <label>Instance Status</label><br/><s:select
                                            name="sortedSet.filterMap['%{@com.ec2box.manage.action.SystemAction@FILTER_BY_INSTANCE_STATUS}']"
                                            list="instanceStatusMap"
                                            theme="simple" headerKey="" headerValue="-Any-"/>
                                    </td>
                                    <td>
                                        <label>System Status</label><br/><s:select
                                            name="sortedSet.filterMap['%{@com.ec2box.manage.action.SystemAction@FILTER_BY_SYSTEM_STATUS}']"
                                            list="systemStatusMap" theme="simple"
                                            headerKey="" headerValue="-Any-"/>
                                    </td>
                                    <td>
                                        <label>Alarm State</label><br/><s:select
                                            name="sortedSet.filterMap['%{@com.ec2box.manage.action.SystemAction@FILTER_BY_ALARM_STATE}']"
                                            list="alarmStateMap" theme="simple"
                                            headerKey="" headerValue="-Any-"/>
                                    </td>
                                </s:if>
                                <td style="padding:20px 5px 0px 5px;">
                                    <s:submit cssClass="btn btn-primary" value="Filter"/>
                                </td>
                            </tr>
                        </table>
                    </s:form>
                </td>
            </tr>
        </table>


        <s:if test="(sortedSet.itemList!= null && !sortedSet.itemList.isEmpty())">
            <s:form action="selectSystemsForCompositeTerms" id="select_frm" theme="simple">
                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                <s:if test="script!=null && script.id!=null">
                    <s:hidden name="script.id"/>
                </s:if>
                <s:hidden name="showStatus"/>
                <div class="scrollWrapper">
                    <table class="table-striped scrollableTable" style="min-width:100%;table-layout: auto">
                        <thead>
                        <tr>
                            <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                            theme="simple"/></th>

                            <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">
                                Display
                                Name
                            </th>

                            <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_INSTANCE_ID"/>"
                                class="sort">
                                Instance Id
                            </th>
                            <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User
                            </th>
                            <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host
                            </th>
                            <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_PORT"/>" class="sort">Port
                            </th>
                            <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_STATE"/>" class="sort">
                                State
                            </th>
                            <s:if test="showStatus">
                                <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_INSTANCE_STATUS"/>"
                                    class="sort">I-Status
                                </th>
                                <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_SYSTEM_STATUS"/>"
                                    class="sort">S-Status
                                </th>
                                <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_ALARMS"/>"
                                    class="sort">
                                    Alarms
                                </th>
                            </s:if>
                            <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_REGION"/>" class="sort">
                                Region
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <s:iterator var="system" value="sortedSet.itemList" status="stat">
                            <tr>
                                <td>
                                    <s:if test="state=='running' && host!=''">
                                        <s:checkboxlist name="systemSelectId" list="#{id:''}" cssClass="systemSelect"
                                                        theme="simple"/>
                                    </s:if>
                                </td>

                                <td>
                                    <s:property value="displayNm"/>
                                </td>
                                <td><s:property value="instance"/></td>
                                <td>

                                    <a id="edit_btn_<s:property value="id"/>" title="Update System Properties"
                                       class="edit_btn" href="#">
                                        <s:property value="user"/>
                                    </a>
                                </td>
                                <td><s:property value="host"/></td>
                                <td>
                                    <a id="edit_btn_<s:property value="id"/>" title="Update System Properties"
                                       class="edit_btn" href="#">
                                        <s:property value="port"/>
                                    </a>
                                </td>
                                <td><s:property value="state"/></td>
                                <s:if test="showStatus">
                                    <td><s:property value="instanceStatus"/></td>
                                    <td><s:property value="systemStatus"/></td>
                                    <td><span class="text-success"><s:property value="monitorOk"/></span> / <span
                                            class="text-warning"><s:property value="monitorInsufficientData"/></span> / <span
                                            class="text-danger"><s:property value="monitorAlarm"/></span>
                                    </td>
                                </s:if>
                                <td><s:property value="ec2Region"/></td>
                            </tr>
                        </s:iterator>
                        </tbody>
                    </table>
                </div>
            </s:form>
            <s:if test="script!=null && script.id!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <div class="btn btn-primary select_frm_btn spacer spacer-bottom">Execute Script</div>
            </s:if>
            <s:else>
                <div class="btn btn-primary select_frm_btn spacer spacer-bottom">Create SSH Terminals</div>
            </s:else>
        </s:if>
        <s:else>
            <div class="actionMessage">
                <p class="error">No instances available. Try changing the filter values above
                    <s:if test="%{#session.userType==\"M\"}">
                        or importing the corresponding EC2 Keys (<a href="../manage/viewEC2Keys.action?_csrf=<s:property
                            value="#session['_csrf']"/>">Set EC2 Keys</a>).
                    </s:if>
                </p>
            </div>
        </s:else>

        <s:iterator var="system" value="sortedSet.itemList" status="stat">

            <div id="edit_dialog_<s:property value="id"/>" class="modal fade">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <h4 class="modal-title">Set Properties: <s:property
                                    value="hostSystem.displayLabel"/></h4>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <s:form action="saveSystem" id="save_sys_form_edit_%{id}">
                                    <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                    <s:textfield name="hostSystem.user" value="%{user}" label="System User"
                                                 size="10"/>
                                    <s:textfield name="hostSystem.port" value="%{port}" label="Port" size="2"/>
                                    <s:hidden name="hostSystem.id" value="%{id}"/>
                                    <s:hidden name="hostSystem.displayNm" value="%{displayNm}"/>
                                    <s:hidden name="hostSystem.host" value="%{host}"/>
                                    <s:hidden name="hostSystem.keyId" value="%{keyId}"/>
                                    <s:hidden name="hostSystem.displayLabel" value="%{displayLabel}"/>
                                    <s:hidden name="hostSystem.ec2Region" value="%{ec2Region}"/>
                                    <s:hidden name="hostSystem.state" value="%{state}"/>
                                    <s:hidden name="hostSystem.instance" value="%{instance}"/>
                                    <s:hidden name="hostSystem.instanceStatus" value="%{instanceStatus}"/>
                                    <s:hidden name="hostSystem.systemStatus" value="%{systemStatus}"/>
                                    <s:hidden name="sortedSet.orderByDirection"/>
                                    <s:hidden name="sortedSet.orderByField"/>
                                    <s:hidden name="selectForm"/>
                                    <s:if test="script!=null && script.id!=null">
                                        <s:hidden name="script.id"/>
                                    </s:if>
                                    <s:hidden name="showStatus"/>
                                </s:form>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary cancel_btn" data-dismiss="modal">Cancel
                            </button>
                            <button type="button" class="btn btn-primary submit_btn">Submit</button>
                        </div>
                    </div>
                </div>
            </div>

        </s:iterator>

    </div>

</div>
</body>
</html>
