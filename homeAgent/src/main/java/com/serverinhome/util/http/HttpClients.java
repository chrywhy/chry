/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.util.http;

/**
 *
 * @author chry
 */
public class HttpClients {
    private HttpClients() {}
    public static JdkHttpClient getJdkHttpInstance() throws Exception {
        return JdkHttpClient.getInstance();
    }

    public static ApacheHttpClient createApacheHttpInstance() throws Exception {
        return new ApacheHttpClient();
    }
}
