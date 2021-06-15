package com.example.register.trans.server;

import com.example.register.process.Application;
import com.example.register.process.RegistryServer;
import com.example.register.trans.ApplicationThread;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author hiluyx
 * @since 2021/6/9 22:02
 **/
public class ApplicationServerThread extends ApplicationThread<ServerBootstrap, ServerChannel> {

    private RegistryServer app;

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try{
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new HttpServerHandler(app));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(6668).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            group.shutdownGracefully();
        }

    }

    @Override
    public void init(Application application) throws Exception {
        if (application instanceof RegistryServer) {
            app = (RegistryServer) application;
        } else {
            throw new Exception("server application thread init error.");
        }
        bootstrap = new ServerBootstrap();
    }

    @Override
    public void run() {
        start();
    }
}
