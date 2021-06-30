package com.example.register.trans.client;

public abstract class HttpTaskDoneRunnable implements Runnable {

    private HttpTaskCarrierExecutor executor;

    public HttpTaskDoneRunnable() { }

    public void setExecutor(HttpTaskCarrierExecutor executor) { this.executor = executor; }

    @Override
    @Deprecated
    public void run() {
        doneRun(executor);
    }

    public abstract void doneRun(HttpTaskCarrierExecutor executor);

}
