/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.task;

import io.bastillion.manage.model.User;
import com.google.gson.Gson;
import io.bastillion.manage.model.SessionOutput;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.SessionOutputUtil;
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
