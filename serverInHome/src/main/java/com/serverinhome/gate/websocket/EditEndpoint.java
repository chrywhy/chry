package com.serverinhome.gate.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/edit")
public class EditEndpoint {

    private static Set<Session> peers = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
    private static String _text = "";

    @OnOpen
    public void onOpen(Session client) throws IOException, EncodeException {
        System.out.print("################# open -" + _text);
        peers.add(client);
        client.getBasicRemote().sendText(_text);
    }

    @OnClose
    public void onClose(Session session) {
        peers.remove(session);
    }

    @OnMessage
    public void shapeCreated(String message, Session client) throws IOException, EncodeException {
        _text = message;
        for (Session otherSession : peers) {
            if (!otherSession.equals(client)) {
//                System.out.print("################# message = " + message);
                otherSession.getBasicRemote().sendText(message);
            }
        }
    }
}
