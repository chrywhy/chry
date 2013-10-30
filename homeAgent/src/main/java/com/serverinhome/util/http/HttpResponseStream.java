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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

public class HttpResponseStream {

    private final InputStream _inputStream;
    private final InputStream _errorStream;
    private final String _contentEncoding;
    private final String _contentType;
    private final int _contentLength;
    private final int _statucCode;

    public HttpResponseStream(String message) throws HttpException, IOException {
        _statucCode = 200;
        _inputStream = new ByteArrayInputStream(message.getBytes());;
        _errorStream = null;
        _contentEncoding = "";
        _contentType = "text/plain";
        _contentLength = message.length();
//        Logger.info2("#######2####### encode: " + _contentEncoding + "  ##  contentTyep: " + _contentType);
    }

    public HttpResponseStream(HttpURLConnection urlConn) throws HttpException, IOException {
        if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new HttpException(urlConn.getResponseCode());
        }
        _inputStream = urlConn.getInputStream();
        _errorStream = urlConn.getErrorStream();
        _contentEncoding = urlConn.getContentEncoding();
        _contentType = urlConn.getContentType();
        _contentLength = urlConn.getContentLength();
        _statucCode = urlConn.getResponseCode();
//        Logger.info2("#######2####### encode: " + _contentEncoding + "  ##  contentTyep: " + _contentType);
    }

    public String getContentEncoding() {
        return _contentEncoding;
    }

    public String getContentType() {
        return _contentType;
    }

    public int getContentLength() {
        return _contentLength;
    }

    public int getResponseCode() {
        return _statucCode;
    }

    public boolean isGzip() {
        return "gzip".equalsIgnoreCase(_contentEncoding);
    }

    private InputStream _getDecodedStream() throws IOException {
        InputStream is = _inputStream;
        if (isGzip()) {
            is = new GZIPInputStream(_inputStream);
        }
        return is;
    }

    public String getContentString() throws IOException {
        return StreamUtil.inputStreamToString(_inputStream);
    }
    
    public String decodeToString() throws IOException {
        InputStream is = _getDecodedStream();
        return StreamUtil.inputStreamToString(is);
    }

    public long decodeToStream(OutputStream os) throws IOException {
        return decodeToStream(os, null);
    }

    public long decodeToStream(OutputStream os, ProgressListener listener) throws IOException {
        InputStream is = _getDecodedStream();
        return StreamUtil.inputStreamToOutputStream(is, os, listener);
    }

    public long writeToStream(OutputStream os) throws IOException {
        return writeToStream(os, null);
    }

    public long writeToStream(OutputStream os, ProgressListener listener) throws IOException {
        return StreamUtil.inputStreamToOutputStream(_inputStream, os, listener);
    }

    public String errorToString() throws IOException {
        return StreamUtil.inputStreamToString(_errorStream);
    }
}
