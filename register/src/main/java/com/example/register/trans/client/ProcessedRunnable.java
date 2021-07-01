package com.example.register.trans.client;

import java.io.IOException;

public abstract class ProcessedRunnable implements Runnable {

    private HttpTaskCarrierExecutor process;

    public ProcessedRunnable() { }

    public void setExecutor(HttpTaskCarrierExecutor process) { this.process = process; }

    @Override
    @Deprecated
    public void run() {
        if (process.isSuccess()) {
            try {
                successAndThen(process, process.getResultString());
            } catch (Exception e) {
                process.setParseSuccess(false);
            }
        } else {
            failAndThen(process, process.getResultString());
        }
    }

    public void successAndThen(HttpTaskCarrierExecutor process, String resultString) throws Exception {

    }

    public void failAndThen(HttpTaskCarrierExecutor process, String resultString) {

    }

}
