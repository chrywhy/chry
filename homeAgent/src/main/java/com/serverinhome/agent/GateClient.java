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
import com.serverinhome.util.http.websocket.WebsocketStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.NotYetConnectedException;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ChatServer Client
 *
 * @author Huiyu Wang
 */
public class GateClient extends WebsocketClient {
    public enum RspType {
        internal(0), http(1);        
        private final int value;
        RspType(int val) {
            value = val;
        }
        public int getVal() {
            return value;
        }
    }
    
    private final String _userName;
    private final WebsocketStream _ws;

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
        _ws = new WebsocketStream(this);
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
                    sendHttpMessage(hrs);
//                    String rspMsg = hrs.decodeToString();
                    System.out.println("send response for url - " + url);
//                    sendMessage(rspMsg);
                } else {
                    sendDefMessage();
                }
            }
        } catch (Exception e) {
            sendDefMessage();
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

    private void sendMessage(String message) {
        send(message);
    }

    public void sendHttpMessage(HttpResponseStream hrs) {
        try {
            _ws.write(RspType.http.getVal());
            _ws.write(hrs.getResponseCode());
            byte[] encoding = hrs.getContentEncoding().getBytes();
            _ws.write(encoding.length);
            _ws.write(encoding);
            byte[] contentType = hrs.getContentType().getBytes();
            _ws.write(contentType.length);
            _ws.write(contentType);
            _ws.write(hrs.getContentLength());
            hrs.
            jBody.put("message", hrs.getContentString());
            jMsg.put("head", jHead);
            jMsg.put("body", jBody);
            send(jMsg.toString());
        } catch (JSONException | IOException | NotYetConnectedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void sendDefMessage() {
        try {
            JSONObject jMsg = new JSONObject();
            JSONObject jHead = new JSONObject();
            JSONObject jBody = new JSONObject();
            jHead.put("msgType", "default");
            jHead.put("statusCode", 200);
            jHead.put("encodeType", "");
            jHead.put("contentType", "text/plain");
            String message = "Hello, I'm home agent:" + _userName;
            jHead.put("contentLength", message.length());
            jBody.put("message", message);
            jMsg.put("head", jHead);
            jMsg.put("body", jBody);
            send(jMsg.toString());
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void sendResult(HttpResponseStream hrs) throws IOException {
        String rspMsg = hrs.decodeToString();
        System.out.println(rspMsg);
        sendMessage(rspMsg);
    }
}