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


            $("#change_region_btn").button().click(function() {
                $('#submitEC2Region').submit();
            });
        });

    </script>

    <title>EC2Box - Set your EC2 Region</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <h3>Set your EC2 Region</h3>

    <s:if test="hasActionMessages()">
        <s:actionmessage escape="false"/>
    </s:if>
    <s:else>
        <p>Set or change your EC2 Region below</p>
        <s:actionerror/>
        <s:form action="submitEC2Region">
            <s:checkboxlist name="ec2Region"  list="ec2RegionMap" label="EC2 Region" />
            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="change_region_btn" class="login" >Set EC2 Region</div></td>
            </tr>
        </s:form>
    </s:else>
    </div>


</div>

</body>
</html>
