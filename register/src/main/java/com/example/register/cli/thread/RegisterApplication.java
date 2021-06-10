package com.example.register.cli.thread;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

//@SpringBootApplication
public class RegisterApplication {

    public static void main(String[] args) {
        // 客户端就只需要创建一个 线程组了
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        // 创建 启动器
        Bootstrap bootstrap = new Bootstrap();
        try{
            // 设置相关的参数
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(0,0,10, TimeUnit.SECONDS))
                                    .addLast(new NettyClientHandler());
                        }
                    });
            // 连接服务
            ChannelFuture future = bootstrap.connect("localhost",6668).sync();
            // 对服务关闭 监听
            future.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            loopGroup.shutdownGracefully();
        }

    }

    public static class NettyClientHandler extends ChannelInboundHandlerAdapter {

        /**
         * 连接上服务的回调方法
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 发送数据
//            System.out.println("连接上了 服务器....");
//            ctx.writeAndFlush(Unpooled.copiedBuffer("哈哈 你好呀!!!", CharsetUtil.UTF_8));
        }

        /**
         * 读取服务端返回的信息
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            ByteBuf buf  = (ByteBuf) msg;
//            System.out.println("服务端返回的信息：" + buf.toString(CharsetUtil.UTF_8));
            //ctx.writeAndFlush(Unpooled.copiedBuffer("哈哈 你好呀!!!", CharsetUtil.UTF_8));
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.ALL_IDLE) {
                    ctx.channel().writeAndFlush(Unpooled.copiedBuffer("heartbeat", CharsetUtil.UTF_8));
                }
            }
        }
    }

}
