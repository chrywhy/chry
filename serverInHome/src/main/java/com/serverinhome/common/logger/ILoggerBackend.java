package com.serverinhome.common.logger;

/**
 * A ILoggerBackend provides a print() method to output the
 * logging messages to stdout, stderr, or anywhere you like
 */
public interface ILoggerBackend {
    void print(String msg);
}
