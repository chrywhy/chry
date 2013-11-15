/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.serverinhome.gate.connector;

import com.serverinhome.gate.servlet.ServletUtil;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author chry
 */
public abstract class AgentConnectorManager {
    private AgentConnectorManager(){};
    private static Map<String, AgentConnector> _acs = new ConcurrentHashMap<>();
    
    public static AgentConnector createAgentConnector(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AgentConnector ac = new AgentConnector(request, response);
        register(request, ac);
        return ac;
    }
            
    protected static void register(HttpServletRequest request, AgentConnector ac) {
        String user = ServletUtil.getParameterString(request, "user");
        int id = ServletUtil.getParameterInt(request, "requestId");
        _acs.put(user + "_" + id, ac);
    }
    
    protected static void release(AgentConnector ac) {
        HttpServletRequest request = ac.getRequest();
        String user = ServletUtil.getParameterString(request, "user");
        int id = ServletUtil.getParameterInt(request, "requestId");
    }

    public static AgentConnector get(HttpServletRequest request) {
        String user = ServletUtil.getParameterString(request, "user");
        int id = ServletUtil.getParameterInt(request, "requestId");
        AgentConnector ac = _acs.remove(user + "_" + id);
        ac.setAgentRequest(request);
        return ac;
    }            
}
