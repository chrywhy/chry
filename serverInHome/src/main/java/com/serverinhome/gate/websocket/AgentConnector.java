package com.serverinhome.gate.websocket;

import com.serverinhome.gate.ActiveCsrs;
import com.serverinhome.gate.Csr;
import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/agentConnector/{user}")
public class AgentConnector {

    @OnOpen
    public void onOpen(Session session, EndpointConfig c, @PathParam("user") String user) throws IOException, EncodeException {
        System.out.print("################# open websocket for user: " + user);
        ActiveCsrs.add(user, new Csr(user, session));
        session.getBasicRemote().sendText("Hi, websocket is open for user: " + user);
    }

    @OnClose
    public void onClose(Session session) {
        ActiveCsrs.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, EncodeException {
        System.out.print("################# message = " + message);
        Csr csr = ActiveCsrs.get(session);
        csr.setResponse(message);
        session.getBasicRemote().sendText("Hi " + csr.getUserName() + ", I'm gate server");
    }
}
