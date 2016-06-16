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
package com.ec2box.common.util;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Utility to look up configurable commands and resources
 */
public class AppConfig {

    private static ResourceBundle prop = ResourceBundle.getBundle("EC2BoxConfig");

    private AppConfig() {
    }

    /**
     * gets the property from config
     *
     * @param name property name
     * @return configuration property
     */
    public static String getProperty(String name) {

        return prop.getString(name);
    }

    /**
     * gets the property from config and replaces placeholders
     *
     * @param name property name
     * @param replacementMap name value pairs of place holders to replace
     * @return configuration property
     */
    public static String getProperty(String name, Map<String, String> replacementMap) {

        String value = prop.getString(name);
        //iterate through map to replace text
        Set<String> keySet = replacementMap.keySet();
        for(String key :keySet){
            //replace values in string
            String rVal=replacementMap.get(key);
            value=value.replace("${"+key+"}",rVal);
        }
        return value;
    }

    /**
     * gets the property from config and returns map of name / value pairs
     *
     * @param name property name
     * @return configuration property
     */
    public static Map<String,String> getMapProperties(String name) {

        String values= prop.getString(name);
        Map<String,String> map= new LinkedHashMap<>();

        for(String set:values.split(";")){
            String key=set.split(",")[0];
            String val =set.split(",")[1];
            map.put(key,val);

        }
        return map;
    }



}
