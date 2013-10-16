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
import com.serverinhome.util.HTMLFilter;
import com.serverinhome.util.http.HttpPostStream;
import com.serverinhome.util.http.HttpResponseStream;
import com.serverinhome.util.http.HttpService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;

@WebServlet(name="DispatchToAgent", urlPatterns={"/dispatch"})
public class DispatchToAgent extends BaseProxyServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    }

    protected HttpResponseStream _handleRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            boolean isPost)
            throws IOException, ServletException {
        String company = _getParameterString(request, "company").toLowerCase();
        int collectorId = _getParameterInt(request, "id");
        String credential = _getParameterString(request, "credential");

        String uri = request.getRequestURI().replaceFirst("^/csproxy/ProxyForward/", "/");
        String query = request.getQueryString();
        String url = "https://" + company + ".logicmonitor.com" + uri + "?" + query;
        LogMsg.info("Forward request", "URL=" + url);
        
        if (company.isEmpty() || collectorId <= 0 || credential.isEmpty()) {
            LogMsg.warn("Invalid forward request OK", "URL=" + url);
            throw new ServletException("Invlid Forward Reuest");
        }

        HttpService httpSrv;
        try {
            httpSrv = HttpService.getInstance();
        }
        catch (Exception e) {
            LogMsg.error("Csproxy intrnal error", "URL=" + url, e);
            throw new ServletException("Csproxy intrnal error", e);
        }

        HttpResponseStream respStream;
        try {
            if (isPost) {
                HttpPostStream hps = _getHttpPostStream(request);
                respStream = httpSrv.post(url, hps);
            }
            else {
                respStream = httpSrv.get(url);
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
