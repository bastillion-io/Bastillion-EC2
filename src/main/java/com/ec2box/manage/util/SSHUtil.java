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
package com.ec2box.manage.util;

import com.ec2box.manage.model.HostSystem;
import com.ec2box.manage.model.UserSchSessions;
import com.jcraft.jsch.*;
import com.ec2box.manage.db.EC2KeyDB;
import com.ec2box.manage.db.SystemStatusDB;
import com.ec2box.manage.model.EC2Key;
import com.ec2box.manage.model.SchSession;
import com.ec2box.manage.task.SessionOutputTask;

import java.io.*;
import java.util.Map;

/**
 * SSH utility class used to create public/private key for system and distribute authorized key files
 */
public class SSHUtil {

    //system path to public/private key
    public static String KEY_PATH = DBUtils.class.getClassLoader().getResource("keydb").getPath();

    public static final int SESSION_TIMEOUT = 60000;
    public static final int CHANNEL_TIMEOUT = 60000;






    /**
     * distributes uploaded item to system defined
     *
     * @param hostSystem object contains host system information
     * @param session          an established SSH session
     * @param source           source file
     * @param destination      destination file
     * @return status uploaded file
     */
    public static HostSystem pushUpload(HostSystem hostSystem, Session session, String source, String destination) {


        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        Channel channel = null;
        ChannelSftp c = null;

        try {


            channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect(CHANNEL_TIMEOUT);

            c = (ChannelSftp) channel;
            destination = destination.replaceAll("~\\/|~", "");


            //get file input stream
            FileInputStream file = new FileInputStream(source);
            c.put(file, destination);


        } catch (Exception e) {
            hostSystem.setErrorMsg(e.getMessage());
            hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
        }
        //exit
        if (c != null) {
            c.exit();
        }
        //disconnect
        if (channel != null) {
            channel.disconnect();
        }

        return hostSystem;


    }


    /**
     * open new ssh session on host system
     *
     * @param passhrase key passphrase for instance
     * @param password password for instance
     * @param userId user id
     * @param sessionId session id
     * @param hostSystem host system
     * @param userSessionMap user session map
     * @return status of systems
     */
    public static HostSystem openSSHTermOnSystem(String passhrase, String password, Long userId, Long sessionId, HostSystem hostSystem,  Map<Long, UserSchSessions> userSessionMap) {

        JSch jsch = new JSch();

        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);

        SchSession schSession = null;

        try {
           EC2Key ec2Key = EC2KeyDB.getEC2KeyByKeyNm(hostSystem.getKeyNm(), hostSystem.getEc2Region());
            //add private key
            if(ec2Key!=null && ec2Key.getId()!=null){
                if(passhrase!=null && !passhrase.trim().equals("")){
                    jsch.addIdentity(KEY_PATH + "/" + ec2Key.getId()+".pem", passhrase);
                }else{
                    jsch.addIdentity(KEY_PATH + "/" + ec2Key.getId()+".pem");

                }
            }

            //create session
            Session session = jsch.getSession(hostSystem.getUser(), hostSystem.getHost(), hostSystem.getPort());


            //set password if it exists
            if (password != null && !password.trim().equals("")) {
                session.setPassword(password);
            }


            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("vt102");

            InputStream outFromChannel = channel.getInputStream();

            Runnable run=new SessionOutputTask(sessionId, hostSystem.getId(), userId, outFromChannel);
            Thread thread = new Thread(run);
            thread.start();


            OutputStream inputToChannel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputToChannel, true);


            channel.connect();

            schSession = new SchSession();
            schSession.setSession(session);
            schSession.setChannel(channel);
            schSession.setCommander(commander);
            schSession.setInputToChannel(inputToChannel);
            schSession.setOutFromChannel(outFromChannel);
            schSession.setHostSystem(hostSystem);


        } catch (Exception e) {
            hostSystem.setErrorMsg(e.getMessage());
            if (e.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("auth fail") || e.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystem.setStatusCd(HostSystem.AUTH_FAIL_STATUS);
            } else {
                hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
            }
        }


        //add session to map
        if (hostSystem.getStatusCd().equals(HostSystem.SUCCESS_STATUS)) {
            //get the server maps for user
            UserSchSessions userSchSessions = userSessionMap.get(userId);

            //if no user session create a new one
            if (userSchSessions == null) {
                userSchSessions = new UserSchSessions();
            }
            Map<Long, SchSession> schSessionMap = userSchSessions.getSchSessionMap();

            //add server information
            schSessionMap.put(hostSystem.getId(), schSession);
            userSchSessions.setSchSessionMap(schSessionMap);
            //add back to map
            userSessionMap.put(userId, userSchSessions);
        }

        SystemStatusDB.updateSystemStatus(hostSystem,userId);

        return hostSystem;
    }

    /**
     * Stores private key for ec2 instances on filesystem
     * @param keyName key name
     * @param keyValue key value
     */
    public static void storePrivateKey(String keyName, String keyValue) {
        try {


            //create key file
            File keyFile = new File(KEY_PATH + "/" + keyName + ".pem");
            PrintWriter keyFileWriter = new PrintWriter(keyFile);
            keyFileWriter.print(keyValue);
            keyFileWriter.close();


            //set file permissions to 600
            keyFile.setReadable(false, false);
            keyFile.setReadable(true, true);

            keyFile.setWritable(false, false);
            keyFile.setWritable(true, true);

            keyFile.setExecutable(false, false);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * deletes private key from filesystem
     * @param keyName key name
     */
    public static void deletePrivateKey(String keyName) {
        try {
            File keyFile = new File(KEY_PATH + "/" + keyName + ".pem");
            keyFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
