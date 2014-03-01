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

            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 250,
                width: 500,
                modal: true
            });
            $("#script_dia").dialog({
                autoOpen: false,
                height: 350,
                width: 350,
                modal: true,
                open: function (event, ui) {
                    $(".ui-dialog-titlebar-close").show();
                }
            });

            //open edit dialog
            $(".edit_btn").click(function () {
                //get dialog id to open
                var id = $(this).attr('id').replace("edit_btn_", "");
                $("#edit_dialog_" + id).dialog("open");

            });
            $("#script_btn").click(function () {
                $("#script_dia").dialog("open");
            });

            //submit edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('form:first').submit();
            });
            //close all forms

            $(".cancel_btn").button().click(function () {
                $(".edit_dialog").dialog("close");
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
                var id = $(this).attr('id')

                if ($('#viewSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSystems_sortedSet_orderByField').attr('value', id);
                $("#viewSystems").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>


            $('.scrollableTable').tableScroll({height: 500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");

            $('#view_btn').unbind().click(function () {
                $('#view_frm').submit();
            });

        });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function () {
                <s:if test="hostSystem.id>0">
                $("#edit_dialog_<s:property value="hostSystem.id"/>").dialog("open");
                </s:if>
            });
        </script>
    </s:if>

    <title>EC2Box - Manage Instances</title>
</head>
<body>

<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">
    <s:form action="viewSystems">
        <s:hidden name="sortedSet.orderByDirection"/>
        <s:hidden name="sortedSet.orderByField"/>
        <s:if test="script!=null && script.id!=null">
            <s:hidden name="script.id"/>
        </s:if>
    </s:form>


    <s:if test="script!=null && script.id!=null">
        <h3>Execute Script on Instances</h3>
    </s:if>
    <s:else>
        <h3>Composite SSH Terminals</h3>
    </s:else>

    <s:if test="(sortedSet.itemList!= null && !sortedSet.itemList.isEmpty())||tag!=null||securityGroup!=null">

        <s:if test="script!=null && script.id!=null">
            <p>Run <b>
                <a id="script_btn" href="#"><s:property value="script.displayNm"/></a></b> on the selected instances
                below

            <div class="note">(Select on the user field to change the instance username and other properties)</div>
            </p>
            <div id="script_dia" title="View Script">
                <pre><s:property value="script.script"/></pre>
            </div>
        </s:if>
        <s:else>
            <p>Select the instances below to generate composite SSH sessions in multiple terminals

            <div class="note">(Select on the user field to change the instance username and other properties)</div>
            </p>
        </s:else>

        <s:form id="view_frm" action="viewSystems" theme="simple">
            <label>Tag</label>&nbsp;&nbsp;<s:textfield name="tag" placeholder="tag-name[=value[,tag-name[=value]]"
                                                       theme="simple" size="30"/>
            &nbsp;&nbsp;&nbsp;&nbsp;<label>Security Group</label>&nbsp;&nbsp;<s:textfield name="securityGroup"
                                                                                          placeholder="group[,group]"
                                                                                          theme="simple" size="30"/>
            <div id="view_btn" class="btn btn-primary">Filter</div>
        </s:form>

        <s:if test="(sortedSet.itemList!= null && !sortedSet.itemList.isEmpty())">
            <s:form action="selectSystemsForCompositeTerms" id="select_frm" theme="simple">
                <s:if test="script!=null && script.id!=null">
                    <s:hidden name="script.id"/>
                </s:if>
                <table class="table-striped scrollableTable" style="width:100%">
                    <thead>
                    <tr>
                        <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                        theme="simple"/></th>

                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display
                            Name
                        </th>

                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_INSTANCE_ID"/>" class="sort">
                            Instance Id
                        </th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User
                        </th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host
                        </th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_PORT"/>" class="sort">Port
                        </th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_STATE"/>" class="sort">State
                        </th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_INSTANCE_STATUS"/>"
                            class="sort">I-Status
                        </th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_SYSTEM_STATUS"/>"
                            class="sort">S-Status
                        </th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_REGION"/>" class="sort">
                            Region
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator var="system" value="sortedSet.itemList" status="stat">
                        <tr>
                            <td>
                                <s:if test="state=='running'">
                                    <s:checkboxlist name="systemSelectId" list="#{id:''}" cssClass="systemSelect"
                                                    theme="simple"/>
                                </s:if>
                            </td>

                            <td>
                                <s:property value="displayNm"/>
                            </td>
                            <td><s:property value="instanceId"/></td>
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
                            <td><s:property value="instanceStatus"/></td>
                            <td><s:property value="systemStatus"/></td>
                            <td><s:property value="ec2Region"/></td>
                        </tr>
                    </s:iterator>
                    </tbody>
                </table>
            </s:form>
        <s:if test="script!=null && script.id!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="btn btn-primary select_frm_btn">Execute Script</div>
        </s:if>
        <s:else>
            <div class="btn btn-primary select_frm_btn">Create SSH Terminals</div>
        </s:else>
        </s:if>

        <s:iterator var="system" value="sortedSet.itemList" status="stat">

            <div id="edit_dialog_<s:property value="id"/>" title="Set Properties" class="edit_dialog">
                <p><s:property value="displayLabel"/></p>
                <s:form action="saveSystem" id="save_sys_form_edit_%{id}">
                    <s:textfield name="hostSystem.user" value="%{user}" label="System User" size="10"/>


                    <tr>
                        <td class="tdLabel">
                            <label class="label">Host</label>
                        </td>
                        <td>
                            <s:property value="host"/>
                        </td>
                    </tr>
                    <s:textfield name="hostSystem.port" value="%{port}" label="Port" size="2"/>

                    <s:hidden name="hostSystem.id" value="%{id}"/>
                    <s:hidden name="hostSystem.displayNm" value="%{displayNm}"/>
                    <s:hidden name="hostSystem.host" value="%{host}"/>
                    <s:hidden name="hostSystem.keyId" value="%{keyId}"/>
                    <s:hidden name="hostSystem.displayLabel" value="%{displayLabel}"/>
                    <s:hidden name="hostSystem.ec2Region" value="%{ec2Region}"/>
                    <s:hidden name="hostSystem.state" value="%{state}"/>
                    <s:hidden name="hostSystem.instanceId" value="%{instanceId}"/>
                    <s:hidden name="hostSystem.instanceStatus" value="%{instanceStatus}"/>
                    <s:hidden name="hostSystem.systemStatus" value="%{systemStatus}"/>
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>
                    <s:hidden name="selectForm"/>
                    <s:if test="script!=null && script.id!=null">
                        <s:hidden name="script.id"/>
                    </s:if>
                    <tr>
                        <td>
                        </td>
                        <td>
                            <div class="btn btn-primary submit_btn">Submit</div>
                            <div class="btn btn-primary cancel_btn">Cancel</div>
                        </td>
                    </tr>
                </s:form>
            </div>


        </s:iterator>
    </s:if>
    <s:else>
        <div class="actionMessage">
            <p class="error">Instances not available.
                <s:if test="%{#session.userType==\"M\"}">

                    Import EC2 Keys <a href="../manage/viewEC2Keys.action">here</a>
                </s:if>
            </p>
        </div>
    </s:else>

</div>

</body>
</html>
