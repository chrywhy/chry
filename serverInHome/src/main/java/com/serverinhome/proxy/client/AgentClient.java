/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.proxy.client;

import com.serverinhome.gate.ActiveCsrs;
import com.serverinhome.gate.Csr;
import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;
import java.io.IOException;
import javax.websocket.Session;

/**
 *
 * @author chry
 */
public class AgentClient {
    
    public HttpResponseStream post(String userName, String url, HttpPostStream hps) {
        return null;
    }

    public HttpResponseStream get(String userName, String url) {
        Csr csr = ActiveCsrs.get(userName);
        if (csr != null) {
            Session session = csr.getSession();
            try {
                session.getBasicRemote().sendText(url);
                HttpResponseStream hrs = csr.getResponse();
            } catch (IOException e) {
                System.out.println("Failed to get response rfom client");
            }
        }
        return null;
    }
}
