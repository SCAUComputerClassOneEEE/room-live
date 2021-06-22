package com.example.register.trans;

import com.example.register.serviceInfo.InstanceInfo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

/**
 * @author hiluyx
 * @since 2021/6/19 16:47
 **/
public class HttpTaskCarrierExecutor implements Runnable {
    private Channel httpChannel; // how
    private InstanceInfo instanceInfo; // where
    private FullHttpRequest httpRequest; // for what
    private ChannelFutureListener doneListener;

    private HttpTaskCarrierExecutor() {}

    public static class Builder {

        // there are some read-only or write listener...

        private Bootstrap builderBootstrap;
        private InstanceInfo builderInfo;
        private FullHttpRequest builderRequest;
        private ByteBuf bodyBuf;
        private ChannelFutureListener builderListener;

        public Builder builder() { return new Builder(); }

        private Builder() { }

        public Builder byBootstrap(Bootstrap bootstrap) {
            this.builderBootstrap = bootstrap;
            return this;
        }

        public Builder connectWith(String ip, int port) {
            builderInfo = new InstanceInfo(ip, port);
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

        public Builder operatedCompletelyWith(ChannelFutureListener listener) {
            builderListener = listener;
            return this;
        }

        public HttpTaskCarrierExecutor create() {
            final HttpTaskCarrierExecutor target = new HttpTaskCarrierExecutor();

            String ip = builderInfo.getInstAdr().getIp();
            int port = builderInfo.getInstAdr().getPort();
            builderRequest.replace(bodyBuf);

            target.httpChannel = builderBootstrap.connect(ip, port).channel();
            target.instanceInfo = builderInfo;
            target.httpRequest = builderRequest;
            target.doneListener = builderListener;
            return target;
        }
    }

    @Override
    public void run() {
        while (true) { // sync
            if (httpChannel != null) {
                final Channel channel = httpChannel;
                try {
                    channel.writeAndFlush(httpRequest).sync().addListener(doneListener);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    channel.close();
                }
                break;
            }
        }
    }
}