package com.example.register.trans.server;

import com.example.register.process.Application;
import com.example.register.process.RegistryServer;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.ApplicationThread;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author hiluyx
 * @since 2021/6/9 22:02
 **/
public class ApplicationServer extends ApplicationThread<ServerBootstrap, ServerChannel> {

    private RegistryServer app;
    private final EventLoopGroup boosGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(1);
    private int port;

    private static final Runnable runnable = () -> {

    };

    public ApplicationServer(Application application, ServiceProvidersBootConfig config) throws Exception {
        super(runnable);
        init(application, config);
    }

    @Override
    public void init(Application application, ServiceProvidersBootConfig config) throws Exception {
        if (application instanceof RegistryServer) {
            app = (RegistryServer) application;
        } else {
            throw new Exception("server application thread init error.");
        }
        bootstrap = new ServerBootstrap();
        final Integer writeTimeOut = config.getWriteTimeOut();
        final int maxContentLength = config.getMaxContentLength();
        final int backLog = config.getBackLog();
        port = config.getServerPort();
        ((ServerBootstrap) bootstrap)
                .group(boosGroup, workerGroup)
                .localAddress(port)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, backLog)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new WriteTimeoutHandler(writeTimeOut, TimeUnit.MILLISECONDS))
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(maxContentLength))
                                .addLast(new HttpServerHandler(app));
                    }
                });
    }

    @Override
    public void stopThread() {

    }
}
