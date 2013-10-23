package com.serverinhome.common.logger;

public interface ILoggerListener {
    void log(int level,
             String type,
             String company,
             String component,
             String idStr,
             String method,
             String msg);
}
