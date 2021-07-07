package com.example.register.trans.server;

import com.example.register.process.Application;
import com.example.register.process.NameCenterPeerProcess;
import com.example.register.process.RegistryServer;
import com.example.register.serviceInfo.ServiceProvider;
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
    private final EventLoopGroup boosGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(1);

    private static final ServerScanRunnable runnable = new ServerScanRunnable();

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
                logger.info("server bind " + bootstrap.config().localAddress().toString() + " " + bind.isSuccess());
                if (bind.isSuccess()) {
                    timerTaskScan(app);
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            timerTaskScan(app);
                        }
                    }, heartBeatIntervals);
                } else {
                    server.stopThread();
                }
            } catch (Exception e) {
                logger.error("server runnable error: " + e.getMessage());
            } finally {
                logger.info("server end");
                server.stopThread();
            }

        }

        void timerTaskScan(NameCenterPeerProcess server) {
            Map<String, Set<ServiceProvider>> noBeatMap = server.scan();

            noBeatMap.forEach((appName, apps)->{
                for (ServiceProvider app : apps) {
                    logger.debug("have no new heat " + app.toString());
                    server.offline(app, false, true, false);
                }
            });
        }
    }

    public ApplicationServer(Application application, ServiceProvidersBootConfig config) throws Exception {
        super(runnable);
        runnable.init(this, config.getHeartBeatIntervals());
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
