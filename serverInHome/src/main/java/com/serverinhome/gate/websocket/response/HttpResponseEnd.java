/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.websocket.response;

import java.nio.ByteBuffer;


public class HttpResponseEnd extends AccessResponse {
    public HttpResponseEnd(ByteBuffer bBuf) {
        super(ResponseType.httpEnd, bBuf);
    }    
}
