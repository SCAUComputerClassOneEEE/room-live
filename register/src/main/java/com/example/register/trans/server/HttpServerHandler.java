package com.example.register.trans.server;

import com.example.register.process.NameCenterPeerProcess;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.utils.JSONUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

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
            else if (request.method().name().equals("POST")) {
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
        if (StringUtil.isNullOrEmpty(uri)){
            throw new RuntimeException("uri is null.");
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
        /* 如果请求头里有PEER_REPLICATION，不再调用replicate
        * callByPeer = true, secondPeer = true*/
        String replication = request.headers().get("REPLICATION");
        if (StringUtil.isNullOrEmpty(replication)) {
            throw new RuntimeException("replication header is empty");
        }
        boolean secondPeer = replication.equals("PEER");
        InetSocketAddress address = (InetSocketAddress)(cxt.channel().remoteAddress());
        String uri = request.uri();
        String content = request.content().toString(StandardCharsets.UTF_8);
        ServiceProvider serviceProvider = null;
        if (!uri.equals("/antiReplication")) {
            serviceProvider = JSONUtil.readValue(content, ServiceProvider.class);
        }
        switch (uri) {
            case "/register" :      app.register(serviceProvider,
                                false, true, secondPeer);
                                    break;
            case "/renew" :         app.renew(serviceProvider,
                                false, true, secondPeer);
                                    break;
            case "/offline" :       app.offline(serviceProvider,
                                false, true, secondPeer);
                                    break;
            case "/antiReplicate" : app.discover(new ServiceProvider("",
                    address.getHostName(), address.getPort()), content, false, null);
                                    break;
            default:throw new RuntimeException("uri is invalid.");
        }
        response(cxt, HttpResponseStatus.OK, "");
    }

    private void response(ChannelHandlerContext cxt, HttpResponseStatus status, String c) {
        if (taskId == null) {
            cxt.channel().closeFuture();
            return;
        }
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(c, CharsetUtil.UTF_8));
        response.headers().add("taskId", taskId);
        cxt.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        HttpResponseStatus error;
        logger.debug("exceptionCaught:" + cause.getMessage());
        cause.printStackTrace();
        if (cause instanceof WriteTimeoutException) {
            error = HttpResponseStatus.NO_CONTENT;
        } else {
            error = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }
        response(ctx, error, cause.toString());
    }
}
