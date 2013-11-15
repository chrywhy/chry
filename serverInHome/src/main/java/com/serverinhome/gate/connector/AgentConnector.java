/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.connector;

import com.serverinhome.common.logger.LogMsg;
import com.serverinhome.gate.servlet.ServletUtil;
import com.serverinhome.proxy.client.AgentClient;
import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;
import com.serverinhome.util.http.StreamUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author chry
 */
public class AgentConnector {
    private final HttpServletRequest _userRequest;
    private final HttpServletResponse _userResponse;
    private final String _user;
    private int _id;
    private CountDownLatch _countDownLatch;
    private HttpServletRequest _agentRequest;
    PipedOutputStream _pos;
    PipedInputStream _pis;
    
    protected AgentConnector(HttpServletRequest request, HttpServletResponse response) throws IOException {
        _userRequest = request;
        _userResponse = response;
        _user = ServletUtil.getParameterString(_userRequest, "user").toLowerCase();
        _id = ServletUtil.getParameterInt(request, "requestId");
        _agentRequest = null;
        _pos = new PipedOutputStream();
        _pis = new PipedInputStream();
        _pos.connect(_pis);
    }
    
    protected void setAgentRequest(HttpServletRequest request) {
        _agentRequest = request;
    }
    
    public void connectToAgent(boolean isPost) throws IOException {
        String uri = _userRequest.getRequestURI();
        String query = "";
        try {
            query = URLDecoder.decode(_userRequest.getQueryString(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            //TODO
        }
        String url = uri + "?" + query;
        LogMsg.info("Forward request", "URL=" + url);        
        sendRequestToAgent(false);
        StreamUtil.inputStreamToOutputStream(_pis, _userResponse.getOutputStream());
    }

    public void connectToUser(boolean isPost) throws IOException {
        if (_agentRequest == null) {
            throw new RuntimeException("Invalid resposne from agent");
        }
        int statusCode = Integer.parseInt(_agentRequest.getHeader("userStatusCode"));
        String contentType = _agentRequest.getHeader("userContent-Type");
        String encodeType = _agentRequest.getHeader("userContent-Encoding");
        String characterEncoding = _agentRequest.getHeader("userCharacter-Encoding");
        int contentLength = Integer.parseInt(_agentRequest.getHeader("userContent-Length"));
        _userResponse.setStatus(statusCode);
        if (!(contentType ==null || contentType.isEmpty())) {
            _userResponse.setContentType(contentType);
        }
        if (!(encodeType ==null || encodeType.isEmpty())) {
            _userResponse.setHeader("Character-Encoding", encodeType);
        }
        if (!(characterEncoding ==null || characterEncoding.isEmpty())) {
            _userResponse.setCharacterEncoding(characterEncoding);
        }
        _userResponse.setContentLengthLong(_agentRequest.getContentLengthLong());
        OutputStream os = _userResponse.getOutputStream();
        InputStream is = _agentRequest.getInputStream();
        String contentEncoding = _agentRequest.getHeader("Content-Encoding");
        if (contentEncoding != null) {
            _userResponse.setHeader("Content-Encoding", contentEncoding);
        }
        StreamUtil.inputStreamToOutputStream(_agentRequest.getInputStream(), _pos);
    }
    
    protected HttpServletRequest getRequest() {
        return _userRequest;
    }
    
    private void sendRequestToAgent(boolean isPost) {
        String userName = ServletUtil.getParameterString(_userRequest, "user").toLowerCase();
        String uri = _userRequest.getRequestURI();
        String query = _userRequest.getQueryString();
        String url = uri + "?" + query;
        LogMsg.info("Forward request", "URL=" + url);        
        if (userName.isEmpty()) {
            LogMsg.warn("Invalid forward request OK", "URL=" + url);
            throw new RuntimeException("Invlid Forward Reuest");
        }
        AgentClient hsClient;
        hsClient = new AgentClient();
        if (isPost) {
            //TODO
        }
        else {
            hsClient.sendRequest(userName, url);
        }
        LogMsg.debug("Forward request OK", "URL=" + url);
    }

}
