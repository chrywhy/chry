package com.serverinhome.home;

/**
 * HomeAgent
 *
 */
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import org.glassfish.tyrus.client.ClientManager;
        
public class HomeAgent 
{
    private static CountDownLatch latch;
    
    public static void main(String[] args) throws Exception {

        final AgentClientEndpoint clientEndPoint = new AgentClientEndpoint(new URI("ws://localhost:8080/edit/"));
        clientEndPoint.addMessageHandler(new AgentClientEndpoint.MessageHandler() {
                    public void handleMessage(String message) {
                            System.out.println(message);
                    }
                });
 
        while (true) {
            clientEndPoint.sendMessage("Hi, I'm home agent");
            Thread.sleep(30000);
        }

/*        latch = new CountDownLatch(1);
 
        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(AgentClientEndpoint.class, new URI("ws://localhost:8080/edit"));
            latch.await(); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
*/ 
    }
}
