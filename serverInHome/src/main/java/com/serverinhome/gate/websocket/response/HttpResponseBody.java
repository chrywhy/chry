/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.websocket.response;

import java.nio.ByteBuffer;


public class HttpResponseBody extends AccessResponse {
    public HttpResponseBody(ByteBuffer bBuf) {
        super(ResponseType.httpBody, bBuf);
    }    
}
