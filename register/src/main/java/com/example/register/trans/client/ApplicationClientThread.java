package com.example.register.trans.client;

import com.example.register.process.Application;
import com.example.register.process.RegistryClient;
import com.example.register.process.RegistryServer;
import com.example.register.trans.ApplicationThread;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class ApplicationClientThread extends ApplicationThread<Bootstrap, Channel> {

    private RegistryClient app;

    private void start() {
        EventLoopGroup loopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new HttpClientHandler(app));
                        }
                    });
            ChannelFuture future = bootstrap.connect("localhost",6668).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e){

        } finally {
            loopGroup.shutdownGracefully();
        }

    }

    @Override
    public void init(Application application) throws Exception {
        if (application instanceof RegistryClient) {
            app = (RegistryClient) application;
        } else {
            throw new Exception("Client application thread init error.");
        }
        bootstrap = new Bootstrap();
    }

    @Override
    public void run() {
        start();
    }
}
