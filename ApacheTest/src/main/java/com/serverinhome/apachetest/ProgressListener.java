package com.serverinhome.apachetest;

public abstract interface ProgressListener {
    public abstract void postStart();

    public abstract void postFinish();

    public abstract void progress(int paramInt);
}
