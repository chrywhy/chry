/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate;

import com.serverinhome.gate.websocket.response.AccessResponse;
import static com.serverinhome.gate.websocket.response.AccessResponse.ResponseType.httpBody;
import com.serverinhome.gate.websocket.response.AccessResponseInputStream;
import com.serverinhome.gate.websocket.response.HttpResponseBody;
import com.serverinhome.gate.websocket.response.HttpResponseHead;
import com.serverinhome.util.http.HttpResponseStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import javax.websocket.Session;

/**
 *
 * @author chry
 */
public class Csr {
    private final String _user;
    private final Session _session;
    private final BlockingQueue<AccessResponse> msgQueue = new LinkedBlockingQueue<AccessResponse>();
    private AccessResponseInputStream _arIs;
    
    public Csr(String user, Session session) {
        _user = user;
        _session = session;
        _arIs = new AccessResponseInputStream(msgQueue);
    }
    
    public String getUserName() {
        return _user;
    }
    
    public Session getSession() {
        return _session;
    }
    
   public void addResponse(AccessResponse rsp) {
       try {
            msgQueue.add(rsp);
            System.out.println("################ add rsp new data buffer");
       } catch (IllegalStateException e) {
           System.err.println("msgQueue full - " + msgQueue.size());
       }
   }
   
    private AccessResponse getResponse() {
        try {
            return msgQueue.take();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public HttpResponseStream getResponseStream() {
        HttpResponseStream hrs = null;
        HttpResponseHead head = null;
        HttpResponseBody body = null;
        while(true) {
            AccessResponse ar = getResponse();
            switch (ar.type) {
                case httpHead:
                    head = (HttpResponseHead) ar;
                    return new HttpResponseStream(head, _arIs);
                case pong:
                case httpBody:
                default:
            }
        } 
    }
}
