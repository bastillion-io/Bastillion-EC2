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
<div class="nav">

    <div style="float: left;margin-top: 5px;margin-left: -10px"><img
            src="<%= request.getContextPath() %>/img/keybox_50x38.png"/></div>

    <h3>
        <a href="menu.jsp">EC2Box</a>
    </h3>

     <div class="nav_item">
            <a href="setAWSCred.action">Set AWS Credentials</a>
     </div>

     <div class="nav_item">
        <a href="viewEC2Keys.action">Set EC2 Keys</a>
    </div>
         <div class="nav_item">
            <a href="viewSystems.action">Composite SSH Terms</a>
         </div>
         <div class="nav_item">
            <a href="viewScripts.action">Composite Scripts</a>
          </div>
    <div class="nav_item">
        <a href="setPassword.action">Change Password</a>
    </div>
    <div class="nav_item">
        <a href="../logout.action">Logout</a>
    </div>

</div>
