package com.example.register.trans.server;

import com.example.register.process.Application;
import com.example.register.process.NameCenterPeerProcess;
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
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * @author hiluyx
 * @since 2021/6/9 22:02
 **/
public class ApplicationServer extends ApplicationThread<ServerBootstrap, ServerChannel> {

    private NameCenterPeerProcess app;
    private final EventLoopGroup boosGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(1);

    private static final ServerScanRunnable runnable = new ServerScanRunnable();

    protected static class ServerScanRunnable implements Runnable{
        ApplicationServer server;

        void setServer(ApplicationServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            final ServerBootstrap bootstrap = (ServerBootstrap) server.bootstrap;
            NameCenterPeerProcess app = server.app;
            try {
                ChannelFuture bind = bootstrap.bind().sync();
                if (bind.isSuccess()) {
                    while (true) {
                        try {

                        } catch (Exception e) {
                            break;
                        }
                    }
                } else {
                    server.stopThread();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                server.stopThread();
            }

        }
    }

    public ApplicationServer(Application application, ServiceProvidersBootConfig config) throws Exception {
        super(runnable);
        runnable.setServer(this);
        init(application, config);
    }

    @Override
    protected void init(Application application, ServiceProvidersBootConfig config) throws Exception {
        if (this.isAlive()) return;

        if (application instanceof NameCenterPeerProcess) {
            app = (NameCenterPeerProcess) application;
        } else {
            throw new Exception("server application thread init error.");
        }
        bootstrap = new ServerBootstrap();
        final Integer writeTimeOut = config.getWriteTimeOut();
        final int maxContentLength = config.getMaxContentLength();
        final int backLog = config.getBackLog();
        int port = config.getServerPort();
        ((ServerBootstrap) bootstrap)
                .group(boosGroup, workerGroup)
                .localAddress(port)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, backLog)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new WriteTimeoutHandler(writeTimeOut, TimeUnit.MILLISECONDS)) // 返回408
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(maxContentLength))
                                .addLast(new HttpServerHandler(app));
                    }
                });
    }

    @Override
    public void stopThread() {
        super.stopThread();
        if (!boosGroup.isTerminated())
            boosGroup.shutdownGracefully();
        if (!workerGroup.isTerminated())
        workerGroup.shutdownGracefully();
    }
}
