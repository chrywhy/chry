/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.util.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

/**
 *
 * @author chry
 */
public class ApacheHttpClient implements HttpClient {
    private CloseableHttpClient _httpClient;
    private CloseableHttpClient _httpsClient;
    public ApacheHttpClient() {
        try {
            HttpClientBuilder hc = HttpClientBuilder.create();
            hc.useSystemProperties();
            _httpClient = hc.build();        
            _httpsClient = getTrustAllHttpClient();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ApacheHttpClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(ApacheHttpClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(ApacheHttpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public HttpResponseStream get(String sUrl) throws IOException {
        HttpUriRequest req = new HttpGet(sUrl);
        HttpResponse rsp;
        if (sUrl.toLowerCase().startsWith("https")) {
            rsp = _httpsClient.execute(req);
        } else {
            rsp = _httpClient.execute(req);
        }
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
        HttpResponse rsp;
        if (sUrl.toLowerCase().startsWith("https")) {
            rsp = _httpsClient.execute(hp);
        } else {
            rsp = _httpClient.execute(hp);
        }
        HttpResponseStream hrs = new HttpResponseStream(rsp);
        return hrs;
    }
    
    public CloseableHttpClient getTrustSelfSignedHttpClient() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            HttpClientBuilder hc = HttpClients.custom().setSSLSocketFactory(sslsf);
            return hc.build();            
//            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            HttpClientBuilder hc = HttpClientBuilder.create();
            hc.useSystemProperties();
            return hc.build();
//            return new DefaultHttpClient();
        }
    }    

    public CloseableHttpClient getTrustAllHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustStrategy(){
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
                return true;
            }
        });
        SSLContext sslContext = builder.build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

            @Override
            public void verify(String host, SSLSocket ssl) throws IOException {
            }

            @Override
            public void verify(String host, X509Certificate cert) throws SSLException {
            }

            @Override
            public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
            }

            @Override
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
            
        });   
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                           .register("https", sslsf)
                           .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClientBuilder hc = HttpClients.custom().setConnectionManager(cm);
//        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
//        HttpClientBuilder hc = HttpClients.custom().setSSLSocketFactory(sslsf);
        return hc.build();
    }    
}
