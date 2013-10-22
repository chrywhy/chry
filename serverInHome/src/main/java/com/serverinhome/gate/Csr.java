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
    private final Session _session;
    private final CountDownLatch _doneLatch = new CountDownLatch(1);
    
    public Csr(Session session) {
        _session = session;
    }
    
    public Session getSession() {
        return _session;
    }
}
