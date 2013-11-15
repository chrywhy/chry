package com.serverinhome.gate.servlet;

import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServlet;

public abstract class BaseProxyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected abstract HttpResponseStream _handleRequest(
            HttpServletRequest paramHttpServletRequest,
            HttpServletResponse paramHttpServletResponse,
            boolean isPost)
            throws IOException, ServletException;

    protected void _forward(HttpServletRequest request, HttpServletResponse response, String uri)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(uri);
        dispatcher.forward(request, response);
    }

    private static void setResponse(HttpServletResponse response, HttpResponseStream httpRespStream) throws IOException {
        if (httpRespStream != null) {
            String contentType = httpRespStream.getContentType();
            response.setContentType(contentType);
            response.setContentLength((int)httpRespStream.getContentLength());
            OutputStream servletRespStream = response.getOutputStream();
            httpRespStream.decodeToStream(servletRespStream);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpResponseStream repStream = null;
        repStream = _handleRequest(request, response, false);
        setResponse(response, repStream);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpResponseStream repStream = null;
        repStream = _handleRequest(request, response, true);
        setResponse(response, repStream);
    }

    protected HttpPostStream _getHttpPostStream(HttpServletRequest request) throws IOException {
        InputStream is = request.getInputStream();
        String contentType = request.getContentType();
        String contentEncoding = request.getHeader("Content-Encoding");
        return new HttpPostStream(is, contentEncoding, contentType, true);
    }
}
