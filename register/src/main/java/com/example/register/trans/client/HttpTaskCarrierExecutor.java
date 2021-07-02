package com.example.register.trans.client;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.utils.HttpTaskExecutorPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.ObjectUtil;

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
    private final Object lock = new Object();
    private ServiceProvider provider; // where
    private FullHttpRequest httpRequest; // for what
    private volatile TaskExecuteResult result;
    private String taskId;
    private ProcessedRunnable doneTodo;
    private ApplicationClient client; // 发送http

    private HttpTaskCarrierExecutor() {}

    public static class Builder {
        private ServiceProvider builderProvider;
        private FullHttpRequest builderRequest;
        private ByteBuf bodyBuf;
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
            bodyBuf = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            return this;
        }

        public Builder access(HttpMethod method, String uri) {
            builderRequest = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1,
                    method, uri);
            return this;
        }


        public HttpTaskCarrierExecutor create() {
            final HttpTaskCarrierExecutor target = new HttpTaskCarrierExecutor();

            builderRequest.replace(
                    bodyBuf == null ?
                    Unpooled.copiedBuffer("", StandardCharsets.UTF_8) : bodyBuf
            );
            builderRunnable.setExecutor(target);

            ObjectUtil.checkNotNull(builderClient, "builderClient is null");
            ObjectUtil.checkNotNull(builderProvider, "builderProvider is null");
            ObjectUtil.checkNotNull(builderRequest, "builderRequest is null, access pre.");

            target.client = builderClient;
            target.provider = builderProvider;
            target.httpRequest = builderRequest;
            target.taskId = UUID.randomUUID().toString();
            builderHeaders.add("taskId", target.taskId);
            String accept = builderHeaders.get("Accept");
            if (accept == null || accept.equals("")) {
                builderHeaders.add("Accept", "application/json");
            }
            target.httpRequest.headers().add(builderHeaders);
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
            byte[] array = content.array();
            release();
            return new String(array);
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
        setResult(result);
        HttpTaskExecutorPool.getInstance().submit(this.doneTodo);
    }

    protected void fail(ResultType type, Throwable cause) {
        setResult(type, cause);
        HttpTaskExecutorPool.getInstance().submit(this.doneTodo);
    }

    protected void connectAndSend() {
        try {
            // connect
            ChannelFuture sync = ((Bootstrap)client.getBootstrap()).connect(provider.getInfo().host(), provider.getInfo().port()).await();
            if (!sync.isSuccess()) {
                /*
                connect time out
                */
                fail(ResultType.CONNECT_TIME_OUT, new Exception("connect time out with" + provider.getInfo()));
                return;
            }
            /*
             * connect successfully!
             * send...
             */
            ChannelFuture send = sync.channel().writeAndFlush(httpRequest);
            send.addListener((ChannelFutureListener) channelFuture -> HttpTaskExecutorPool.taskMap.put(taskId, this));
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
        synchronized (lock) {
            lock.wait(); // 阻塞等待done Runnable的任务结束，这时候syncer可能为null
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
