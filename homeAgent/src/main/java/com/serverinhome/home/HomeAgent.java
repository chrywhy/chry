package com.serverinhome.home;

/**
 * HomeAgent
 *
 */
import java.net.URI;

public class HomeAgent 
{
    public static void main(String[] args) throws Exception {

        final AgentClientEndpoint clientEndPoint = new AgentClientEndpoint(new URI("ws://localhost:8080/"));
        clientEndPoint.addMessageHandler(new AgentClientEndpoint.MessageHandler() {
                    public void handleMessage(String message) {
                            System.out.println(message);
                    }
                });
 
        while (true) {
            clientEndPoint.sendMessage("Hi, I'm home agent for user: chry");
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
