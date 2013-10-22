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
    private static Map<String, String> _sid2user = new ConcurrentHashMap<String, String>();
    private static Map<String, String> _user2sid = new ConcurrentHashMap<String, String>();
    private static Map<String, Csr> _sid2Crs = new ConcurrentHashMap<String, Csr>();
    public ActiveCsrs() {
    }
    
    public static void add(String user, Csr csr) {
        Session session = csr.getSession();
        String sid = session.getId();
        _sid2user.put(user, sid);
        _user2sid.put(user, sid);
        _sid2Crs.put(sid, csr);
    }
    
    public static Csr get(String user) {
        String sid = _user2sid.get(user);
        if (sid != null) {
            return _sid2Crs.get(sid);
        }
        return null;
    }

    public static Csr get(Session session) {
        String sid = session.getId();
        if (sid != null) {
            return _sid2Crs.get(sid);
        }
        return null;
    }
    
    public static void remove(String user) {
        String sid = _user2sid.get(user);
        if (sid != null) {
            _sid2Crs.remove(sid);
            _user2sid.remove(user);
        }
    }

    public static void remove(Session session) {
        String sid = session.getId();
        String user = _sid2user.get(sid);
        _sid2user.remove(sid);
        _user2sid.remove(user);
        _sid2Crs.remove(sid);
    }
}
