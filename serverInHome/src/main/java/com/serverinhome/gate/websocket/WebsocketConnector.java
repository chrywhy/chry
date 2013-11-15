package com.serverinhome.gate.websocket;

import com.serverinhome.gate.websocket.response.AccessResponse;
import com.serverinhome.gate.ActiveCsrs;
import com.serverinhome.gate.Csr;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONException;
import org.json.JSONObject;

@ServerEndpoint("/websocketConnector/{user}")
public class WebsocketConnector {

    @OnOpen
    public void onOpen(Session session, EndpointConfig c, @PathParam("user") String user) throws IOException, EncodeException {
        System.out.print("################# open websocket for user: " + user);
        ActiveCsrs.add(user, new Csr(user, session));
        session.getBasicRemote().sendText(createOpenMessage(user));
    }

    @OnClose
    public void onClose(Session session) {
        ActiveCsrs.remove(session);
    }

    @OnMessage
/*
    public void onMessage(String message, Session session) throws IOException, EncodeException {
        try {
            JSONObject jMsg = new JSONObject(message);
            JSONObject jHead = jMsg.getJSONObject("head");
            JSONObject jBody = jMsg.getJSONObject("body");
            String msgType = jHead.getString("msgType");
            if ("defaultRsp".equalsIgnoreCase(msgType)) {
                System.out.println("######## " + jBody.getString("message"));
            } else {
                System.out.println("######## " + jHead.toString());
            }
            Csr csr = ActiveCsrs.get(session);
//            csr.setResponse(jHead, jBody);
//        session.getBasicRemote().sendText("Hi " + csr.getUserName() + ", I'm gate server");
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
*/
    public void onMessage(ByteBuffer msgBuf, Session session) throws IOException {
        Csr csr = ActiveCsrs.get(session);
        AccessResponse rsp = AccessResponse.CreateAccessResponse(msgBuf);
        csr.addResponse(rsp);
    }
  

    public static String createOpenMessage(String user) {
        try {
            JSONObject jMsg = new JSONObject();
            JSONObject jHead = new JSONObject();
            jHead.put("msgType", "open");
            jHead.put("user", user);
            JSONObject jBody = new JSONObject();
            jBody.put("content", "Hi, websocket is opened");
            jMsg.put("head", jHead);
            jMsg.put("body", jBody);
            return jMsg.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String createDispatchMessage(String user, String url) {
        try {
            JSONObject jMsg = new JSONObject();
            JSONObject jHead = new JSONObject();
            jHead.put("msgType", "request");
            jHead.put("user", user);
            JSONObject jBody = new JSONObject();
            jBody.put("url", url);
            jMsg.put("head", jHead);
            jMsg.put("body", jBody);
            return jMsg.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
}
