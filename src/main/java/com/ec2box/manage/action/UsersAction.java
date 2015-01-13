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


import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.ec2box.manage.db.UserDB;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.model.User;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action to manage users
 */
public class UsersAction extends ActionSupport  implements ServletRequestAware {

    /**
	 * 
	 */
	private static final long serialVersionUID = 338185512652521656L;
	SortedSet sortedSet=new SortedSet();
    User user = new User();
    HttpServletRequest servletRequest;

    @Action(value = "/manage/viewUsers",
            results = {
                    @Result(name = "success", location = "/manage/view_users.jsp")
            }
    )
    public String viewUsers() {
        sortedSet = UserDB.getUserSet(sortedSet);

        return SUCCESS;
    }

    @Action(value = "/manage/saveUser",
            results = {
                    @Result(name = "input", location = "/manage/view_users.jsp"),
                    @Result(name = "success", location = "/manage/viewUsers.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type="redirect")
            }
    )
    public String saveUser() {

        if (user.getId() != null) {
            if(user.getPassword()==null || user.getPassword().trim().equals("")){
                User tmpUser = UserDB.getUser(getUser().getId());
                user.setPassword(tmpUser.getPassword());
            }
            user.setExpiryTime(DateUtils.addMinutes(new Date(), user.getTimeToExpire()));
            UserDB.updateUser(user);
        } else {
        	user.setExpiryTime(DateUtils.addMinutes(new Date(), user.getTimeToExpire()));
            UserDB.insertUser(user);
        }
        return SUCCESS;
    }

    @Action(value = "/manage/deleteUser",
            results = {
                    @Result(name = "success", location = "/manage/viewUsers.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type="redirect")
            }
    )
    public String deleteUser() {

        if (user.getId() != null) {
            UserDB.disableUser(user.getId());
        }
        return SUCCESS;
    }

    /**
     * Validates all fields for adding a user
     */
    public void validateSaveUser() {

        if (user == null
                || user.getUsername() == null
                || user.getUsername().trim().equals("")) {
            addFieldError("user.username", "Required");
        }

        if (user == null
                || user.getLastNm() == null
                || user.getLastNm().trim().equals("")) {
            addFieldError("user.lastNm", "Required");
        }

        if (user == null
                || user.getFirstNm() == null
                || user.getFirstNm().trim().equals("")) {
            addFieldError("user.firstNm", "Required");
        }
        if (user != null
                && user.getPassword() != null
                && !user.getPassword().trim().equals("")
                && !user.getPassword().equals(user.getPasswordConfirm())) {
            addActionError("Passwords do not match");
        }

        if(user!=null && user.getId()==null && (user.getPassword()==null || user.getPassword().trim().equals(""))){
            addActionError("Password is required");
        }

        if(user!=null && !UserDB.isUnique(user.getId(),user.getUsername())){
            addActionError("Username has been taken");
        }
        if (!this.getFieldErrors().isEmpty()||!this.getActionErrors().isEmpty()) {
            sortedSet = UserDB.getUserSet(sortedSet);
        }


    }


    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
