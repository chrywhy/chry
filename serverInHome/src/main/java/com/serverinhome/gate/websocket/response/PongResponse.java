/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.websocket.response;

import java.nio.ByteBuffer;

public class PongResponse extends AccessResponse {    
    public PongResponse(ByteBuffer bBuf) {
        super(ResponseType.pong, bBuf);
    }
}
