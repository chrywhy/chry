/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.websocket.response;

import java.nio.ByteBuffer;


public class HttpResponseHead extends AccessResponse {
    public final int statusCode;
    public final String contentType;
    public final String encodeType;
    public final long contentLength;
    
    public HttpResponseHead(ByteBuffer bBuf) {
        super(ResponseType.httpHead, bBuf);
        this.statusCode = bBuf.getInt();
        int l = bBuf.getInt();
        byte[] dst = new byte[l];
        bBuf.get(dst, 0, l);
        this.contentType = new String(dst);
        l = bBuf.getInt();
        dst = new byte[l];
        bBuf.get(dst, 0, l);
        this.encodeType = new String(dst);
        this.contentLength = bBuf.getLong();
    }
}
