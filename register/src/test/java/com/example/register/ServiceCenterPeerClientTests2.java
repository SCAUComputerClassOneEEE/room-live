package com.example.register;

import com.example.register.trans.server.HttpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ServiceCenterPeerClientTests2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        ChannelFuture sync = bootstrap
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .localAddress(8000)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new WriteTimeoutHandler(3000, TimeUnit.MILLISECONDS)) // 返回408
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                .addLast(new Server0())
                                .addLast(new Server());
                    }
                }).bind().sync();
        if (sync.isSuccess()) {
            System.out.println("...");
        }
    }

    static class Server0 extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println(msg);
            ctx.fireChannelRead(msg);
        }
    }

    static class Server extends SimpleChannelInboundHandler<FullHttpRequest> {
        FullHttpRequest request;

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpResponse) throws Exception {
            request = fullHttpResponse;
//            channelHandlerContext.channel().closeFuture().sync();
            channelHandlerContext
                    .writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
                    .addListener(ChannelFutureListener.CLOSE);
//            channelHandlerContext.channel().closeFuture();
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelRegistered: " + request);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelUnregistered: " + request);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelActive: " + request);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelInactive: " + request);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelReadComplete: ");

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("exceptionCaught: " + request);
        }
    }

}
