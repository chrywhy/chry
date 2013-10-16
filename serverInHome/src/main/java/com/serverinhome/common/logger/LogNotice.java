package com.serverinhome.common.logger;

/**
 * A convenient wrapper for Logger2
 */
public final class LogNotice {
    private LogNotice() {
    }

    public static void debug(String cause, String action, String context) {
        Logger2._notice(Logger2.DEBUG, cause, action, context);
    }

    public static void info(String cause, String action, String context) {
        Logger2._notice(Logger2.INFO, cause, action, context);
    }

    public static void warn(String cause, String action, String context) {
        Logger2._notice(Logger2.WARN, cause, action, context);
    }

    public static void error(String cause, String action, String context) {
        Logger2._notice(Logger2.ERROR, cause, action, context);
    }

    public static void critical(String cause, String action, String context) {
        Logger2._notice(Logger2.CRITICAL, cause, action, context);
    }

    public static void debug(String cause, String action, String context, Throwable e) {
        Logger2._notice(Logger2.DEBUG, cause, action, context, e);
    }

    public static void info(String cause, String action, String context, Throwable e) {
        Logger2._notice(Logger2.INFO, cause, action, context, e);
    }

    public static void warn(String cause, String action, String context, Throwable e) {
        Logger2._notice(Logger2.WARN, cause, action, context, e);
    }

    public static void error(String cause, String action, String context, Throwable e) {
        Logger2._notice(Logger2.ERROR, cause, action, context, e);
    }

    public static void critical(String cause, String action, String context, Throwable e) {
        Logger2._notice(Logger2.CRITICAL, cause, action, context, e);
    }
}
