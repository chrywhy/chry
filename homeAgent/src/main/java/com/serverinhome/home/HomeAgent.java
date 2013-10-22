package com.serverinhome.home;

/**
 * HomeAgent
 *
 */
import java.net.URI;

public class HomeAgent 
{
    public static void main(String[] args) throws Exception {

        final GateClientEndpoint gateClient = new GateClientEndpoint(new URI("ws://localhost:8080/agentConnector/" + args[0]));
        gateClient.addMessageHandler(new GateClientEndpoint.MessageHandler() {
                    public void handleMessage(String message) {
                            System.out.println(message);
                    }
                });
        while (true) {
            gateClient.sendMessage("Hi, I'm home agent for user: " + args[0]);
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
