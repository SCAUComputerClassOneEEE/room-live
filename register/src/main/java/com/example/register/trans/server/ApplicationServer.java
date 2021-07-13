package com.example.register.trans.server;

import com.example.register.process.Application;
import com.example.register.process.NameCenterPeerProcess;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.process.ApplicationBootConfig;
import com.example.register.trans.ApplicationThread;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author hiluyx
 * @since 2021/6/9 22:02
 **/
public class ApplicationServer extends ApplicationThread<ServerBootstrap, ServerChannel> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServer.class);

    private NameCenterPeerProcess app;
    private final EventLoopGroup boosGroup;

    private static final ServerScanRunnable runnable = new ServerScanRunnable();

    public ApplicationServer(Application application, ApplicationBootConfig config) {
        super(runnable, config.getWorkerNThread());
        boosGroup = new NioEventLoopGroup(config.getBossNThread());
        runnable.init(this, config.getHeartBeatIntervals());
        init(application, config);
    }

    @Override
    protected void init(Application application, ApplicationBootConfig config) {
        if (this.isAlive()) return;

        app = (NameCenterPeerProcess) application;
        bootstrap = new ServerBootstrap();
        final Integer writeTimeOut = config.getWriteTimeOut();
        final Integer readTimeOut = config.getReadTimeOut();
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
                                .addLast(new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS)) // 返回408
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
    }

    protected static class ServerScanRunnable implements Runnable{
        ApplicationServer server;
        int heartBeatIntervals;

        void init(ApplicationServer server, int heartBeatIntervals) {
            this.server = server;
            this.heartBeatIntervals = heartBeatIntervals;
        }

        @Override
        public void run() {
            final ServerBootstrap bootstrap = (ServerBootstrap) server.bootstrap;
            NameCenterPeerProcess app = server.app;
            try {
                ChannelFuture bind = bootstrap.bind().sync();
                server.future = bind;
                logger.info("Server bind " + bootstrap.config().localAddress().toString() + " " + bind.isSuccess());
                if (bind.isSuccess()) {
                    while (server.app.flagForStop()) {
                        logger.debug("Server cycle before sleep");
                        /*before sleep*/
                        long s = System.currentTimeMillis();
                        timerTaskScan(app);
                        long e = System.currentTimeMillis();
                        Thread.sleep(heartBeatIntervals - (e-s));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Server runnable error: " + e.getMessage());
            } finally {
                logger.info("Server end");
                server.stopThread();
            }

        }

        void timerTaskScan(NameCenterPeerProcess server) {
            Map<String, Set<ServiceProvider>> noBeatMap = server.scan();

            noBeatMap.forEach((appName, apps)->{
                for (ServiceProvider app : apps) {
                    if (app.equals(server.getMyself())) continue;
                    logger.debug("have no new heat " + app);
                    server.offline(app, false, true, false);
                }
            });
        }
    }

}
