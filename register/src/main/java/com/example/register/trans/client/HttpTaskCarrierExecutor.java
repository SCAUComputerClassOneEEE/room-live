package com.example.register.trans.client;

import com.example.register.serviceInfo.InstanceInfo;
import com.example.register.serviceInfo.ServiceProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;
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

    private HttpTaskCarrierExecutor() {}

    public FullHttpRequest getHttpRequest() {
        return httpRequest;
    }

    public static class Builder {

        private Bootstrap builderBootstrap;
        private ServiceProvider provider;
        private FullHttpRequest builderRequest;
        private ByteBuf bodyBuf;

        public static Builder builder() { return new Builder(); }

        private Builder() { }

        public Builder byBootstrap(Bootstrap bootstrap) {
            this.builderBootstrap = bootstrap;
            return this;
        }

        public Builder connectWith(ServiceProvider provider) {
            this.provider = provider;
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

            target.bootstrap = builderBootstrap;
            target.provider = provider;
            target.httpRequest = builderRequest;
            return target;
        }
    }

    // send
    public void connectAndSend() {
        try {
            ChannelFuture sync = bootstrap.connect(provider.getInfo().host(), provider.getInfo().port()).sync();
            httpRequest.headers().add("taskId", UUID.randomUUID().toString());
            httpRequest.headers().add("startTime", System.currentTimeMillis());
            sync.channel().writeAndFlush(httpRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
