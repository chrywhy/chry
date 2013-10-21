package com.serverinhome.gate;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 *
 * @author Wang Huiyu
 */
public class ActiveClients {
    private static Map<String, Session> _clients = new ConcurrentHashMap<String, Session>();
    public ActiveClients() {
    }
    
    public static void add(Session client) {
        Map<String, Object> userProp = client.getUserProperties();
        String user = userProp.get("user").toString();
        _clients.put(user, client);
    }
    
    public static Session get(String user) {
        return _clients.get(user);
    }

    public static void remove(String user) {
        _clients.get(user);
    }

    public static void remove(Session client) {
        Map<String, Object> userProp = client.getUserProperties();
        String user = userProp.get("user").toString();
        _clients.get(user);
    }
}
