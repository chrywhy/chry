/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.agent;

/**
 *
 * @author chry
 */
import com.serverinhome.util.http.HttpClient;
import com.serverinhome.util.http.HttpResponseStream;
import com.serverinhome.util.http.websocket.WebsocketClient;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

/**
 * ChatServer Client
 *
 * @author Huiyu Wang
 */
public class GateClient extends WebsocketClient {

    private final String _userName;

    public static GateClient create(String userName) {
        try {
            URI gateUri = new URI("wss://localhost:8181/agentConnector/" + userName);
            return new GateClient(userName, gateUri);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private GateClient(String userName, URI uri) {
        super(uri);
        _userName = userName;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Message from gate server:" + message);
        try {
            JSONObject jMsg = new JSONObject(message);
            JSONObject jHead = jMsg.getJSONObject("head");
            JSONObject jBody = jMsg.getJSONObject("body");
            String msgType = jHead.getString("msgType");
            if ("request".equalsIgnoreCase(msgType)) {
                message = jBody.getString("url");
                HttpClient httpClient = new HttpClient();
                int urlPos = message.indexOf("url=");
                String url = "";
                if (urlPos >= 0) {
                    urlPos += 4;
                    int urlEnd = message.indexOf("&", urlPos);
                    if (urlEnd < 0) {
                        url = message.substring(urlPos);
                    } else {
                        url = message.substring(urlPos, urlEnd);
                    }
//                    sendMessage("#############################");
                    HttpResponseStream hrs = httpClient.get(url);
                    String rspMsg = hrs.decodeToString();
                    System.out.println(rspMsg);
                    sendMessage(rspMsg);
                } else {
                    sendMessage("Hello, I'm home agent:" + _userName);
                }
            }
        } catch (Exception e) {
            sendMessage("Hello, I'm home agent:" + _userName);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected");
        System.exit(0);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void sendMessage(String message) {
        send(message);
    }

    public void sendResult(HttpResponseStream hrs) throws IOException {
        String rspMsg = hrs.decodeToString();
        System.out.println(rspMsg);
        sendMessage(rspMsg);
    }
}