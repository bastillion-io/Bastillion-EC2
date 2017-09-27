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
package com.ec2box.manage.task;

import com.ec2box.manage.model.User;
import com.google.gson.Gson;
import com.ec2box.manage.model.SessionOutput;
import com.ec2box.manage.util.DBUtils;
import com.ec2box.manage.util.SessionOutputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.sql.Connection;
import java.util.List;

/**
 * class to send output to web socket client
 */
public class SentOutputTask implements Runnable {

    private static Logger log = LoggerFactory.getLogger(SentOutputTask.class);

    Session session;
    Long sessionId;
    User user;

    public SentOutputTask(Long sessionId, Session session, User user) {
        this.sessionId = sessionId;
        this.session = session;
        this.user = user;
    }

    public void run() {

        while (session.isOpen()) {
            Connection con = DBUtils.getConn();
            List<SessionOutput> outputList = SessionOutputUtil.getOutput(con, sessionId, user);
            try {
                if (outputList != null && !outputList.isEmpty()) {
                    String json = new Gson().toJson(outputList);
                    //send json to session
                    this.session.getBasicRemote().sendText(json);
                }
                Thread.sleep(50);
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }
            finally {
                DBUtils.closeConn(con);
            }
        }
    }
}
