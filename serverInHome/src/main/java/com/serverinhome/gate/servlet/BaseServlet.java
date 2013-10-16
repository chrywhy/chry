package com.serverinhome.gate.servlet;

import com.serverinhome.util.StringUtil;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.json.JSONArray;
import org.json.JSONObject;

abstract public class BaseServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    abstract public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    // ---- Utils -------------------------------------------------------------

    static protected String _getParameterString(HttpServletRequest request,
                                                String key) {
        return StringUtil.unescapeHTML(StringUtil.strip(request.getParameter(key)));
    }


    protected long _getParameterLong(HttpServletRequest request,
                                     String key) {
        String str = _getParameterString(request, key);
        if (str.length() == 0) {
            return 0;
        }

        try {
            return Long.valueOf(str);
        }
        catch (Exception e) {
            return 0;
        }
    }

    protected long _getParameterLongWithoutException(HttpServletRequest request, String key) {
        String str = StringUtil.strip((String) request.getParameter(key));
        if (str == null || str.length() == 0) {
            return 0;
        }

        try {
            return Long.valueOf(str);
        }
        catch (Exception e) {
            return 0;
        }
    }


    protected int _getParameterIntWithoutException(HttpServletRequest request, String key) {
        String str = StringUtil.strip((String) request.getParameter(key));
        if (str == null || str.length() == 0) {
            return 0;
        }

        try {
            return Integer.valueOf(str);
        }
        catch (Exception e) {
            return 0;
        }
    }


    protected int _getParameterIntWithoutException(HttpServletRequest request, String key, int defaultValue) {
        String str = StringUtil.strip((String) request.getParameter(key));
        if (str == null || str.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(str);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }


    protected int _getParameterInt1(HttpServletRequest request, String key)
            throws Exception {
        String str = StringUtil.strip((String) request.getParameter(key));
        if (str == null || str.length() == 0) {
            return 0;
        }

        return Integer.valueOf(str);
    }


    protected String _getParameterStr(HttpServletRequest request,
                                      String key) {
        String str = StringUtil.unescapeHTML(StringUtil.strip((String) request.getParameter(key)));
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("No parameter - " + key);
        }

        return str;
    }

    static protected int _getParameterInt(HttpServletRequest request,
                                          String key) {
        String str = StringUtil.strip((String) request.getParameter(key));
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("No parameter - " + key);
        }

        return Integer.valueOf(str);
    }


    static protected boolean _getParameterBoolean(HttpServletRequest request,
                                                  String key) {
        String str = _getParameterString(request, key);
        return str.equalsIgnoreCase("y") || str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1");
    }


    protected boolean _getParameterBoolean(HttpServletRequest request,
                                           String key,
                                           boolean defValue) {
        String str = _getParameterString(request, key);
        if (str.length() == 0) {
            return defValue;
        }
        return str.equalsIgnoreCase("y") || str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1") || str.equalsIgnoreCase("yes");
    }


    protected String _getParameterString(HttpServletRequest request, String key, String defVal) {
        String s = StringUtil.unescapeHTML(StringUtil.strip(request.getParameter(key)));
        return s.length() == 0 ? defVal : s;
    }


    protected int[] _getParameterInts(HttpServletRequest request, String key) {
        String s = _getParameterString(request, key);
        return StringUtil.splitInts(s);
    }

    protected String[] _getParameterStrings(HttpServletRequest request, String key) {
        String s = _getParameterString(request, key);
        return StringUtil.split(s, ",", true);
    }

    protected int[] _getParameterIntsWithoutException(HttpServletRequest request, String key) {
        try {
            String s = _getParameterString(request, key);
            return StringUtil.splitInts(s);
        }
        catch (NumberFormatException e) {
            return new int[0];
        }
    }

    protected String _addLine(String in, boolean newLineOnly) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(in));
        String line = "";

        StringBuffer sb = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            if (newLineOnly) {
                sb.append("\n");
            }
            else {
                sb.append("\r\n");
            }
        }
        reader.close();
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    protected void _return(HttpServletResponse response, JSONObject j)
            throws IOException {
        response.setContentType("text/plain");
        response.getWriter().println(j.toString());
    }

    // ------------------------------------------------------------------------

    protected void _setStatus(JSONObject json, int status, String errmsg) {
        try {
            json.put("status", status);
            json.put("errmsg", errmsg);
        }
        catch (Exception e) {
        }
    }

    protected void _setStatus(JSONObject json, int status, String errmsg, Object data) {
        try {
            json.put("status", status);
            json.put("errmsg", errmsg);
            json.put("data", data);
        }
        catch (Exception e) {
        }
    }

    protected void _setStatus(JSONObject json, int status, String errmsg, String jdata) {
        try {
            json.put("status", status);
            json.put("errmsg", errmsg);
            json.put("data", jdata);
        }
        catch (Exception e) {
        }
    }

    protected void _setStatus(JSONObject json, int status, String errmsg, int data) {
        try {
            json.put("status", status);
            json.put("errmsg", errmsg);
            json.put("data", data);
        }
        catch (Exception e) {
        }
    }

    protected void _setStatus(JSONObject j, int code, String msg, JSONObject jdata) {
        try {
            j.put("status", code);
            j.put("errmsg", msg);
            j.put("data", jdata);
        }
        catch (Exception e) {
        }
    }

    protected void _setStatus(JSONObject j, int code, String msg, JSONArray jdata) {
        _setStatus(j, code, msg);
        try {
            j.put("data", jdata);
        }
        catch (Exception e) {
        }
    }

    protected void _setStatus(JSONObject json, Object data) {
        _setStatus(json, 200, "OK", data);
    }

    // ------------------------------------------------------------------------
    protected String _getUsername(HttpServletRequest request) {
        String username = (String) request.getSession(false).getAttribute("username");
        if (username == null) {
            return "unknown";
        }
        else {
            return username;
        }
    }
}
