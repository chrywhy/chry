/*
*****************************************************************************************************************
* Module Introduction:
* dispatch all collectors's request to santaba server 
*
* Wang Huiyu   2012/07/17	Initial Version			
*****************************************************************************************************************
*/

package com.serverinhome.gate.servlet;

import com.serverinhome.gate.connector.AgentConnector;
import com.serverinhome.gate.connector.AgentConnectorManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet(name="ResponseToUser", urlPatterns={"/response"})
public class ResponseToUser extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        AgentConnector ac = AgentConnectorManager.get(request);
        ac.connectToUser(false);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        AgentConnector ac = AgentConnectorManager.get(request);
        ac.connectToUser(true);
    }
}
