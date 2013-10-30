/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.util.http.websocket;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author chry
 */
public class WebsocketStream extends OutputStream {
    private static int BUF_SIZE = 1024;
    private byte[] _buffer = new byte[BUF_SIZE];
    private int _bufSize = 0;
    private final WebsocketClient _ws;
    
    public WebsocketStream(WebsocketClient ws) {
        _ws = ws;
    }
    
    @Override
    public void write(int n) throws IOException {
        if (_bufSize + 4 > BUF_SIZE) {
            _sendBuf();
        }
        _addBufData(n);
    }
    
    @Override
    public void close() throws IOException {
        if (_bufSize > 0) {
            _sendBuf();
        }
        super.close();
    }
    
    private void _clearBuf() {
        _bufSize = 0;
    }

    private void _sendBuf() {
        byte[] data = _buffer;
        if (_bufSize < BUF_SIZE) {
            data = new byte[_bufSize];
            for (int i = 0; i < _bufSize; i++) {
                data[i] = _buffer[i];
            }
        }
        _ws.send(data);
        _bufSize = 0;
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
    
    public static byte[] int2byte(int n) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (n & 0xff);
        bytes[1] = (byte) ((n >> 8) & 0xff);
        bytes[2] = (byte) ((n >> 16) & 0xff);
        bytes[3] = (byte) (n >>> 24);
        return bytes; 
    } 
    
    public static void int2byte(int n, byte[] bytes, int offset) {
        byte[] targets = new byte[4];

        bytes[offset] = (byte) (n & 0xff);
        bytes[offset + 1] = (byte) ((n >> 8) & 0xff);
        bytes[offset + 2] = (byte) ((n >> 16) & 0xff);
        bytes[offset + 3] = (byte) (n >>> 24);
    } 
}
