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


    <title>EC2Box - Main Menu</title>

    <script type="text/javascript">
        $(document).ready(function() {


            $("table").css("background-color", "#ffffff");
            $("table tr:even").css("background-color", "#e0e0e0");

        });
    </script>


    <style type="text/css">
        .vborder td {
            padding:10px;
            white-space: normal;
        }
    </style>


</head>


<body style="background: #FFFFFF">
<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <table class="vborder" >
            <thead>
            <tr>
                <th colspan="2">Main Menu</th>
            </tr>
            </thead>

            <tbody>
            <tr>

                <td>
                    <a href="setAWSCred.action">Set AWS Credentials</a>

                </td>


                <td>
                  Set your Amazon Web Service credentials.
                </td>

            </tr>
            <tr>
                <td>
                    <a href="viewEC2Keys.action">Set EC2 Keys</a>

                </td>
                <td>
                 Import the private keys used on your EC2 systems.
                </td>
            </tr>
            <tr>
                <td>
                    <a href="setEC2Region.action">Set EC2 Region</a>

                </td>
                <td>
                 Select the regions to manage.
                </td>
            </tr>



            <tr>
                <td>
                  <a href="viewSystems.action">Composite SSH Terms</a>

                </td>
                <td>
                    Execute multiple-simultaneous web-terminals on selected systems.
                </td>
            </tr>

            <tr>
                <td>
                   <a href="viewScripts.action">Composite Scripts</a>

                </td>
                <td>
                    Create scripts to be executed on selected systems simultaneously through a web-terminal
                </td>
            </tr>

              <tr>
                <td>
                  <a href="setPassword.action">Change Password</a>
                </td>
                <td>
                    Change administrative login to application
                </td>
            </tr>


            </tbody>
        </table>
    </div>
</div>
</body>
</html>