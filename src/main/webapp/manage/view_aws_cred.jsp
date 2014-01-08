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

            $("#add_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 400,
                modal: true
            });



            //open add dialog
            $("#add_btn").button().click(function() {
                $("#add_dialog").dialog("open");
            });

            //call delete action
            $(".del_btn").button().click(function() {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteAWSCred.action?awsCred.id='+ id +'&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add form
            $(".submit_btn").button().click(function() {
                $(this).parents('form:first').submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#add_dialog").dialog("close");
            });

            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewAWSCred_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewAWSCred_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewAWSCred_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewAWSCred_sortedSet_orderByField').attr('value', id);
                $("#viewAWSCred").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>


            $('.scrollableTable').tableScroll({height:500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>

    <s:if test="fieldErrors.size > 0 || actionErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {

                $("#add_dialog").dialog("open");

            });
        </script>
    </s:if>

    <title>EC2Box - Set AWS Credentials</title>

</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:form action="viewAWSCred">
            <s:hidden name="sortedSet.orderByDirection" />
            <s:hidden name="sortedSet.orderByField"/>
        </s:form>

        <h3>Set AWS Credentials</h3>
        <p>Add / Delete your AWS credentials below</p>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <table class="vborder scrollableTable">
                <thead>

                <tr>

                    <th id="<s:property value="@com.ec2box.manage.db.AWSCredDB@SORT_BY_ACCESS_KEY"/>" class="sort">Access Key
                    </th>

                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>
                <s:iterator value="sortedSet.itemList" status="stat">
                    <tr>
                        <td><s:property value="accessKey"/></td>
                        <td>
                            <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">
                                Delete
                            </div>
                            <div style="clear:both"></div>
                        </td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
        </s:if>





        <div id="add_btn">Add AWS Credentials</div>
        <div id="add_dialog" title="Add AWS Credentials">
            <s:actionerror/>
            <s:form action="saveAWSCred" class="save_aws_form_add" autocomplete="off">
               <s:textfield name="awsCred.accessKey" label="Access Key" size="35" />
                <s:password name="awsCred.secretKey" label="Secret Key" size="35" />
                <s:hidden name="sortedSet.orderByDirection"/>
                <s:hidden name="sortedSet.orderByField"/>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <div class="submit_btn">Submit</div>
                        <div class="cancel_btn">Cancel</div>
                    </td>
                </tr>
            </s:form>

        </div>




    </div>
</div>
</body>
</html>
