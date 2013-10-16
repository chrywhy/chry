package com.serverinhome.common.logger;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @deprecated As of release v41, you need to use {@link Logger2 Logger2}, {@link LogMsg LogMsg},
 *             and {@link LogNotice LogNotice}.
 */
@Deprecated
public class Logger {
    public static final int LOGLEVEL_TRACE = 0;
    public static final int LOGLEVEL_DEBUG = 1;
    public static final int LOGLEVEL_INFO = 2;
    public static final int LOGLEVEL_WARN = 3;
    public static final int LOGLEVEL_ERROR = 4;
    public static final int LOGLEVEL_CRITICAL = 5;
    public static final int LOGLEVEL_EXCEPTION = 6;
    public static final int LOGLEVEL_DISABLE = 7;

    public static int _defaultLoglevel = LOGLEVEL_INFO; // make it easy to override this for debugging purpose

    private final CopyOnWriteArrayList<LoggerListener> _listeners = new
            CopyOnWriteArrayList<LoggerListener>();

    /**
     * Data formatter
     */
    private static final SimpleDateFormat _dateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss z");

    /**
     * Company -> (component -> level)
     */
    private final Map<String, Map<String, Integer>> _logLevels = new HashMap<String, Map<String, Integer>>();

    final class ThreadLocalComponent extends ThreadLocal {
        private String _initVal = "default";

        public Object initialValue() {
            return new StringBuffer(_initVal);
        }

        public String getComponent() {
            StringBuffer sb = (StringBuffer) super.get();
            synchronized (sb) {
                return sb.toString();
            }
        }

        public void setComponent(String component) {
            StringBuffer sb = (StringBuffer) super.get();
            synchronized (sb) {
                sb.replace(0, sb.length(), component);
            }
        }

        public ThreadLocalComponent(String initVal) {
            _initVal = initVal;
        }
    }

    final private ThreadLocalComponent _component = new ThreadLocalComponent("default");
    final private ThreadLocalComponent _idStr = new ThreadLocalComponent("");
    final private ThreadLocalComponent _company = new ThreadLocalComponent("default");

    private Logger() {
    }

    private static final Logger _INST = new Logger();

    private static Logger getInstance() {
        return _INST;
    }

    // ---- Utils -------------------------------------------------------------
    static private String _logLevelToString(int level) {
        switch (level) {
            case LOGLEVEL_TRACE:
                return "TRACE";
            case LOGLEVEL_DEBUG:
                return "DEBUG";
            case LOGLEVEL_INFO:
                return "INFO";
            case LOGLEVEL_WARN:
                return "WARN";
            case LOGLEVEL_ERROR:
                return "ERROR";
            case LOGLEVEL_CRITICAL:
                return "CRITICAL";
            case LOGLEVEL_EXCEPTION:
                return "EXCEPTION";
            case LOGLEVEL_DISABLE:
                return "DISABLE";
            default:
                return "UNKNOWN";
        }
    }

    private String _getComponent(String component) {
        if (component == null || component.length() == 0) {
            component = _component.getComponent();
        }

        return component;
    }

    private String _getCompany(String company) {
        if (company == null || company.length() == 0) {
            company = _company.getComponent();
        }

        return company;
    }

    // --------------------------------------------------------------------------------
    // 
    private Collection<String> _getKnownComponents(String company) {
        synchronized (_logLevels) {
            if (company == null || company.length() == 0) {
                company = "default";
            }

            if (!_logLevels.containsKey(company)) {
                return new ArrayList<String>();
            }

            Map<String, Integer> componentLogLevels = _logLevels.get(company);

            Collection<String> known = new ArrayList<String>();

            for (String key : componentLogLevels.keySet()) {
                known.add(key);
            }

            // if default is missing, let's add it
            if (!known.contains("default")) {
                known.add("default");
            }

            return known;
        }
    }

    static public Collection<String> getKnownComponents(String company) {
        return getInstance()._getKnownComponents(company);
    }

    static public Collection<String> getKnownComponents() {
        return getKnownComponents("default");
    }

    private String _getIdStr() {
        return _idStr.getComponent();
    }

    static public String getIdStr() {
        return getInstance()._getIdStr();
    }

    static public String getComponent() {
        return getInstance()._component.getComponent();
    }

    private String _getCompany() {
        return _company.getComponent();
    }

    static private String _cutClassName(String fqClassName) {
        int idx = fqClassName.lastIndexOf('.');
        if (idx < 0) {
            return fqClassName;
        }

        return fqClassName.substring(idx + 1);
    }

    private boolean _logEnable(int level) {
        String component = _getComponent(null);
        String idStr = _getIdStr();
        String company = _getCompany();
        int logLevel = _getLogLevel(company, component);

        return !(logLevel == LOGLEVEL_DISABLE || level <= logLevel);
    }

    static public boolean logEnable(int level) {
        return getInstance()._logEnable(level);
    }

    private void _log(int level, String company, String component, String method, String msg) {
        component = _getComponent(component);
        String idStr = _getIdStr();
        company = _getCompany(company);
        int logLevel = _getLogLevel(company, component);
        if (logLevel == LOGLEVEL_DISABLE) {
            return;
        }

        if (level >= logLevel) {
            StackTraceElement[] elems = Thread.currentThread().getStackTrace();

            String realMethod = null;
            for (int i = 1; i < elems.length; i++) {
                if (elems[i].getClassName().equals("com.santaba.common.logger.Logger")) {
                    continue;
                }

                realMethod = String.format("%s.%s:%d", _cutClassName(elems[i].getClassName()), elems[i].getMethodName(), elems[i].getLineNumber());
                break;
            }

            if (realMethod == null) {
                if (elems.length >= 4) {
                    method = String.format("%s.%s:%d", _cutClassName(elems[3].getClassName()), elems[3].getMethodName(), elems[3].getLineNumber());
                }
            }
            else {
                method = realMethod; // let's override this ...
            }

            String s = formatLoggingMessage(level, company, idStr, component, method, msg);

            // synchronized by it self
            System.out.println(s);

            for (LoggerListener listener : _listeners) {
                listener.log(level, company, component, method, msg);
            }
        }
    }

    static public String formatLoggingMessage(int level,
                                              String company,
                                              String idStr,
                                              String component,
                                              String method,
                                              String msg) {
        StringBuilder buf = new StringBuilder();

        buf.append("[").append(_dateFormatter.format(new Date())).append("] ")
                .append("[").append(_logLevelToString(level)).append("] ");

        // Print a thread ID here.
        buf.append("[").append(Thread.currentThread().getId()).append("] ");

        if (idStr != null && idStr.length() > 0) {
            buf.append('[').append(idStr).append("]");
        }

        if (component.startsWith("collector")) {
            buf.append("[").append(Thread.currentThread().getName()).append("]");
        }
        else if (company == null || company.length() == 0) {
            buf.append("[").append(Thread.currentThread().getName()).append("]");
        }
        else {
            buf.append("[").append(company).append("] ");
        }

        buf.append("[").append(component).append("] ")
                .append("[").append(method).append("] ")
                .append(msg);

        return buf.toString();
    }

    // --------------------------------------------------------------------------------
    // for agent only, so no company and component
    private void _log(int level, long id, String method, String msg) {
        String component = _getComponent("");
        String idStr = _getIdStr();
        int logLevel = _getLogLevel("", component);
        if (logLevel == LOGLEVEL_DISABLE) {
            return;
        }

        if (level >= logLevel) {
            StackTraceElement[] elems = Thread.currentThread().getStackTrace();
            if (elems.length >= 4) {
                method = String.format("%s.%s:%d", _cutClassName(elems[3].getClassName()),
                        elems[3].getMethodName(), elems[3].getLineNumber());
                if (method.startsWith("Logger.") && elems.length >= 5) {
                    method = String.format("%s.%s:%d", _cutClassName(elems[4].getClassName()),
                            elems[4].getMethodName(), elems[4].getLineNumber());
                }
            }

            String s = formatLoggingMessage(level, null, idStr, component, method, msg);
            System.out.println(s);

            for (LoggerListener listener : _listeners) {
                listener.log(level, "", component, method, msg);
            }
        }
    }

    static public String exceptionToString(Throwable e) {
        String s = "";
        StringWriter sw = null;
        PrintWriter pw = null;

        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            s += sw.toString();
        }
        finally {
            try {
                if (sw != null) {
                    sw.close();
                }
                if (pw != null) {
                    pw.close();
                }
            }
            catch (Exception e1) {
                // not a big deal
            }
        }

        try {
            return e.getMessage() + "\n" + s;
        }
        catch (Exception e1) {
            return s;
        }
    }


    // ---- Logging methods ---------------------------------------------------
    static public int logLevelToInt(String level) {
        if (level.equalsIgnoreCase("TRACE")) {
            return LOGLEVEL_TRACE;
        }
        else if (level.equalsIgnoreCase("DEBUG")) {
            return LOGLEVEL_DEBUG;
        }
        else if (level.equalsIgnoreCase("INFO")) {
            return LOGLEVEL_INFO;
        }
        else if (level.equalsIgnoreCase("WARN")) {
            return LOGLEVEL_WARN;
        }
        else if (level.equalsIgnoreCase("ERROR")) {
            return LOGLEVEL_ERROR;
        }
        else if (level.equalsIgnoreCase("CRITICAL")) {
            return LOGLEVEL_CRITICAL;
        }
        else if (level.equalsIgnoreCase("EXCEPTION")) {
            return LOGLEVEL_EXCEPTION;
        }
        else if (level.equalsIgnoreCase("DISABLE")) {
            return LOGLEVEL_DISABLE;
        }
        else {
            return _defaultLoglevel;
        }
    }

    static public String logLevelFromInt(int level) {
        switch (level) {
            case LOGLEVEL_TRACE:
                return "TRACE";
            case LOGLEVEL_DEBUG:
                return "DEBUG";
            case LOGLEVEL_INFO:
                return "INFO";
            case LOGLEVEL_WARN:
                return "WARN";
            case LOGLEVEL_ERROR:
                return "ERROR";
            case LOGLEVEL_CRITICAL:
                return "CRITICAL";
            case LOGLEVEL_EXCEPTION:
                return "EXCEPTION";
            case LOGLEVEL_DISABLE:
                return "DISABLE";
            default:
                return "INFO";
        }
    }

    static public void setComponent(String component) {
        getInstance()._component.setComponent(component);
    }

    static public void setIdStr(String idStr) {
        getInstance()._idStr.setComponent(idStr);
    }

    static public void setCompany(String company) {
        getInstance()._company.setComponent(company);
    }

    static public void setLogLevel(String component, String level) {
        setLogLevel("default", component, level);
    }

    static public void setLogLevel(String company, String component, String level) {
        setLogLevel(company, component, logLevelToInt(level));
    }

    private void _setLogLevel(String company, String component, int level) {
        synchronized (_logLevels) {
            if (company == null || company.length() == 0) {
                company = "default";
            }

            if (!_logLevels.containsKey(company)) {
                _logLevels.put(company, new HashMap<String, Integer>());
            }
            Map<String, Integer> componentLogLevels = _logLevels.get(company);

            if (component.equals("*")) {
                componentLogLevels.clear();
                _defaultLoglevel = level;
            }
            else {
                componentLogLevels.put(component, level);
            }
        }
    }

    static public void setLogLevel(String company, String component, int level) {
        getInstance()._setLogLevel(company, component, level);
    }

    private int _getLogLevel(String company, String module) {
        synchronized (_logLevels) {
            if (company == null || company.length() == 0) {
                company = "default";
            }

            if (!_logLevels.containsKey(company)) {
                return _defaultLoglevel;
            }

            Map<String, Integer> componentLogLevels = _logLevels.get(company);
            if (componentLogLevels == null) {
                return _defaultLoglevel;
            }

            Integer level = componentLogLevels.get(module);
            if (level == null) {
                return _defaultLoglevel;
            }

            return level;
        }
    }

    static public int getLogLevel(String company, String module) {
        return getInstance()._getLogLevel(company, module);
    }

    static public int getLogLevel(String module) {
        return getLogLevel("default", module);
    }

    static public String getLogLevelAsString(int level) {
        return getInstance()._logLevelToString(level);
    }

    static public void addListener(LoggerListener listener) {
        getInstance()._listeners.add(listener);
    }

    static public void removeListener(LoggerListener listener) {
        getInstance()._listeners.remove(listener);
    }


    // ------------------------------------------------------------------------

    static public void trace(String msg) {
        getInstance()._log(LOGLEVEL_TRACE, "", "", "", msg);
    }

    static public void trace(String method, String msg) {
        getInstance()._log(LOGLEVEL_TRACE, "", "", method, msg);
    }

    static public void trace(String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_TRACE, "", component, method, msg);
    }

    static public void trace(String company, String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_TRACE, company, component, method, msg);
    }

    static public void trace(long id, String method, String msg) {
        getInstance()._log(LOGLEVEL_TRACE, id, method, msg);
    }

    static public void trace2(String msg) {
        getInstance()._log(LOGLEVEL_TRACE, "", "", "", msg);
    }

    static public void trace2(String component, String msg) {
        getInstance()._log(LOGLEVEL_TRACE, "", component, "", msg);
    }

    static public void trace2(String company, String component, String msg) {
        getInstance()._log(LOGLEVEL_TRACE, company, component, "", msg);
    }

    static public void trace2(long id, String msg) {
        getInstance()._log(LOGLEVEL_TRACE, id, "", msg);
    }

    static public void debug(String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, "", "", "", msg);
    }

    static public void debug(String method, String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, "", "", method, msg);
    }

    static public void debug(String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, "", component, method, msg);
    }

    static public void debug(String company, String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, company, component, method, msg);
    }

    static public void debug(long id, String method, String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, id, method, msg);
    }

    static public void debug2(String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, "", "", "", msg);
    }

    static public void debug2(String component, String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, "", component, "", msg);
    }

    static public void debug2(String company, String component, String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, company, component, "", msg);
    }

    static public void debug2(long id, String msg) {
        getInstance()._log(LOGLEVEL_DEBUG, id, "", msg);
    }

    static public void info(String msg) {
        getInstance()._log(LOGLEVEL_INFO, "", "", "", msg);
    }

    static public void info(String method, String msg) {
        getInstance()._log(LOGLEVEL_INFO, "", "", method, msg);
    }

    static public void info(String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_INFO, "", component, method, msg);
    }

    static public void info(String company, String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_INFO, company, component, method, msg);
    }

    static public void info(long id, String method, String msg) {
        getInstance()._log(LOGLEVEL_INFO, id, method, msg);
    }

    static public void info2(String msg) {
        getInstance()._log(LOGLEVEL_INFO, "", "", "", msg);
    }

    static public void info2(String component, String msg) {
        getInstance()._log(LOGLEVEL_INFO, "", component, "", msg);
    }

    static public void info2(String company, String component, String msg) {
        getInstance()._log(LOGLEVEL_INFO, company, component, "", msg);
    }

    static public void info2(long id, String msg) {
        getInstance()._log(LOGLEVEL_INFO, id, "", msg);
    }

    static public void warn(String msg) {
        getInstance()._log(LOGLEVEL_WARN, "", "", "", msg);
    }

    static public void warn(String method, String msg) {
        getInstance()._log(LOGLEVEL_WARN, "", "", method, msg);
    }

    static public void warn(String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_WARN, "", component, method, msg);
    }

    static public void warn(String company, String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_WARN, company, component, method, msg);
    }

    static public void warn(long id, String method, String msg) {
        getInstance()._log(LOGLEVEL_WARN, id, method, msg);
    }

    static public void warn2(String msg) {
        getInstance()._log(LOGLEVEL_WARN, "", "", "", msg);
    }

    static public void warn2(String component, String msg) {
        getInstance()._log(LOGLEVEL_WARN, "", component, "", msg);
    }

    static public void warn2(String company, String component, String msg) {
        getInstance()._log(LOGLEVEL_WARN, company, component, "", msg);
    }

    static public void warn2(long id, String msg) {
        getInstance()._log(LOGLEVEL_WARN, id, "", msg);
    }


    static public void error(String msg) {
        getInstance()._log(LOGLEVEL_ERROR, "", "", "", msg);
    }

    static public void error(String method, String msg) {
        getInstance()._log(LOGLEVEL_ERROR, "", "", method, msg);
    }

    static public void error(String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_ERROR, "", component, method, msg);
    }

    static public void error(String company, String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_ERROR, company, component, method, msg);
    }

    static public void error(long id, String method, String msg) {
        getInstance()._log(LOGLEVEL_ERROR, id, method, msg);
    }

    static public void error2(String msg) {
        getInstance()._log(LOGLEVEL_ERROR, "", "", "", msg);
    }

    static public void error2(String component, String msg) {
        getInstance()._log(LOGLEVEL_ERROR, "", component, "", msg);
    }

    static public void error2(String company, String component, String msg) {
        getInstance()._log(LOGLEVEL_ERROR, company, component, "", msg);
    }

    static public void error2(long id, String msg) {
        getInstance()._log(LOGLEVEL_ERROR, id, "", msg);
    }


    static public void critical(String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, "", "", "", msg);
    }

    static public void critical(String method, String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, "", "", method, msg);
    }

    static public void critical(String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, "", component, method, msg);
    }

    static public void critical(String company, String component, String method, String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, company, component, method, msg);
    }

    static public void critical(long id, String method, String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, id, method, msg);
    }

    static public void critical2(String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, "", "", "", msg);
    }

    static public void critical2(String component, String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, "", component, "", msg);
    }

    static public void critical2(String company, String component, String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, company, component, "", msg);
    }

    static public void critical2(long id, String msg) {
        getInstance()._log(LOGLEVEL_CRITICAL, id, "", msg);
    }

    static public void exception(Throwable e) {
        exception("", "", "", e);
    }

    static public void exception(String method, Throwable e) {
        exception("", "", method, e);
    }

    static public void exception(String component, String method, Throwable e) {
        exception("", component, method, e);
    }

    static public void exception(String company, String component, String method, Throwable e) {
        exception(company, component, method, "", e);
    }

    static public void exception2(Throwable e) {
        exception("", "", "", e);
    }

    static public void exception2(Throwable e, String msg) {
        exception("", "", "", msg, e);
    }

    static public void exception2(String component, Throwable e) {
        exception("", component, "", e);
    }

    static public void exception2(String company, String component, Throwable e) {
        exception(company, component, "", "", e);
    }

    static public void exception(HttpServletRequest request, Throwable e) {
        exception("", "", "", request, e);
    }

    static public void exception(String method, HttpServletRequest request, Throwable e) {
        exception("", "", method, request, e);
    }

    static public void exception(String component, String method, HttpServletRequest request, Throwable e) {
        exception("", component, method, request, e);
    }

    static public void exception(String company, String component, String method, HttpServletRequest request, Throwable e) {
        exception(company, component, method, request, "", e);
    }

    static public void exception2(HttpServletRequest request, Throwable e) {
        exception("", "", "", request, e);
    }

    static public void exception2(String component, HttpServletRequest request, Throwable e) {
        exception("", component, "", request, e);
    }

    static public void exception2(String company, String component, HttpServletRequest request, Throwable e) {
        exception(company, component, "", request, "", e);
    }


    static public void exception(String company, String component, String method, HttpServletRequest request, String msg, Throwable e) {
        StringBuffer buf = new StringBuffer();
        buf.append("PATHINFO=").append(request.getRequestURI()).append(", ");

        StringBuffer buf1 = new StringBuffer();
        Enumeration eum = request.getParameterNames();
        while (eum.hasMoreElements()) {
            String key = (String) eum.nextElement();
            String value = (String) request.getParameter(key);
            buf1.append(key).append('=').append(value).append(",  ");
        }
        buf.append("QUERYSTRING=").append(buf1);

        buf.append("\n\n").append(msg).append("\n");

        exception(company, component, method, buf.toString(), e);
    }

    static public void exception(String company, String component, String method, String msg, Throwable e) {
        getInstance()._log(LOGLEVEL_EXCEPTION, company, component, method, msg + '\n' + exceptionToString(e));
    }


    static public void main(String[] args) throws Exception {
        Logger.info("hello");
    }
}
