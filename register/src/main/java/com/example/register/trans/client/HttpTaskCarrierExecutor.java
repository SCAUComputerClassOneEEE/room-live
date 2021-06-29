package com.example.register.trans.client;

import com.example.register.serviceInfo.InstanceInfo;
import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.utils.HttpTaskExecutorPool;
import com.example.register.utils.JSONUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.ObjectUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 *
 * 只用于发送 http 请求
 * @author hiluyx
 * @since 2021/6/19 16:47
 **/
public class HttpTaskCarrierExecutor {
    private Bootstrap bootstrap; // how

    private ServiceProvider provider; // where

    private FullHttpRequest httpRequest; // for what

    private volatile TaskExecuteResult result;

    private String taskId;

    private HttpTaskCarrierExecutor() {}

    public FullHttpRequest getHttpRequest() {
        return httpRequest;
    }

    public static class Builder {
        private Bootstrap builderBootstrap;
        private ServiceProvider builderProvider;
        private FullHttpRequest builderRequest;
        private ByteBuf bodyBuf;
        private HttpHeaders builderHeaders;
        private String taskId;

        public static Builder builder() { return new Builder(); }

        private Builder() { }

        public Builder byBootstrap(Bootstrap bootstrap) {
            this.builderBootstrap = bootstrap;
            return this;
        }

        public Builder connectWith(ServiceProvider provider) {
            this.builderProvider = provider;
            return this;
        }

        /**
         *
         *
         * 不要在这里添加自动生成的taskId
         */
        public Builder addHeader(String h, Object o) {
            if (builderHeaders == null) {
                builderHeaders = new DefaultHttpHeaders();
            }
            if (h.equals("taskId"))
                taskId = (String)o;
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

            builderRequest.replace(bodyBuf);

            ObjectUtil.checkNotNull(builderBootstrap, "builderProvider is null");
            ObjectUtil.checkNotNull(builderProvider, "builderProvider is null");
            ObjectUtil.checkNotNull(builderRequest, "builderRequest is null");

            target.bootstrap = builderBootstrap;
            target.provider = builderProvider;
            target.httpRequest = builderRequest;
            target.httpRequest.headers().add(builderHeaders);
            target.taskId = taskId;
            return target;
        }
    }

    public static class TaskExecuteResult {
        /*
        -1 connect time out;
         0 read time out;
         ------ result is null
         1 success;
         */
        private int state;
        private FullHttpResponse result;

        public TaskExecuteResult(FullHttpResponse result) {
            state = 1;
            this.result = result;
        }

        public TaskExecuteResult(int s) {
            state = s;
            result = null;
        }

        public boolean success() { return state == 1; }

        public int getState() { return state; }

        public void setState(int state) { this.state = state; }

        public FullHttpResponse getResult() { return result; }

        public void setResult(FullHttpResponse result) { this.result = result; }
    }

    // send
    public void connectAndSend() {
        try {
            ChannelFuture sync = bootstrap.connect(provider.getInfo().host(), provider.getInfo().port()).awaitUninterruptibly();
            /*
            connect time out
             */
            if (!sync.isSuccess()) {
                result = new TaskExecuteResult(-1);
                return;
            }
            sync.channel().writeAndFlush(httpRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String syncGetAndTimeOutRemove() {
        while (result == null) {
            if (result != null) break;
        }
        if (!result.success())
            return null;

        ByteBuf content = this.result.getResult().content();
        byte[] array = content.array();

        return new String(array);
    }

    public void setResult(TaskExecuteResult result) {
        this.result = result;
    }
}
