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
import com.serverinhome.util.http.HttpClients;
import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;
import com.serverinhome.util.http.websocket.WebsocketClient;
import com.serverinhome.util.http.websocket.WsOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ChatServer Client
 *
 * @author Huiyu Wang
 */
public class GateClient extends WebsocketClient {
    public enum ResponseType {
        unknown((byte)0), ping((byte)1), pong((byte)2), httpHead((byte)3), httpBody((byte)4), httpEnd((byte)5);
        byte b;
        ResponseType(byte b) {
            this.b = b;
        }
        
        public byte getByte() {
            return b;
        }
        
        public static ResponseType get(byte b) {
            ResponseType[] vals = ResponseType.values();
            for(ResponseType t : vals) {
                if (t.b == b) {
                    return t;
                }
            }
            return unknown;
        }
    };    
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
    private final WsOutputStream _ws;

    public static GateClient create(String userName) {
        try {
            URI gateUri = new URI("wss://localhost:8181/websocketConnector/" + userName);
            return new GateClient(userName, gateUri);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private GateClient(String userName, URI uri) {
        super(uri);
        _userName = userName;
        _ws = new WsOutputStream(this);
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
                message = URLDecoder.decode(jBody.getString("url"), "UTF-8");
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
                    System.out.println("send http request - " + url);
                    HttpClient httpClient = HttpClients.createApacheHttpInstance();
                    HttpResponseStream hrs = httpClient.get(url);
//                    sendHttpMessage(hrs);
                    sendHttpResponse(hrs);
                    System.out.println("send response for url - " + url);
//                    String rspMsg = hrs.decodeToString();
//                    sendMessage(rspMsg);
                } else {
                    sendDefMessage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void sendHttpResponse(HttpResponseStream hrs) throws Exception {
        HttpClient hc = HttpClients.createApacheHttpInstance();
        String url = "http://localhost:8080/response?user=chry&requestId=1";
        HttpPostStream hps = new HttpPostStream(hrs);
        hc.post(url, hps);
    }

    public static void int2bytes(int n, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((n >> 24) & 0xff);
        bytes[offset + 1] = (byte) ((n >> 16) & 0xff);
        bytes[offset + 2] = (byte) ((n >> 8) & 0xff);
        bytes[offset + 3] = (byte) (n & 0xff);
    } 
    
    public static void long2bytes(long n, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((n >> 56) & 0xff);
        bytes[offset + 3] = (byte) ((n >> 48) & 0xff);
        bytes[offset + 4] = (byte) ((n >> 40) & 0xff);
        bytes[offset + 5] = (byte) ((n >> 32) & 0xff);
        bytes[offset + 6] = (byte) ((n >> 24) & 0xff);
        bytes[offset + 1] = (byte) ((n >> 16) & 0xff);
        bytes[offset + 2] = (byte) ((n >> 8) & 0xff);
        bytes[offset + 7] = (byte) (n & 0xff);
    } 
    
    public int sendHttpHead(HttpResponseStream hrs, int index) {
        byte[] contentType = hrs.getContentType().getBytes();  
        String encodingStr = hrs.getContentEncoding();
        if (encodingStr == null) {
            encodingStr = "";
        }
        byte[] encoding = encodingStr.getBytes();
        byte[] bData = new byte[1 + 4 + 4 + 4 + 4 + contentType.length + 4 + encoding.length + 8];
        int i = 0;
        bData[i] = ResponseType.httpHead.getByte();     //type
        i++;                                            //requestId  `
        int2bytes(0, bData, i);
        i += 4;                                         //index
        int2bytes(index++, bData, i);
        i += 4;
        int2bytes(hrs.getResponseCode(), bData, i);         //statusCode
        i += 4;
        int2bytes(contentType.length, bData, i);            //contentType length
        i += 4;
        for (int k = 0; k < contentType.length; k++, i++) {     //contentType
            bData[i] = contentType[k];
        }
        int2bytes(encoding.length, bData, i);                   //encoding length
        i += 4;
        for (int k = 0; k < encoding.length; k++, i++) {        //encoding
            bData[i] = encoding[k];
        }
        long2bytes(hrs.getContentLength(), bData, i);            //contentType length
        send(bData);
        return index;
//        send("Hello");
    }
    
    public int sendHttpBody(HttpResponseStream hrs, int index) {
        boolean eof = false;
        while(!eof) {
            byte[] bData = new byte[1024];
            int i = 0;
            bData[i] = ResponseType.httpBody.getByte();     //type
            i++;                                            //requestId  `
            int2bytes(0, bData, i);
            i += 4;                                         //index
            int2bytes(index++, bData, i);
            i += 4;
            int size;
            try {
                size = hrs.writeToBuffer(bData, i);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("########## read size: " + size);
            if (size < 1024) {
                byte[] nData = new byte[size];
                for (int n = 0; n < size; n++) {
                    nData[n] = bData[n];
                }
                bData = nData;
                eof = true;
            }
            send(bData);
        }
        return index;
    }
    
     public int sendHttpEnd(HttpResponseStream hrs, int index) {
        byte[] bData = new byte[9];
        int i = 0;
        bData[i] = ResponseType.httpEnd.getByte();     //type
        i++;                                            //requestId  `
        int2bytes(0, bData, i);
        i += 4;                                         
        int2bytes(index++, bData, i);                        //index = -1, means end of response
        send(bData);    //send end of response message
        return index;
     }

    public void sendHttpMessage(HttpResponseStream hrs) {
        int index = sendHttpHead(hrs, 0);
        index = sendHttpBody(hrs, index);
        index = sendHttpEnd(hrs, index);
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
        send(rspMsg);
    }
}