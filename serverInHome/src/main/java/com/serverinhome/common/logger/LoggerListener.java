package com.serverinhome.common.logger;

@Deprecated
public interface LoggerListener {
    public void log(int level, String company, String component, String method, String msg);
}
