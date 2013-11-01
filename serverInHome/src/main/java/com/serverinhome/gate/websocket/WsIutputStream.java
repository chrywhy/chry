/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;

/**
 *
 * @author chry
 */
public class WsIutputStream extends InputStream {
    private static class Data {
        byte b;
        boolean eof;
        private Data(byte b, boolean eof) {
            this.b = b;
            this.eof = eof;
        }
        public static Data newData(byte b) {
            return new Data(b, true);
        }
        public static Data newEof() {
            return new Data((byte)0, false);
        }
    }
    private BlockingQueue<Data> _buffer;
    private final Session _ws;
    
    public WsIutputStream(Session ws) {
        _ws = ws;
        _buffer = new LinkedBlockingQueue<Data>();
    }
    
    @Override
    public int available() {
        return _buffer.size();
    }

    @Override
    public int read() throws IOException {
        byte[] bytes = new byte[4];
        int n = 0;
        for (int i = 0; i < 4; i++) {
            Data d;
            try {
                d = _buffer.take();
                if (d.eof) {
                    throw new IOException("EOF");
                } else {
                    n = n & d.b << ((3-i) * 8);
                }
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }
        return n;
    }
        
    @Override
    public void close() throws IOException {
        super.close();
    }
        
    private int _addBufData(int n) {
        if (_bufSize < BUF_SIZE) {
            int2byte(n, _buffer, _bufSize);
            _bufSize += 4;
        }
        return _bufSize;
    }
    
    private int _addBufData(byte b) {
        if (_bufSize < BUF_SIZE) {
            _buffer[_bufSize++] = b;
        }
        return _bufSize;
    }    
}
