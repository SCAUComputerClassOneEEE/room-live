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
        // 创建对应的 线程池
        // 创建Boss group
        EventLoopGroup boosGroup = new NioEventLoopGroup(1);
        // 创建 workgroup
        EventLoopGroup workGroup = new NioEventLoopGroup();
        // 创建对应的启动类
        ServerBootstrap bootstrap = new ServerBootstrap();
        try{
            // 设置相关的配置信息
            bootstrap.group(boosGroup,workGroup) // 设置对应的线程组
                    .channel(NioServerSocketChannel.class) // 设置对应的通道
                    .option(ChannelOption.SO_BACKLOG,1024) // 设置线程的连接个数
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 设置
                        /**
                         * 给pipeline 设置处理器
                         * @param socketChannel
                         * @throws Exception
                         */
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(0,0,10, TimeUnit.SECONDS))
                                    .addLast(new NettyServerHandler());
                        }
                    });
            System.out.println("服务启动了....");
            // 绑定端口  启动服务
            ChannelFuture channelFuture = bootstrap.bind(6668).sync();
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            // 优雅停服
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public static class NettyServerHandler extends ChannelInboundHandlerAdapter {
        private int lossConnectCount = 0;
        /**
         * 读取客户端发送来的数据
         * @param ctx ChannelHandler的上下文对象 有管道 pipeline 通道 channel 和 请求地址 等信息
         * @param msg 客户端发送的具体数据
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("客户端发送的数据是:" +buf.toString(CharsetUtil.UTF_8));

        }

        /**
         * 读取客户端发送数据完成后的方法
         *    在本方法中可以发送返回的数据
         * @param ctx
         * @throws Exception
         */
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
