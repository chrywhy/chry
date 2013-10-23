package com.serverinhome.home;

/**
 * HomeAgent
 *
 */
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeAgent implements Runnable {
    private GateClientEndpoint _gateClient;
    public HomeAgent(String userName) {
        try {
            _gateClient = new GateClientEndpoint(userName);
        } catch (Exception e)  {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void run() {
        System.out.println("HomeAgent started");
    }
    
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        HomeAgent homeAgent = new HomeAgent(args[0]);
        executor.execute(homeAgent);
    }
}
