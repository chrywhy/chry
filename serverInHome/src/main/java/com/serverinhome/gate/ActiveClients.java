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
        String name = userProp.get("name").toString();
        _clients.put(name, client);
    }
    
    public static void get(String name) {
        _clients.get(name);
    }

    public static void remove(String name) {
        _clients.get(name);
    }

    public static void remove(Session client) {
        Map<String, Object> userProp = client.getUserProperties();
        String name = userProp.get("name").toString();
        _clients.get(name);
    }
}
