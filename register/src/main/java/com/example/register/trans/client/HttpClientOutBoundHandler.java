package com.example.register.trans.client;

import com.example.register.process.RegistryClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpClientOutBoundHandler extends ChannelOutboundHandlerAdapter {

    private RegistryClient app;

    public HttpClientOutBoundHandler(RegistryClient app) {
        this.app = app;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        if (msg instanceof FullHttpRequest) {

        }
    }
}
