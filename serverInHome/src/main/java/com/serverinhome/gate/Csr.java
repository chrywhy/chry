/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate;

import java.util.concurrent.CountDownLatch;
import javax.websocket.Session;

/**
 *
 * @author chry
 */
public class Csr {
    private final String _user;
    private final Session _session;
    private final CountDownLatch _doneLatch = new CountDownLatch(1);
    
    public Csr(String user, Session session) {
        _user = user;
        _session = session;
    }
    
    public String getUserName() {
        return _user;
    }
    
    public Session getSession() {
        return _session;
    }
}
