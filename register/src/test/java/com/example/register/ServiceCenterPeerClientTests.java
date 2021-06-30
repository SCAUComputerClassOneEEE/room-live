package com.example.register;

import com.example.register.process.RegistryClient;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.trans.client.HttpClientInBoundHandler;
import com.example.register.trans.client.HttpClientOutBoundHandler;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


class ServiceCenterPeerClientTests {
    private static FullHttpResponse sfullHttpResponse;

    public static void main(String[] args) throws IOException, InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3) // connect time out 3s
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ReadTimeoutHandler(1, TimeUnit.SECONDS)) // read time out 5s
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                .addLast(new Client())
                                .addLast(new Client2());
                    }
                });
        for (int i = 0; i < 1; i++) {
            ChannelFuture localhost;
            localhost = bootstrap.connect("localhost", 8080).await();
            if (localhost.isSuccess()) {
                System.out.println("connect success");
                DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
                defaultFullHttpRequest.headers().add("taskId", UUID.randomUUID().toString());
                localhost.channel().writeAndFlush(defaultFullHttpRequest);
            } else {
                System.out.println("!connect success");
            }
        }
        System.out.println("sssss"+sfullHttpResponse);
    }

    static class Client2 extends ChannelOutboundHandlerAdapter {
        private final AttributeKey<String> taskId = AttributeKey.valueOf("taskId");

        public Client2() {

        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest msg1 = (FullHttpRequest) msg;

                String taskIdStr = msg1.headers().get("taskId");
                Attribute<String> attr = ctx.channel().attr(taskId);
                attr.set(taskIdStr);
            }

            super.write(ctx, msg, promise);
        }
    }

    static class Client extends SimpleChannelInboundHandler<FullHttpResponse> {
        private final AttributeKey<String> taskId = AttributeKey.valueOf("taskId");
        FullHttpResponse response;

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse) throws Exception {
            response = fullHttpResponse;
            sfullHttpResponse = fullHttpResponse;
            System.out.println("channelRead0: " + fullHttpResponse);
            System.out.println(channelHandlerContext.channel().attr(taskId).get());
            channelHandlerContext.channel().closeFuture();

        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelRegistered: " + response);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelUnregistered: " + response);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelActive: " + response);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelInactive: " + response);
            throw new RuntimeException("............");
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelReadComplete: " + response);
//            ReferenceCountUtil.release(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("exceptionCaught: " + cause.getMessage());
            //cause.printStackTrace();
        }
    }

}
