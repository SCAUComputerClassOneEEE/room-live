package com.example.register.trans.client;

import com.example.register.process.Application;
import com.example.register.process.RegistryClient;
import com.example.register.trans.ApplicationThread;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 *
 * queue for task
 * 独立的任务 tasker 处理线程。
 */
public class ApplicationClient extends ApplicationThread<Bootstrap, Channel> {

    private RegistryClient app;
    private final EventLoopGroup clientNetWorkLoop = new NioEventLoopGroup();

    @Override
    public void init(Application application) throws Exception {
        if (application instanceof RegistryClient) {
            app = (RegistryClient) application;
        } else {
            throw new Exception("Client application thread init error.");
        }
        bootstrap = new Bootstrap();
        final Bootstrap boots = (Bootstrap)bootstrap;
        boots.group(clientNetWorkLoop)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                .addLast(new HttpClientHandler(app));
                    }
                });
    }

    @Override
    public void stopThread() {

    }

}
