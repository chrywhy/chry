package com.serverinhome.gate.websocket;

import com.serverinhome.gate.ActiveClients;
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
    public void onOpen(Session client) throws IOException, EncodeException {
        System.out.print("################# open -");
        ActiveClients.add(client);
        client.getBasicRemote().sendText("Hi, websocket is open");
    }

    @OnClose
    public void onClose(Session client) {
        ActiveClients.remove(client);
    }

    @OnMessage
    public void onMessage(String message, Session client) throws IOException, EncodeException {
            System.out.print("################# message = " + message);
            client.getBasicRemote().sendText("Hi, I'm gate server");
    }
}
