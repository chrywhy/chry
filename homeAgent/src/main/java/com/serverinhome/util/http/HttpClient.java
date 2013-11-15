package com.serverinhome.util.http;

import java.io.IOException;

public interface HttpClient {
    public HttpResponseStream get(String sUrl) throws IOException;
    public HttpResponseStream post(String sUrl, HttpPostStream hps) throws IOException;
}
