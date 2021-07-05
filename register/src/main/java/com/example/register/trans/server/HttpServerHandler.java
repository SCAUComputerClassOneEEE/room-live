package com.example.register.trans.server;

import com.example.register.process.NameCenterPeerProcess;
import com.example.register.process.RegistryClient;
import com.example.register.process.RegistryServer;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final NameCenterPeerProcess app;
    private String taskId;

    public HttpServerHandler(NameCenterPeerProcess app) {
        this.app = app;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext cxt, FullHttpRequest request) throws Exception {
        try {
            if (!app.isRunning()) {
                throw new RuntimeException("server thread end.");
            }
            taskId = request.headers().get("taskId");
            if (StringUtil.isNullOrEmpty(taskId)) {
                taskId = "";
                throw new RuntimeException("taskId is null.");
            }
            if (request.method().name().equals("GET")) {
                getMethod(cxt, request);
            }
            else if ("POST".equals(request.method().name())) {
                postMethod(cxt, request);
            } else {
                response(cxt, HttpResponseStatus.BAD_REQUEST, "error http method!");
            }
        } catch (Exception e) {
            this.exceptionCaught(cxt, e);
        } finally {
            cxt.channel().closeFuture().sync();
        }
    }

    private void getMethod(ChannelHandlerContext cxt, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        String replication = request.headers().get("REPLICATION");
        if (StringUtil.isNullOrEmpty(replication) || StringUtil.isNullOrEmpty(uri)){
            throw new RuntimeException("replication header is empty or uri is null.");
        }
        if (!uri.equals("/discover")) {
            throw new RuntimeException("uri is invalid.");
        }

        String[] appNames = request.content().toString(CharsetUtil.UTF_8).split(",");
        Map<String,  Set<ServiceProvider>> appServiceMap = new HashMap<>();
        for (String appName : appNames) {
            Set<ServiceProvider> apps = app.find(appName);
            if (apps != null) {
                appServiceMap.put(appName, apps);
            }
        }
        String mapString = JSONUtil.writeValue(appServiceMap);
        response(cxt, HttpResponseStatus.OK, mapString);
    }

    public void postMethod(ChannelHandlerContext cxt, FullHttpRequest request) throws Exception {
        /*
        * 如果请求头里有PEER_REPLICATION，不再调用replicate
        * callByPeer = true, secondPeer = true
        * */
    }

    private void response(ChannelHandlerContext cxt, HttpResponseStatus status, String c) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(c, CharsetUtil.UTF_8));
        response.headers().add("taskId", taskId);
        cxt.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        response(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, cause.toString());
    }
}
