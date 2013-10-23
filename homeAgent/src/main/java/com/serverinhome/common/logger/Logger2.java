package com.serverinhome.common.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * see <a href="https://confluence.logicmonitor.com/display/DEV/Logger+API">Logger specification</a>
 */
public final class Logger2 {
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int CRITICAL = 5;
    public static final int DISABLE = 6;

    public static final String MESSAGE = "MSG";
    public static final String NOTICE = "NOTICE";


    /**
     * Data formatter
     */
    private static final SimpleDateFormat _dateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss z");

    /**
     * Default level for all components for which we don't
     * specified a log level.
     */
    private static volatile int _defaultLevel = INFO;

    /**
     * A global map to control minimum log level for various components.
     * <p/>
     * Only log messages with the same or higher level will be printed out.
     */
    private static final ConcurrentHashMap<String/*company*/, ConcurrentHashMap<String/*component*/, Integer>> _levels =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();


    private static final CopyOnWriteArrayList<ILoggerListener> _listeners = new
            CopyOnWriteArrayList<ILoggerListener>();

    /**
     * By default, we print all log messages to stdout
     */
    private static volatile ILoggerBackend _backend = new ILoggerBackend() {
        public void print(String msg) {
            System.out.println(msg);
        }
    };

    private Logger2() {
    }

    /**
     * Specifies the minimum log level for the given company and component. All
     * log messages with the given company/component and its level is less than
     * the given level output by any threads after this call will be discarded.
     *
     * @param company   its value could be '', '*', or a specific company name like
     *                  'logicmonitor'. When its value is '*', it means all companies. It's case-insensitive.
     * @param component its value could be '', '*', or a specific component name like
     *                  'collector.esx'. When its value is '*', it means all components. It's case-insensitive.
     */
    public static void setLogLevel(String company, String component, int level) {
        if (company == null) {
            throw new NullPointerException("Company is null");
        }
        if (component == null) {
            throw new NullPointerException("Component is null");
        }
        if (level < DEBUG || level > DISABLE) {
            throw new IllegalArgumentException(String.format("The level %d is invalid.", level));
        }

        company = company.toLowerCase();
        component = component.toLowerCase();

        synchronized (_levels) {
            if (company.equals("*")) {
                if (component.equals("*")) {
                    _defaultLevel = level;
                    _levels.clear();
                }
                else {
                    // a specific component of all companies
                    for (Map.Entry<String, ConcurrentHashMap<String, Integer>> entry : _levels.entrySet()) {
                        ConcurrentHashMap<String, Integer> componentLevels = entry.getValue();
                        componentLevels.put(component, level);
                    }
                }
            }
            else {
                // a specific company
                ConcurrentHashMap<String, Integer> componentLevels = _levels.get(company);
                if (componentLevels == null) {
                    componentLevels = new ConcurrentHashMap<String, Integer>();
                    _levels.put(company, componentLevels);
                }

                if (component.equals("*")) {
                    // all components of a specific company
                    componentLevels.clear();
                    componentLevels.put("*", level);
                }
                else {
                    // a specific component of a specific company
                    componentLevels.put(component, level);
                }
            }
        }
    }

    public static void setLogLevel(String company, String component, String level) {
        setLogLevel(company, component, _levelToInt(level));
    }

    public static void setLogLevel(String component, int level) {
        setLogLevel("", component, level);
    }

    public static void setLogLevel(String component, String level) {
        setLogLevel("", component, level);
    }

    /**
     * The rule we use to determine the minimum log level for a given <company, component> pair are
     * <p/>
     * if the level for this pair is specified, use it.
     * else if the level for <company,*> is specified, use it.
     * else use the global _defaultLevel.
     */
    private static int _getLogLevel(String company, String component) {
        ConcurrentMap<String, Integer> componentLevels = _levels.get(company);
        if (componentLevels == null) {
            return _defaultLevel;
        }

        Integer lvl = componentLevels.get(component);
        if (lvl != null) {
            return lvl;
        }

        lvl = componentLevels.get("*");
        return lvl == null ? _defaultLevel : lvl;
    }

    public static void addListener(ILoggerListener listener) {
        _listeners.add(listener);
    }

    public static void removeListener(ILoggerListener listener) {
        _listeners.remove(listener);
    }

    /**
     * We use thread-local storage to manage thread identities
     */
    private static final ThreadLocal<String> _component = new ThreadLocal<String>();
    private static final ThreadLocal<String> _idStr = new ThreadLocal<String>();
    private static final ThreadLocal<String> _company = new ThreadLocal<String>();

    protected static String _getComponent() {
        String s = _component.get();
        return s == null ? "" : s;
    }

    protected static String _getCompany() {
        String s = _company.get();
        return s == null ? "" : s;
    }

    protected static String _getIdStr() {
        String s = _idStr.get();
        return s == null ? "" : s;
    }

    protected static void _setComponent(String component) {
        if (component == null) {
            throw new NullPointerException("component is null");
        }
        if (component.equals("*")) {
            throw new IllegalArgumentException("component can't be *");
        }

        _component.set(component.toLowerCase());
    }

    protected static void _setCompany(String company) {
        if (company == null) {
            throw new NullPointerException("company is null");
        }
        if (company.equals("*")) {
            throw new IllegalArgumentException("company can't be *");
        }

        _company.set(company.toLowerCase());
    }

    protected static void _setIdStr(String idStr) {
        if (idStr == null) {
            throw new NullPointerException("idStr is null");
        }
        _idStr.set(idStr);
    }


    public static void setBackend(ILoggerBackend backend) {
        _backend = backend;
    }


    /**
     * check if the log message with the specified level is enabled
     * in the context of the current thread.
     * <p/>
     * All calls to mesg() and notice() will be prefixed by this call such as
     * <p/>
     * if ( Logger2.enable(Logger2.DEBUG) )
     * Logger2.mesg(Logger2.DEBUG, ...);
     */
    public static boolean enable(int level) {
        if (level == DISABLE) {
            return false;
        }

        if (level < DEBUG && level > CRITICAL) {
            throw new IllegalArgumentException(String.format("The level %d is invalid", level));
        }

        String company = _getCompany();
        String component = _getComponent();

        return level >= _getLogLevel(company, component);
    }

    /**
     * Specifies identifications for the current thread. All log messages
     * output after this call from this thread will bear those identification
     * information in the message
     */
    public static void setIdentities(String company, String component, String idStr) {
        if (company != null) {
            _setCompany(company);
        }
        if (component != null) {
            _setComponent(component);
        }
        if (idStr != null) {
            _setIdStr(idStr);
        }
    }

    protected static void _mesg(int level, String msg, String context) {
        if (msg == null) {
            throw new NullPointerException("msg is null");
        }
        if (context == null) {
            throw new NullPointerException("context is null");
        }

        if (!enable(level)) {
            return;
        }
        String s = String.format("%s, CONTEXT=%s", msg, context);
        _log(level, MESSAGE, s);
    }

    protected static void _mesg(int level, String msg, String context, Throwable e) {
        if (msg == null) {
            throw new NullPointerException("msg is null");
        }
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (!enable(level)) {
            return;
        }
        String s = String.format("%s, CONTEXT=%s, EXCEPTION=%s", msg, context, exceptionToString(e));
        _log(level, MESSAGE, s);
    }

    protected static void _notice(int level, String cause, String action, String context) {
        if (cause == null) {
            throw new NullPointerException("cause is null");
        }
        if (action == null) {
            throw new NullPointerException("action is null");
        }
        if (context == null) {
            throw new NullPointerException("context is null");
        }

        if (!enable(level)) {
            return;
        }
        String s = String.format("CAUSE=%s, ACTION=%s, CONTEXT=%s", cause, action, context);
        _log(level, NOTICE, s);
    }

    protected static void _notice(int level, String cause, String action, String context, Throwable e) {
        if (cause == null) {
            throw new NullPointerException("cause is null");
        }
        if (action == null) {
            throw new NullPointerException("action is null");
        }
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (!enable(level)) {
            return;
        }
        String s = String.format("CAUSE=%s, ACTION=%s, CONTEXT=%s, EXCEPTION=%s",
                cause, action, context, exceptionToString(e));
        _log(level, NOTICE, s);
    }

    /**
     * Format a log message and use System.out.println() to print it out.
     * The format of a log message is:
     * <p/>
     * [date] [level] [threadId] [identities] [method:lineNum] msg
     */
    private static void  _log(int level, String type, String msg) {
        String method = "";
        StackTraceElement[] elems = Thread.currentThread().getStackTrace();

        String realMethod = null;
        for (int i = 1; i < elems.length; i++) {
            if (elems[i].getClassName().startsWith("com.santaba.common.logger.")) {
                continue;
            }

            realMethod = String.format("%s.%s:%d", _cutClassName(elems[i].getClassName()),
                    elems[i].getMethodName(), elems[i].getLineNumber());
            break;
        }

        if (realMethod == null) {
            if (elems.length >= 4) {
                method = String.format("%s.%s:%d", _cutClassName(elems[3].getClassName()),
                        elems[3].getMethodName(), elems[3].getLineNumber());
            }
        }
        else {
            method = realMethod; // let's override this ...
        }

        String s = formatLoggingMessage(level, type,
                _getCompany(), _getComponent(), _getIdStr(),
                method, msg);
        if (_backend != null) {
            _backend.print(s);
        }

        for (ILoggerListener listener : _listeners) {
            listener.log(level, type, _getCompany(), _getComponent(), _getIdStr(), method, msg);
        }
    }


    private static String _cutClassName(String fqClassName) {
        int idx = fqClassName.lastIndexOf('.');
        if (idx < 0) {
            return fqClassName;
        }

        return fqClassName.substring(idx + 1);
    }


    private static String _levelToString(int level) {
        switch (level) {
            case DEBUG:
                return "DEBUG";
            case INFO:
                return "INFO";
            case WARN:
                return "WARN";
            case ERROR:
                return "ERROR";
            case CRITICAL:
                return "CRITICAL";
            case DISABLE:
                return "DISABLE";
            default:
                throw new IllegalArgumentException(String.format("The level %d is invalid", level));
        }
    }

    private static int _levelToInt(String level) {
        level = level.toLowerCase();
        if (level.equals("debug") || level.equals("trace")) {
            // we don't support trace level anymore. treat it equal to debug
            return DEBUG;
        }
        if (level.equals("info")) {
            return INFO;
        }
        if (level.equals("warn")) {
            return WARN;
        }
        if (level.equals("error")) {
            return ERROR;
        }
        if (level.equals("critical")) {
            return CRITICAL;
        }
        if (level.equals("disable")) {
            return DISABLE;
        }

        // impossible
        throw new IllegalArgumentException("Invalid level " + level);
    }

    private static String _getDateString() {
        return _dateFormatter.format(new Date());
    }

    public static String formatLoggingMessage(int level,
                                              String type,
                                              String company,
                                              String component,
                                              String idStr,
                                              String method,
                                              String msg) {
        return String.format("[%s] [%s] [%s] [%s:%s:%s:%s] [%s] %s",
                _getDateString(),
                type,
                _levelToString(level),
                Thread.currentThread().getName(),
                company, component, idStr,
                method, msg);
    }


    public static String exceptionToString(Throwable e) {
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
            catch (Exception ignore) {
            }
        }

        try {
            return e.getMessage() + "\n" + s;
        }
        catch (Exception e1) {
            return s;
        }
    }

    static public void main(String[] args) throws Exception {
        Logger2._mesg(INFO, "hello", "");
    }
}
