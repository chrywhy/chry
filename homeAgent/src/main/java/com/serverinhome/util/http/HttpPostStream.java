/***********************************************
 *  When send HTTP request(get/post), server will return response,
 *  The response could be different contend encoding and type, and it could
 *  have upload/download binary file, images. But there is common thing that all
 *  these content could be got from InputStream of the connection. So, HttpInputStream
 *  is a wrapper class for such InputStream, with help of HttpInputStream, HttpService
 *  needn't care what type of content in the get/post request or response, HttpService
 *  become simple to handle any type of get/post request or response
 *************************************************/

package com.serverinhome.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.apache.http.Header;

public class HttpPostStream {
    protected final boolean _hasAlreadyEncoded;
    protected final InputStream _inputStream;
    protected final Map<String, String> _headers = new HashMap<>();

    public HttpPostStream(InputStream is, String contentEncoding, String contentType, boolean hasAlreadyEncoded) {
        _headers.put("Content-Encoding", contentEncoding);
        _headers.put("Content-Type", contentType);
        _inputStream = is;
        _hasAlreadyEncoded = hasAlreadyEncoded;
    }
    
    public HttpPostStream(HttpResponseStream hrs) {
        _headers.put("userStatusCode", "" + hrs.getResponseCode());
        for (Header h : hrs.getHeaders()) {
            _headers.put("user" + h.getName(), h.getValue());
        }
        _headers.put("userContent-Length", "" + hrs.getContentLength());
        _inputStream = hrs.getInputStream();
        _hasAlreadyEncoded = true;
    }
    
    public InputStream getInputStream() {
        return _inputStream;
    }
    
    public Map<String, String> getHeaders() {
        return _headers;
    }
    
    public String getContentEncoding() {
        return _headers.get("Content-Encoding");
    }

    public String getContentType() {
        return _headers.get("Content-Type");
    }

    public long getContentLength() {
        try {
            String lenStr = _headers.get("Content-Length");
            long len;
            len = Integer.parseInt(lenStr);
            return len;
        } catch(Exception e) {
            return -1;
        }
    }
    
    private boolean _needGzip() {
        return !_hasAlreadyEncoded && "gzip".equalsIgnoreCase(getContentEncoding());
    }

    public long encodeToStream(OutputStream outputStream) throws IOException {
        return encodeToStream(outputStream, null);
    }

    public long encodeToStream(OutputStream outputStream, ProgressListener listener) throws IOException {
        OutputStream os = outputStream;
        if (_needGzip()) {
            os = new GZIPOutputStream(outputStream);
        }
        return StreamUtil.inputStreamToOutputStream(_inputStream, os, listener);
    }

    public long writeToStream(OutputStream os) throws IOException {
        return StreamUtil.inputStreamToOutputStream(_inputStream, os, null);
    }
}
