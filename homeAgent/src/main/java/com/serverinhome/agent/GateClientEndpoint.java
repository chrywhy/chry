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
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
 
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.grizzly.GrizzlyEngine;
 
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
    private ClientManager _client;
 
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
            URI endpointURI = new URI("wss://localhost:8181/agentConnector/" + userName);
            _client = ClientManager.createClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Session connectToServer(URI uri) {
        try {
            return _client.connectToServer(this, ClientEndpointConfig.Builder.create().build(), uri);
        } catch (DeploymentException ex) {
            throw new RuntimeException(ex);
        }
    }
 
    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };


    /**
     * Trust every server - dont check for any certificate
     */
    public void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }


            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }


            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};


        // Install the all-trusting trust manager
        try {
/*            
            System.getProperties().put(SSLContextConfigurator.TRUST_STORE_PASSWORD, "abc");
            System.getProperties().put(SSLContextConfigurator.TRUST_FACTORY_MANAGER_ALGORITHM, "xyz");
            final SSLContextConfigurator defaultConfig = new SSLContextConfigurator();

            defaultConfig.retrieve(System.getProperties());
                // or setup SSLContextConfigurator using its API.

            SSLEngineConfigurator sslEngineConfigurator =
                new SSLEngineConfigurator(defaultConfig, true, false, false);
            _client.getProperties().put(GrizzlyEngine.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);            

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            client.setSSLContext(sc);
*/
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            HttpClient httpClient = new HttpClient();
            int urlPos = message.indexOf("url=");
            String url = "";
            if (urlPos >=0) {
                urlPos += 4;
                int urlEnd = message.indexOf("&", urlPos);
                if (urlEnd < 0) {
                    url = message.substring(urlPos);
                } else {
                    url = message.substring(urlPos, urlEnd);
                }
                HttpResponseStream hrs = httpClient.get(url);
                String rspMsg = hrs.decodeToString();
                System.out.println(rspMsg);
                sendMessage(rspMsg);
            } else {
                sendMessage("Hello, I'm home agent:" + _userName);
            }
        } catch (Exception e) {
            sendMessage("Hello, I'm home agent:" + _userName);
        }
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