package com.serverinhome.agent;

/**
 * HomeAgent
 *
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeAgent implements Runnable {
    private GateClient _gateClient;
    String _userName;
    public HomeAgent(String userName) {
        try {
            _userName =userName;
            _gateClient = GateClient.create(userName);
            _gateClient.trustAllHosts();
        } catch (Exception e)  {
            throw new RuntimeException(e);
        }
    }
        
    @Override
    public void run() {
        try {
            System.out.println("HomeAgent started");
//            _gateClient.connectBlocking();
            _gateClient.run();
        } catch (Exception ex) {
            Logger.getLogger(HomeAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        HomeAgent homeAgent = new HomeAgent(args[0]);
        executor.execute(homeAgent);
    }
}
