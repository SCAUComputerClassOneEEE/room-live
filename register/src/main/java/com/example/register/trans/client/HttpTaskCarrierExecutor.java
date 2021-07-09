package com.example.register.trans.client;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.utils.HttpTaskExecutorPool;
import com.example.register.utils.JSONUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * 只用于发送 http 请求
 *
 * 可同步可嵌套提交的任务执行器
 * @author hiluyx
 * @since 2021/6/19 16:47
 **/
public class HttpTaskCarrierExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HttpTaskCarrierExecutor.class);

    private final Object lock = new Object();
    private ServiceProvider provider; // where
    private volatile TaskExecuteResult result;
    private final String taskId;
    private ProcessedRunnable doneTodo;
    private ApplicationClient client; // 发送http
    private long startExec;
    private long endExec;
    private String body;
    private HttpMethod method;
    private String uri;
    private HttpHeaders headers;

    private HttpTaskCarrierExecutor() {
        taskId = UUID.randomUUID().toString();
    }

    public String getTaskId() {
        return taskId;
    }
    public ApplicationClient getClient() {
        return client;
    }

    public static class Builder {
        private ServiceProvider builderProvider;
        private String bodyBuf;
        private HttpMethod bMethod;
        private String bUri;
        private HttpHeaders builderHeaders;
        private ProcessedRunnable builderRunnable;
        private ApplicationClient builderClient;

        public static Builder builder() { return new Builder(); }

        private Builder() { }

        public Builder byClient(ApplicationClient client) {
            this.builderClient = client;
            return this;
        }

        public Builder connectWith(ServiceProvider provider) {
            this.builderProvider = provider;
            return this;
        }

        public Builder done(ProcessedRunnable runnable) {
            builderRunnable = runnable;
            return this;
        }

        /*
         * 不要在这里添加自动生成的taskId
         */
        public Builder addHeader(String h, Object o) {
            if (builderHeaders == null) {
                builderHeaders = new DefaultHttpHeaders();
            }
            builderHeaders.add(h, o);
            return this;
        }

        public Builder withBody(String body) {
            bodyBuf = body;
            return this;
        }

        public Builder access(HttpMethod method, String uri) {
            bMethod = method;
            bUri = uri;
            return this;
        }

        public HttpTaskCarrierExecutor create() {
            final HttpTaskCarrierExecutor target = new HttpTaskCarrierExecutor();

            builderRunnable.setExecutor(target);

            ObjectUtil.checkNotNull(builderClient, "builderClient is null");
            ObjectUtil.checkNotNull(builderProvider, "builderProvider is null");

            target.client = builderClient;
            target.provider = builderProvider;
            target.method = bMethod;
            target.uri = bUri;
            target.body = bodyBuf;
            builderHeaders.add("taskId", target.taskId);
            String accept = builderHeaders.get("Accept");
            if (accept == null || accept.equals("")) {
                builderHeaders.add("Accept", "application/json");
            }
            target.headers = builderHeaders;
            target.doneTodo = builderRunnable;
            return target;
        }
    }

    private static class TaskExecuteResult {
        final ResultType state;
        final FullHttpResponse result;
        Throwable cause;

        TaskExecuteResult(FullHttpResponse result) {
            state = ResultType.SUCCESS;
            result.retain();
            this.result = result;
        }

        TaskExecuteResult(ResultType type) {
            state = type;
            result = null;
        }

        Throwable getCause() { return cause; }

        FullHttpResponse getResult() { return result; }

        void setCause(Throwable cause) { this.cause = cause; }

        ResultType getState() {
            return state;
        }

        boolean success() { return state.equals(ResultType.SUCCESS); }

        void release() {
            if (result.refCnt() > 0)
                result.release();
        }

        String resultString() throws Throwable {
            if (result == null)
                return null;
            if (!success())
                throw getCause();
            ByteBuf content = result.content();
            release();
            return content.toString(CharsetUtil.UTF_8);
        }
    }

    /*
    *                               ... parent thread processing
    *                               /|\
    *                                |   false
    * builder --> create --> subTask --> (sync?)              doneRunnable to do --> parent thread is notified
    *                                |   true, to wait                   /|\
    *                                |                                    |
    *                               sync                                  |
    *                                |                                    |
    *                               \|/                                   |
    *                          connectAndSend --> CONNECT_TIME_OUT  --> syncFail
    *                                |       |--> RUNTIME_EXCEPTION --> syncFail
    *                               \|/                                   |
    *                         request out bound                           |
    *                                |                                    |
    *                               \|/                                   |
    *                       response in bound --> READ_TIME_OUT     --> syncFail
    *                                |       |--> RUNTIME_EXCEPTION --> syncFail
    *                                |       |--> UNKNOWN           --> syncFail
    *                               \|/                                   |
    *                        channel inactive --> SUCCESS           --> syncSuccess
    * */
    protected void success(FullHttpResponse result) {
        logger.info(taskId + " success");
        endExec = System.currentTimeMillis();
        provider.fixAccessAvg(endExec - startExec);
        provider.decrementConnectingInt();
        setResult(result);
        HttpTaskExecutorPool.getInstance().submit(this.doneTodo);
    }

    protected void fail(ResultType type, Throwable cause) {
        logger.info(taskId + " fail because of " + type.name()+ ": " + cause.getMessage());
        cause.printStackTrace();
        endExec = System.currentTimeMillis();
        provider.fixAccessAvg(endExec - startExec);
        provider.decrementConnectingInt();
        setResult(type, cause);
        HttpTaskExecutorPool.getInstance().submit(this.doneTodo);
    }

    protected void connectAndSend() {
        try {
            startExec = System.currentTimeMillis();
            provider.incrementConnectingInt();
            // connect
            logger.debug(taskId + " Try to connect with " + provider);
            ChannelFuture sync = ((Bootstrap)client.getBootstrap()).connect(provider.getHost(), provider.port()).sync();
            if (!sync.isSuccess()) {
                /*connect time out */
                fail(ResultType.CONNECT_TIME_OUT, new Exception("Connect TIMEOUT with" + provider.toString()));
            } else {
                DefaultFullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri);
                httpRequest.content().writeBytes(body.getBytes(StandardCharsets.UTF_8));

                httpRequest.headers().add(headers);

                logger.info(taskId + " connect success and then put into taskMap.\n" + httpRequest);
                HttpTaskExecutorPool.taskMap.put(taskId, this);
                sync.channel().writeAndFlush(httpRequest).sync();
            }
        } catch (Exception e) {
            fail(ResultType.RUNTIME_EXCEPTION, e);
        }
    }

    protected boolean execSuccess() {
        return result.success();
    }

    protected HttpResponseStatus getResultStatus() {
        return result.getResult().status();
    }

    protected ResultType getErrorType() {
        return result.getState();
    }

    protected String getResultString() {
        String rs;
        try {
            rs = result.resultString();
        } catch (Throwable throwable) {
            rs = result.getCause().toString();
        } finally {
            HttpTaskExecutorPool.taskMap.remove(taskId);
        }
        return rs;
    }

    public void sub() throws Exception {
        client.subTask(this);
    }

    public void sync() throws InterruptedException {
        logger.debug(taskId + " executor's lock wait.");
        synchronized (lock) {
            lock.wait(); // 阻塞等待done Runnable的任务结束，这时候syncer可能为null
            logger.debug(taskId + " executor's lock notify successfully.");
        }
    }

    protected Object getLock() {
        return lock;
    }

    private void setResult(FullHttpResponse response) {
        this.result = new TaskExecuteResult(response);
    }

    private void setResult(ResultType type, Throwable cause) {
        this.result = new TaskExecuteResult(type);
        this.result.setCause(cause);
    }
}
