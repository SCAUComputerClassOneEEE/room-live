package com.example.register.trans.server;

import com.example.register.process.RegistryServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private RegistryServer app;

    public HttpServerHandler(RegistryServer app) {
        this.app = app;
    }

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
        if (request.method().name().equals("GET")) { // 主要的实现方法，长轮询
            getMethod(cxt, request);
        }
        switch (request.method().name()) {
            case "PUT" : putMethod(cxt, request); break;
            case "DELETE" : deleteMethod(cxt, request); break;
            case "POST" : postMethod(cxt, request); break;
            default: response(cxt, "error http method!");
        }
        cxt.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
                .addListener(ChannelFutureListener.CLOSE);
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
        System.out.println("c: " + clientVersion + "\n" + "a: " + appName);
        response(cxt, "i know, and do it");
    }

    public void putMethod(ChannelHandlerContext cxt, FullHttpRequest request) {
        ByteBuf content = request.content();

    }

    public void deleteMethod(ChannelHandlerContext cxt, FullHttpRequest request) {

    }

    public void postMethod(ChannelHandlerContext cxt, FullHttpRequest request) {

    }

    private void response(ChannelHandlerContext cxt, String c) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(c, CharsetUtil.UTF_8));

        cxt.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
