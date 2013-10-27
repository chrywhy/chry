/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.util.http.websocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

/**
 *
 * @author chry
 */
public class SSLClientExample {
	public static void main( String[] args ) throws Exception {
		WebsocketClient chatclient = new WebsocketClient( new URI( "wss://localhost:8181/agentConnector/why" ));
                chatclient.trustAllHosts();
		chatclient.connectBlocking();

		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String line = reader.readLine();
			if( line.equals( "close" ) ) {
				chatclient.close();
			} else {
				chatclient.send( line );
			}
		}

	}
}
