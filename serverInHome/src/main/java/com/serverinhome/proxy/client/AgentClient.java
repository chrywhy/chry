/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.proxy.client;

import com.serverinhome.gate.ActiveCsrs;
import com.serverinhome.gate.Csr;
import com.serverinhome.gate.websocket.WebsocketConnector;
import com.serverinhome.gate.websocket.response.AccessResponse;
import com.serverinhome.gate.websocket.response.AccessResponse.ResponseType;
import com.serverinhome.gate.websocket.response.HttpResponseBody;
import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        HttpResponseStream hrs = null;
        Csr csr = ActiveCsrs.get(userName);
        if (csr != null) {
            Session session = csr.getSession();
            try {
                session.getBasicRemote().sendText(WebsocketConnector.createDispatchMessage(userName, url));
                hrs = csr.getResponseStream();
            } catch (IOException e) {
                System.out.println("Failed to get response rfom client");
            }
        }
        return hrs;
    }

    public void sendRequest(String userName, String url) {
        Csr csr = ActiveCsrs.get(userName);
        if (csr != null) {
            Session session = csr.getSession();
            String msg = WebsocketConnector.createDispatchMessage(userName, url);
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException ex) {
                System.err.println("Failed to sent request agent");
            }
        }
    }
}
