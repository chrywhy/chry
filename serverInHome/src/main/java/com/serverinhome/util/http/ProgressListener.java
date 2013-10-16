package com.serverinhome.util.http;

public abstract interface ProgressListener {
    public abstract void postStart();

    public abstract void postFinish();

    public abstract void progress(int paramInt);
}
