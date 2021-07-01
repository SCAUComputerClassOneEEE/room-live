package com.example.register.trans.client;

public abstract class ProcessedRunnable<P> implements Runnable {

    private P process;

    public ProcessedRunnable() { }

    public void setExecutor(P process) { this.process = process; }

    @Override
    @Deprecated
    public void run() {
        processed(process);
    }

    public abstract void processed(P process);

}
