/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.websocket.response;

import java.nio.ByteBuffer;

/**
 *
 * @author chry
 */
public abstract class AccessResponse {
    public enum ResponseType {
        unknown((byte)0), ping((byte)1), pong((byte)2), httpHead((byte)3), httpBody((byte)4), httpEnd((byte)5);
        byte b;
        ResponseType(byte b) {
            this.b = b;
        }
        
        public byte getByte() {
            return b;
        }
        
        public static ResponseType get(byte b) {
            ResponseType[] vals = ResponseType.values();
            for(ResponseType t : vals) {
                if (t.b == b) {
                    return t;
                }
            }
            return unknown;
        }
    };
    public final int requestId;
    public int index;
    public ResponseType type; 

    private ByteBuffer bBuf;
    
            
    protected AccessResponse(ResponseType type, ByteBuffer bBuf) {
        this.type = type;
        this.requestId = bBuf.getInt();
        this.index = bBuf.getInt();
        this.bBuf = bBuf;
    }

    public static AccessResponse CreateAccessResponse(ByteBuffer bBuf) {
        ResponseType type = ResponseType.get(bBuf.get());
        AccessResponse ar;
        switch (type) {
            case httpHead:
                ar = new HttpResponseHead(bBuf);
                break;
            case httpBody:
                ar = new HttpResponseBody(bBuf);
                break;
            case httpEnd:
                ar = new HttpResponseEnd(bBuf);
                break;
            case pong:
            default:
                throw new RuntimeException("unsupported response type - " + type.b);
        }
        System.out.println("################### " + ar.index + "-" + type);
        return ar;
    }
    
    public int getByte2Int() {
        byte b = bBuf.get();
        return 0xff & b;
    }
}
