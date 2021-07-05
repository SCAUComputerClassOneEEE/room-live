package com.example.register.trans.client;

import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class ProcessedRunnable implements Runnable {

    private HttpTaskCarrierExecutor process;

    public ProcessedRunnable() { }

    protected void setExecutor(HttpTaskCarrierExecutor process) { this.process = process; }

    @Override
    public final void run() {
        if (process.execSuccess()) {
            try {
                successAndThen(process.getResultStatus(), process.getResultString());
            } catch (Exception e) {
                System.out.println("i don't know what happened, maybe json parsing.");
                e.printStackTrace();
            }
        } else {
            try {
                failAndThen(process.getErrorType(), process.getResultString());
            } catch (Exception e) {
                process.getClient().stopThread();
                e.printStackTrace();
            }
        }
        synchronized (process.getLock()) {
            process.getLock().notify();
        }
    }

    /**
     * @param resultString HTTP 请求成功后的返回jsonString
     * @param status HTTP 请求成功后的返回状态码
     * */
    public void successAndThen(HttpResponseStatus status, String resultString) throws Exception {

    }

    /**
     * @param resultString HTTP 请求失败后的报错信息
     * @param errorType HTTP 请求失败的出错状态
     * */
    public void failAndThen(ResultType errorType, String resultString) throws Exception {

    }

}
