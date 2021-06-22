package com.example.register.trans.client;

import com.example.register.process.RegistryClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpClientInBoundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private RegistryClient app;

    public HttpClientInBoundHandler(RegistryClient app) {
        this.app = app;
    }

    /**
     *
     * 根据server返回的结果的内容进行处理
     * 200 操作成功并完成所有流程
     * 300
     * 404
     * 500
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse) throws Exception {

    }
}
