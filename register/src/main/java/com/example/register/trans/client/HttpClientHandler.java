package com.example.register.trans.client;

import com.example.register.process.RegistryClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private RegistryClient app;

    public HttpClientHandler(RegistryClient app) {
        this.app = app;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse) throws Exception {

    }
}
