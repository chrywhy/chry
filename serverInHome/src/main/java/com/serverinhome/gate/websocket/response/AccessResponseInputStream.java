/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.websocket.response;

import static com.serverinhome.gate.websocket.response.AccessResponse.ResponseType.httpBody;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author chry
 */
public class AccessResponseInputStream extends InputStream {
    private BlockingQueue<AccessResponse> _msgQueue;
    private AccessResponse _ar = null;
    
    public AccessResponseInputStream(BlockingQueue<AccessResponse> msgQueue) {
        _msgQueue = msgQueue;
    }
    
    @Override
    public int read() throws IOException {
        while (true) {
            if (_ar == null) {
                try {
                    System.out.println("################ beging to get new data buffer......");
                    _ar = _msgQueue.take();
                    System.out.println("################ done.  get new data buffer: " + _ar.index + "-" + _ar.type);
                } catch (InterruptedException ex) {
                    System.out.println("################ error to get new data buffer");
                    throw new IOException(ex);
                }
            }
            switch (_ar.type) {
                case httpHead:
                    System.out.println("###### ignore httpHead package");
                    _ar = null;
                    break;
                case httpBody:
                    try {
                        int b = _ar.getByte2Int();
//                        System.out.println("###### " + b);
                        return b;
                    } catch (BufferUnderflowException e) {
                        _ar = null;
                    }
                case httpEnd:
                    System.out.println("###### end of htpp respoponse");
                    _ar = null;
                    return -1;
                default:
                    _ar = null;
                    throw new RuntimeException("Invalid data");
            }
        }
    }
}
