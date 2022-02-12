/**
 *    Copyright (C) 2015 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.manage.db.ProfileDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.db.UserProfileDB;
import io.bastillion.manage.model.Profile;
import io.bastillion.manage.model.SortedSet;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to assign users to profiles
 */
public class ProfileUsersKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(ProfileUsersKtrl.class);

    @Model(name = "profile")
    Profile profile;
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "userSelectId")
    List<Long> userSelectId = new ArrayList<>();

    public ProfileUsersKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/viewProfileUsers", method = MethodType.GET)
    public String viewProfileUsers() throws ServletException {
        if (profile != null && profile.getId() != null) {
            try {
            profile = ProfileDB.getProfile(profile.getId());
            sortedSet = UserDB.getAdminUserSet(sortedSet, profile.getId());
            } catch (GeneralSecurityException | SQLException ex ){
                log.error(ex.toString(),ex);
                throw new ServletException(ex.toString(), ex);
            }
        }
        return "/manage/view_profile_users.html";
    }

    @Kontrol(path = "/manage/assignUsersToProfile", method = MethodType.POST)
    public String assignSystemsToProfile() throws ServletException {

        if (userSelectId != null) {
            try {
                UserProfileDB.setUsersForProfile(profile.getId(), userSelectId);
            } catch (GeneralSecurityException | SQLException ex ){
                log.error(ex.toString(),ex);
                throw new ServletException(ex.toString(), ex);
            }
        }
        return "redirect:/manage/viewProfiles.ktrl";
    }

}
