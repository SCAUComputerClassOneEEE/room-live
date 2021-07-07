package com.example.register.trans.client;

import com.example.register.process.DiscoveryNodeProcess;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessedRunnable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ProcessedRunnable.class);

    private HttpTaskCarrierExecutor process;

    public ProcessedRunnable() { }

    protected void setExecutor(HttpTaskCarrierExecutor process) { this.process = process; }

    @Override
    public final void run() {
        if (process.execSuccess()) {
            try {
                successAndThen(process.getResultStatus(), process.getResultString());
            } catch (Exception e) {
                logger.error("when HttpTaskCarrierExecutor execSuccess " + process.getTaskId() + " but " + e.getMessage());
            }
        } else {
            failAndThen(process.getErrorType(), process.getResultString());
        }
        synchronized (process.getLock()) {
            process.getLock().notify();
            logger.debug(process.getTaskId() + " executor's lock notify");
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
    public void failAndThen(ResultType errorType, String resultString) {

    }

}
