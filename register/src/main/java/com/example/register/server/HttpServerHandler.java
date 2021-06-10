package com.example.register.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 服务器的接口处。
     * 长轮询直接返回的，不必要进入Thread；
     * 需要hold住的，request和channel放到订阅者中。
     *
     * @param cxt 通道上下文
     * @param request client请求
     * @throws Exception 。。。？
     */
    @Override
    protected void channelRead0(ChannelHandlerContext cxt, FullHttpRequest request) throws Exception {
        if (request.method().name().equals("GET")) {
            getMethod(cxt, request);
        }
        switch (request.method().name()) {
            case "PUT" :
            case "DELETE" :
            case  "POST" :
        }
    }

    private void getMethod(ChannelHandlerContext cxt, FullHttpRequest request) {
        String clientVersion = request.headers().get("clientVersion");
        String appName = request.headers().get("appName");
        if (clientVersion == null || appName == null) {
            cxt.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer("no such header parameter {clientVersion}", CharsetUtil.UTF_8)))
                    .addListener(ChannelFutureListener.CLOSE); // 关闭连接
            return;
        }
        System.out.println("pull table");
        // appName do something...
    }
}
