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

        function populateKeyNames() {
            $.getJSON('getKeyPairJSON.action?_csrf=<s:property value="#session['_csrf']"/>&ec2Key.awsCredId='+$("#importEC2Key_ec2Key_awsCredId").val()+'&ec2Key.ec2Region='+$("#importEC2Key_ec2Key_ec2Region").val(), function(result) {

              $("#importEC2Key_ec2Key_keyNm option").remove();
                var options = $("#importEC2Key_ec2Key_keyNm");
                options.append($("<option />").val('').text('-Select Key Name-'));
                $.each(result, function() {
                    if(this.keyName!=null){
                        options.append($("<option />").val(this.keyName).text(this.keyName));
                    }
                });

              $("#importEC2Key_ec2Key_keyNm option[value='<s:property value="ec2Key.keyNm"/>']").attr("selected",true);
            });



        }

        $(document).ready(function() {


            //call delete action
            $(".del_btn").button().click(function() {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteEC2Key.action?_csrf=<s:property value="#session['_csrf']"/>&ec2Key.id='+ id +'&ec2Key.ec2Region=<s:property value="ec2Key.ec2Region" />&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).parents('.modal').find('form').submit();
            });


            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id');

                if ($('#viewEC2Keys_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewEC2Keys_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewEC2Keys_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewEC2Keys_sortedSet_orderByField').attr('value', id);
                $("#viewEC2Keys").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

        });
    </script>


       <s:if test="fieldErrors.size > 0 || actionErrors.size >0">
            <script type="text/javascript">
                $(document).ready(function() {

                     populateKeyNames();

                    <s:if test="ec2Key.privateKey!=null">
                    $("#import_dialog").modal();
                    </s:if>


                });
            </script>
        </s:if>



    <title>EC2Box - Manage EC2 Keys</title>

</head>
<body>

    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">

            <h3>Manage EC2 Keys</h3>

     <s:if test="awsCredList.isEmpty()">
        <div class="actionMessage">
            <p class="error">
         EC2 Keys not available (<a href="viewAWSCred.action?_csrf=<s:property value="#session['_csrf']"/>">Set AWS Credentials</a>).
            </p>
        </div>
    </s:if>
    <s:else>
          <p>Import and register EC2 keys below. An EC2 server will only show after its private key has been imported</p>

            <p>
            <s:form action="viewEC2Keys">
            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            <s:hidden name="sortedSet.orderByDirection" />
            <s:hidden name="sortedSet.orderByField"/>
            </s:form>
            </p>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
        <div class="scrollWrapper">
                <table class="table-striped scrollableTable">
                    <thead>
                    <tr>

                        <th id="<s:property value="@com.ec2box.manage.db.EC2KeyDB@KEY_NM"/>" class="sort">Key Name</th>
                        <th id="<s:property value="@com.ec2box.manage.db.EC2KeyDB@EC2_REGION"/>" class="sort">EC2 Region</th>
                        <s:if test="awsCredList.size()>1">
                        <th id="<s:property value="@com.ec2box.manage.db.EC2KeyDB@ACCESS_KEY"/>" class="sort">Access Key</th>
                        </s:if>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator var="ec2Key" value="sortedSet.itemList" status="stat">
                    <tr>
                        <td>
                                <s:property value="keyNm"/>
                        </td>
                        <td>
                        <s:set var="ec2Region" value="%{ec2Region}"/>
                        <s:property value="%{ec2RegionMap.get(#ec2Region)}"/>
                        </td>
                        <s:if test="awsCredList.size()>1">
                        <td><s:property value="accessKey"/></td>
                        </s:if>
                            <td>
                                <div id="del_btn_<s:property value="id"/>" class="btn btn-primary del_btn spacer spacer-left" >
                                    Delete
                                </div>
                                <div style="clear:both"></div>
                            </td>
                    </tr>
                    </s:iterator>
                    </tbody>
                </table>
            </div>
        </s:if>



        <button class="btn btn-primary add_btn spacer spacer-bottom" data-toggle="modal" data-target="#import_dialog">Import Private Key</button>
        <div id="import_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Import Existing EC2 Key</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:actionerror/>
                            <s:form action="importEC2Key" class="save_ec2Key_form_import">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                <s:if test="awsCredList.size()==1">
                                    <s:hidden name="ec2Key.awsCredId" value="%{awsCredList.get(0).getId()}"/>
                                </s:if>
                                <s:else>
                                    <s:select name="ec2Key.awsCredId" list="awsCredList" listKey="id" listValue="accessKey" label="Access Key" />
                                </s:else>
                                <s:select name="ec2Key.ec2Region"  list="ec2RegionMap" label="EC2 Region" headerKey="" headerValue="-Select-" onchange="populateKeyNames();" />
                                <s:select name="ec2Key.keyNm" label="Key Name" list="#{'':'-Select Region Above-'}"/>
                                <s:textarea name="ec2Key.privateKey" label="Private Key Value"  rows="15" cols="35" wrap="off"/>

                                <s:hidden name="sortedSet.orderByDirection"/>
                                <s:hidden name="sortedSet.orderByField"/>
                            </s:form>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary cancel_btn" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-primary submit_btn">Submit</button>
                    </div>
                </div>
            </div>
        </div>

    </s:else>

    </div>
</body>
</html>
