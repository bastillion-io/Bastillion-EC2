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

import com.jcraft.jsch.*;
import com.ec2box.manage.db.EC2KeyDB;
import com.ec2box.manage.db.SystemStatusDB;
import com.ec2box.manage.model.EC2Key;
import com.ec2box.manage.model.SchSession;
import com.ec2box.manage.model.SystemStatus;
import com.ec2box.manage.task.SessionOutputTask;

import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

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
     * @param hostSystemStatus object contains host system information
     * @param session          an established SSH session
     * @param source           source file
     * @param destination      destination file
     * @return status uploaded file
     */
    public static SystemStatus pushUpload(SystemStatus hostSystemStatus, Session session, String source, String destination) {


        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);
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
            hostSystemStatus.setErrorMsg(e.getMessage());
            hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
        }
        //exit
        if (c != null) {
            c.exit();
        }
        //disconnect
        if (channel != null) {
            channel.disconnect();
        }

        return hostSystemStatus;


    }


    /**
     * open new ssh session on host system
      * @param adminId
     * @param passhrase
     * @param password
     * @param hostSystemStatus
     * @param schSessionMap
     * @return status of key distribution
     */
    public static SystemStatus openSSHTermOnSystem(Long adminId, String passhrase, String password, SystemStatus hostSystemStatus, Map<Long, SchSession> schSessionMap) {

        JSch jsch = new JSch();

        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);

        SchSession schSession = null;

        try {
           EC2Key ec2Key = EC2KeyDB.getEC2KeyByKeyNm( adminId, hostSystemStatus.getHostSystem().getKeyNm(), hostSystemStatus.getHostSystem().getEc2Region());
            //add private key
            if(ec2Key!=null && ec2Key.getId()!=null){
                jsch.addIdentity(KEY_PATH + "/" + ec2Key.getId()+".pem", passhrase);
            }

            //create session
            Session session = jsch.getSession(hostSystemStatus.getHostSystem().getUser(), hostSystemStatus.getHostSystem().getHost(), hostSystemStatus.getHostSystem().getPort());


            //set password if it exists
            if (password != null && !password.trim().equals("")) {
                session.setPassword(password);
            }


            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("vt102");

            InputStream outFromChannel = channel.getInputStream();

            ExecutorService executor = Executors.newCachedThreadPool();

            executor.execute(new SessionOutputTask(hostSystemStatus.getHostSystem().getId(), outFromChannel));


            OutputStream inputToChannel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputToChannel, true);


            channel.connect();

            schSession = new SchSession();
            schSession.setSession(session);
            schSession.setChannel(channel);
            schSession.setCommander(commander);
            schSession.setInputToChannel(inputToChannel);
            schSession.setOutFromChannel(outFromChannel);
            schSession.setHostSystem(hostSystemStatus.getHostSystem());


        } catch (Exception e) {
            hostSystemStatus.setErrorMsg(e.getMessage());
            if (e.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystemStatus.setStatusCd(SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("auth fail") || e.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystemStatus.setStatusCd(SystemStatus.AUTH_FAIL_STATUS);
            } else {
                hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
            }
        }

        //add session to map
        if (hostSystemStatus.getStatusCd().equals(SystemStatus.SUCCESS_STATUS)) {
            schSessionMap.put(hostSystemStatus.getHostSystem().getId(), schSession);
        }

        SystemStatusDB.updateSystemStatus(hostSystemStatus);

        return hostSystemStatus;
    }


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

    public static void deletePrivateKey(String keyName) {
        try {
            File keyFile = new File(KEY_PATH + "/" + keyName + ".pem");
            keyFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
