/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.proxy.client;

import com.serverinhome.gate.ActiveClients;
import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;
import java.io.IOException;
import javax.websocket.Session;

/**
 *
 * @author chry
 */
public class HomeServerClient {
    
    public HttpResponseStream post(String userName, String url, HttpPostStream hps) {
        return null;
    }

    public HttpResponseStream get(String userName, String url) {
        Session client = ActiveClients.get(userName);
        try {
            client.getBasicRemote().sendText(url);
            //TODO wait result
        } catch (IOException e) {
            System.out.println("Failed to get response rfom client");
        }
        return null;
    }
}
