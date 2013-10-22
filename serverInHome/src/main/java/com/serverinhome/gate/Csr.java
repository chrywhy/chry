/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate;

import com.serverinhome.util.http.HttpResponseStream;
import java.util.concurrent.CountDownLatch;
import javax.websocket.Session;

/**
 *
 * @author chry
 */
public class Csr {
    private final String _user;
    private final Session _session;
    private CountDownLatch _doneLatch = new CountDownLatch(1);
    private HttpResponseStream _hrs;
            
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
    
    public void setResponse(String message) {
        try {
            _hrs = new HttpResponseStream(message);
            _doneLatch.countDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public HttpResponseStream getResponse() {
        try {
            _doneLatch.await();
            _doneLatch = new CountDownLatch(1);
            return _hrs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
