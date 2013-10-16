package com.serverinhome.common.logger;

/**
 * A convenient wrapper for Logger2
 */
public final class LogMsg {
    private LogMsg() {
    }

    public static void debug(String msg, String context) {
        Logger2._mesg(Logger2.DEBUG, msg, context);
    }

    public static void info(String msg, String context) {
        Logger2._mesg(Logger2.INFO, msg, context);
    }

    public static void warn(String msg, String context) {
        Logger2._mesg(Logger2.WARN, msg, context);
    }

    public static void error(String msg, String context) {
        Logger2._mesg(Logger2.ERROR, msg, context);
    }

    public static void critical(String msg, String context) {
        Logger2._mesg(Logger2.CRITICAL, msg, context);
    }

    public static void debug(String msg, String context, Throwable e) {
        Logger2._mesg(Logger2.DEBUG, msg, context, e);
    }

    public static void info(String msg, String context, Throwable e) {
        Logger2._mesg(Logger2.INFO, msg, context, e);
    }

    public static void warn(String msg, String context, Throwable e) {
        Logger2._mesg(Logger2.WARN, msg, context, e);
    }

    public static void error(String msg, String context, Throwable e) {
        Logger2._mesg(Logger2.ERROR, msg, context, e);
    }

    public static void critical(String msg, String context, Throwable e) {
        Logger2._mesg(Logger2.CRITICAL, msg, context, e);
    }
}
