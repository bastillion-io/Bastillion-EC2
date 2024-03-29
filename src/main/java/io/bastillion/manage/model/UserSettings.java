/**
 *    Copyright (C) 2015 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import org.apache.commons.lang3.StringUtils;


/**
 * User theme value object
 */
public class UserSettings {

    String[] colors = null;
    String bg;
    String fg;
    String plane;
    String theme;
    Integer ptyWidth;
    Integer ptyHeight;

    public String[] getColors() {
        return colors;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getFg() {
        return fg;
    }

    public void setFg(String fg) {
        this.fg = fg;
    }


    public String getPlane() {
        if(StringUtils.isNotEmpty(bg) && StringUtils.isNotEmpty(fg)){
            plane=bg+","+fg;
        }
        return plane;
    }

    public void setPlane(String plane) {
        if(StringUtils.isNotEmpty(plane) && plane.split(",").length==2){
            this.setBg(plane.split(",")[0]);
            this.setFg(plane.split(",")[1]);
        }
        this.plane = plane;
    }

    public String getTheme() {
        if(this.colors!=null && this.colors.length==16){
            theme=StringUtils.join(this.colors,",");
        }
        return theme;
    }

    public void setTheme(String theme) {
        if(StringUtils.isNotEmpty(theme) && theme.split(",").length==16){
            this.setColors(theme.split(","));
        }
        this.theme = theme;
    }

    public Integer getPtyWidth() {
        return ptyWidth;
    }

    public void setPtyWidth(Integer ptyWidth) {
        this.ptyWidth = ptyWidth;
    }

    public Integer getPtyHeight() {
        return ptyHeight;
    }

    public void setPtyHeight(Integer ptyHeight) {
        this.ptyHeight = ptyHeight;
    }
}
