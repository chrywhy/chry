/*
*****************************************************************************************************************
* Module Introduction:
* dispatch all collectors's request to santaba server 
*
* Wang Huiyu   2012/07/17	Initial Version			
*****************************************************************************************************************
*/

package com.serverinhome.gate.servlet;

import com.serverinhome.common.logger.LogMsg;
import com.serverinhome.proxy.client.HomeServerClient;
import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;
import com.serverinhome.util.http.HttpService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;

@WebServlet(name="DispatchToAgent", urlPatterns={"/dispatch"})
public class DispatchToAgent extends BaseProxyServlet {
    private static final long serialVersionUID = 1L;

    protected HttpResponseStream _handleRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            boolean isPost)
            throws IOException, ServletException {
        String userName = _getParameterString(request, "user").toLowerCase();

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String url = uri + "?" + query;
        LogMsg.info("Forward request", "URL=" + url);        
        if (userName.isEmpty()) {
            LogMsg.warn("Invalid forward request OK", "URL=" + url);
            throw new ServletException("Invlid Forward Reuest");
        }

        HomeServerClient hsClient;
        try {
            hsClient = new HomeServerClient();
        }
        catch (Exception e) {
            LogMsg.error("Csproxy intrnal error", "URL=" + url, e);
            throw new ServletException("Csproxy intrnal error", e);
        }

        HttpResponseStream respStream;
        try {
            if (isPost) {
                HttpPostStream hps = _getHttpPostStream(request);
                respStream = hsClient.post(userName, url, hps);
            }
            else {
                respStream = hsClient.get(userName, url);
            }
            LogMsg.debug("Forward request OK", "URL=" + url);
        }
        catch (IOException e) {
            LogMsg.warn("Forward request Fail", "URL=" + url, e);
            throw e;
        }
        return respStream;
    }
}
