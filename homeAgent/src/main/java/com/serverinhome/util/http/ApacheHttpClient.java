/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.util.http;

import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;

/**
 *
 * @author chry
 */
public class ApacheHttpClient implements HttpClient {
    private CloseableHttpClient _browser;
    public ApacheHttpClient() {
        HttpClientBuilder hc = HttpClientBuilder.create();
        hc.useSystemProperties();
        _browser = hc.build();        
    }
    
    @Override
    public HttpResponseStream get(String sUrl) throws IOException {
        HttpUriRequest req = new HttpGet(sUrl);
        HttpResponse rsp;
        rsp = _browser.execute(req);
        HttpResponseStream hrs = new HttpResponseStream(rsp);
        return hrs;
    }    

    @Override
    public HttpResponseStream post(String sUrl, HttpPostStream hps) throws IOException {
        HttpPost hp = new HttpPost(sUrl);
        InputStreamEntity entity = new InputStreamEntity(hps.getInputStream(), hps.getContentLength());
        Map<String, String> headers = hps.getHeaders();
        for (String key : headers.keySet()) {
            hp.addHeader(key, headers.get(key));
        }
        entity.setContentType(hps.getContentType());
        entity.setContentEncoding(hps.getContentEncoding());
        hp.setEntity(entity);
        HttpResponse rsp = _browser.execute(hp);
        HttpResponseStream hrs = new HttpResponseStream(rsp);
        return hrs;
    }
}
