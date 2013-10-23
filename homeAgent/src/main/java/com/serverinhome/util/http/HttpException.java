package com.serverinhome.util.http;

import java.io.IOException;

/**
 * *******************************************
 * When Http request failed, Java's Http operation will throw exception,
 * But the exception is not covered all cases, when the TCP connection is OK,
 * it will not thow exception, but return the Http status code instead,
 * this make our logic complicated. Actually and mostly we only take care of complete success,
 * that means only status code 200 is OK, all other a error. So This Exception will cover the status code that is not 200
 */
public class HttpException extends IOException {
    public static final int UNKNOWN_ERROR = 500;
    private final int _statusCode;

    public HttpException(int code) {
        this(code, "response code - " + code);
    }

    public HttpException(String errMsg) {
        this(UNKNOWN_ERROR, errMsg);
    }

    public HttpException(int code, String errMsg) {
        super(errMsg);
        this._statusCode = code;
    }

    public final int getStatusCode() {
        return this._statusCode;
    }
}
