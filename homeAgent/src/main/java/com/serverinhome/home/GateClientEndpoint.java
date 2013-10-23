/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.home;

/**
 *
 * @author chry
 */
import java.net.URI;
import java.util.Map;
 
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
 
/**
 * ChatServer Client
 * 
 * @author Huiyu Wang
 */
@ClientEndpoint
public class GateClientEndpoint {
    Session userSession = null;
    private MessageHandler messageHandler;
    private final String _userName;
 
    public GateClientEndpoint(String userName) {
/*        try {
            WebSocketContainer container = ContainerProvider
                    .getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
*/
        _userName = userName;
        try {
            URI endpointURI = new URI("ws://localhost:8080/agentConnector/" + userName);
            ClientManager client = ClientManager.createClient();
            Map<String, Object> prop = client.getProperties();
            prop.put("user", "chry");
            client.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Callback hook for Connection open events.ik   .
/./     * 
     * @param userSession
     *            the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }
 
    /**
     * Callback hook for Connection close events.
     * 
     * @param userSession
     *            the userSession which is getting closed.
     * @param reason
     *            the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }
 
    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message.
     * 
     * @param message
     *            The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
        System.out.println("Message from gate server:" + message);
        sendMessage("Hello, I'm home agent:" + _userName);
    }
 
    /**
     * register message handler
     * 
     * @param message
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }
 
    /**
     * Send a message.
     * 
     * @param user
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }
 
    /**
     * Message handler.
     * 
     * @author Huiyu Wang
     */
    public static interface MessageHandler {
        public void handleMessage(String message);
    }
}