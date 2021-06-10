package com.example.register.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author hiluyx
 * @since 2021/6/9 22:02
 **/
public class RegisterServer {
    public static void main(String[] args) {
        EventLoopGroup boosGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try{
            bootstrap.group(boosGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(0,0,10, TimeUnit.SECONDS))
                                    .addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(6668).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public static class NettyServerHandler extends ChannelInboundHandlerAdapter {
        private int lossConnectCount = 0;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("客户端发送的数据是:" +buf.toString(CharsetUtil.UTF_8));

        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent){
                IdleStateEvent event = (IdleStateEvent)evt;
                if (event.state()== IdleState.ALL_IDLE){
                    lossConnectCount ++;
                    if (lossConnectCount > 2){
                        System.out.println("关闭这个不活跃通道！");
                        ctx.channel().close();
                    }
                }
            }else {
                super.userEventTriggered(ctx,evt);
            }
        }
    }
}
