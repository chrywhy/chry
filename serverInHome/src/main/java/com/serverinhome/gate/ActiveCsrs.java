package com.serverinhome.gate;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 *
 * @author Wang Huiyu
 */
public class ActiveCsrs {
    private static Map<String, Csr> _csrs = new ConcurrentHashMap<String, Csr>();
    public ActiveCsrs() {
    }
    
    public static void add(Csr csr) {
        Session session = csr.getSession();
        Map<String, Object> userProp = session.getUserProperties();
        String user = userProp.get("user").toString();
        _csrs.put(user, csr);
    }
    
    public static Csr get(String user) {
        return _csrs.get(user);
    }

    public static void remove(String user) {
        _csrs.get(user);
    }

    public static void remove(Session session) {
        Map<String, Object> userProp = session.getUserProperties();
        String user = userProp.get("user").toString();
        _csrs.remove(user);
    }
}
