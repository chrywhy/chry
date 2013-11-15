package com.serverinhome.gate.servlet;

import com.serverinhome.util.http.HttpPostStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author chry
 */
public class ServletUtil {
    private ServletUtil() {}
    
    public static String getParameterString(HttpServletRequest request, String key) {
        String val = request.getParameter(key);
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException("No parameter - " + key);
        }
        String str = null;
        try {
            str = URLDecoder.decode(val, "utf-8").trim();
        } catch (UnsupportedEncodingException ex) {
        }
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("No parameter - " + key);
        }
        return str;
    }

    public static String getParameterString(HttpServletRequest request, String key, String defStr) {
        try {
            return getParameterString(request, key);
        } catch (Throwable e) {
            return defStr;
        }
    }

    public static long getParameterLong(HttpServletRequest request, String key) {
        String val = getParameterString(request, key);
        try {
            return Long.valueOf(val);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Invalid value, not a long integer", e);
        }
    }

    public static long getParameterLong(HttpServletRequest request, String key, long defVal) {
        try {
            return getParameterLong(request, key);
        } catch (Throwable e) {
            return defVal;
        }
    }

    public static int getParameterInt(HttpServletRequest request, String key) {
        String val = getParameterString(request, key);
        try {
            return Integer.valueOf(val);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid value, not a integer", e);
        }
    }


    public static int getParameterInt(HttpServletRequest request, String key, int defVal) {
        try {
            return getParameterInt(request, key);
        } catch (Throwable e) {
            return defVal;
        }
    }
    
    public static boolean getParameterBoolean(HttpServletRequest request, String key) {
        String str = getParameterString(request, key);
        return "true".equalsIgnoreCase(str);
    }


    public static boolean getParameterBoolean(HttpServletRequest request, String key, boolean defVal) {
        try {
            return getParameterBoolean(request, key);
        } catch (Throwable e) {
            return defVal;
        }
    }
    
    public static HttpPostStream getHttpPostStream(HttpServletRequest request) throws IOException {
        InputStream is = request.getInputStream();
        String contentType = request.getContentType();
        String contentEncoding = request.getHeader("Content-Encoding");
        return new HttpPostStream(is, contentEncoding, contentType, true);
    }    
}
