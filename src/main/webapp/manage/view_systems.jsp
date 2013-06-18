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
        $(document).ready(function() {

            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 500,
                modal: true
            });
             $("#script_dia").dialog({
                autoOpen: false,
                height: 350,
                width: 350,
                modal: true,
                open: function(event, ui) {
                    $(".ui-dialog-titlebar-close").show();
                },
            });

            //open edit dialog
            $(".edit_btn").click(function() {
                //get dialog id to open
                var id = $(this).attr('id').replace("edit_btn_", "");
                $("#edit_dialog_" + id).dialog("open");

            });
            $("#script_btn").click(function() {
                $("#script_dia").dialog("open");
             });

            //submit edit form
            $(".submit_btn").button().click(function() {
                $(this).parents('form:first').submit();
            });
            //close all forms

            $(".cancel_btn").button().click(function() {
                $(".edit_dialog").dialog("close");
            });

            $(".select_frm_btn").button().click(function() {
                $("#select_frm").submit();
            });
            //select all check boxes
            $("#select_frm_systemSelectAll").click(function() {

                if ($(this).is(':checked')) {
                    $(".systemSelect").attr('checked', true);
                } else {
                    $(".systemSelect").attr('checked', false);
                }
            });
            $(".sort,.sortAsc,.sortDesc").click(function() {
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


                $('.scrollableTable').tableScroll({height:500});
                $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
    });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {
                <s:if test="hostSystem.id>0">
                $("#edit_dialog_<s:property value="hostSystem.id"/>").dialog("open");
                </s:if>
            });
        </script>
    </s:if>

    <title>EC2Box - Manage Systems</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:form action="viewSystems">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
            <s:if test="script!=null">
              <s:hidden name="script.id"/>
            </s:if>
        </s:form>


        <s:if test="script!=null">
            <h3>Execute Script on Systems</h3>
        </s:if>
        <s:else>
            <h3>Composite SSH Terminals</h3>
        </s:else>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

            <s:if test="script!=null">
                <p>Run <b>
                <a id="script_btn" href="#"><s:property value="script.displayNm"/></a></b> on the selected systems below
                </p>
                <div id="script_dia" title="View Script">
                    <pre><s:property value="script.script"/></pre>
                </div>
            </s:if>
            <s:else>
                <p>Select the systems below to generate composite SSH sessions in multiple terminals</p>
            </s:else>


  	        <s:form action="selectSystemsForCompositeTerms" id="select_frm" theme="simple">
  	        <s:if test="script!=null">

                        <s:hidden name="script.id"/>
                 </s:if>
                <table class="vborder scrollableTable">
                    <thead>
                    <tr>
                            <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                            theme="simple"/></th>

                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display Name</th>

                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_INSTANCE_ID"/>" class="sort">Instance Id</th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User</th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host</th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_STATE"/>" class="sort">State</th>
                        <th id="<s:property value="@com.ec2box.manage.db.SystemDB@SORT_BY_REGION"/>" class="sort">Region</th>
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

                            <a id="edit_btn_<s:property value="id"/>" title="Change User" class="edit_btn" href="#">
                            <s:property value="user"/>
                            </a>
                            </td>
                            <td><s:property value="host"/></td>
                            <td><s:property value="state"/></td>
                            <td><s:property value="ec2Region"/></td>

                        </tr>

                    </s:iterator>
                    </tbody>
                </table>
	    </s:form>
            <s:if test="script!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <div class="select_frm_btn">Execute Script</div>
            </s:if>
            <s:else>
                <div class="select_frm_btn">Create SSH Terminals</div>
            </s:else>

            <s:iterator var="system" value="sortedSet.itemList" status="stat">

                <div id="edit_dialog_<s:property value="id"/>" title="Set User" class="edit_dialog">
                <p><s:property value="displayLabel"/></p>
                    <s:form action="saveSystem" id="save_sys_form_edit_%{id}">
                        <s:textfield name="hostSystem.user" value="%{user}" label="System User" size="10"/>
                        <s:hidden name="hostSystem.displayNm" value="%{displayNm}"/>
                        <s:hidden name="hostSystem.host" value="%{host}"/>
                        <s:hidden name="hostSystem.port" value="%{port}"/>
                        <s:hidden name="hostSystem.id" value="%{id}"/>
                        <s:hidden name="hostSystem.keyNm" value="%{keyNm}"/>
                        <s:hidden name="hostSystem.displayLabel" value="%{displayLabel}"/>
                        <s:hidden name="hostSystem.ec2Region" value="%{ec2Region}"/>
                        <s:hidden name="hostSystem.state" value="%{state}"/>
                        <s:hidden name="hostSystem.instanceId" value="%{instanceId}"/>
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:hidden name="selectForm"/>
                        <s:if test="script!=null">
                            <s:hidden name="script.id"/>
                        </s:if>
                        <tr>
                        <td>&nbsp;</td>
                        <td>
                        <div class="submit_btn">Submit</div>
                        <div class="cancel_btn">Cancel</div>
                        </td>
                        </tr>
                    </s:form>
                </div>
            </s:iterator>
            </s:if>
            <s:else>
            <div class="actionMessage">
                <div>Systems not available. Set EC2 regions <a href="setEC2Region.action">here</a></div>
            </div>
            </s:else>

    </div>
</div>

</body>
</html>
