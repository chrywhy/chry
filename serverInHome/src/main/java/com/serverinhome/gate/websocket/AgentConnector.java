package com.serverinhome.gate.websocket;

import com.serverinhome.gate.ActiveCsrs;
import com.serverinhome.gate.Csr;
import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/agentConnector")
public class AgentConnector {

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        System.out.print("################# open -");
        ActiveCsrs.add(new Csr(session));
        session.getBasicRemote().sendText("Hi, websocket is open");
    }

    @OnClose
    public void onClose(Session session) {
        ActiveCsrs.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session client) throws IOException, EncodeException {
            System.out.print("################# message = " + message);
            client.getBasicRemote().sendText("Hi, I'm gate server");
    }
}
