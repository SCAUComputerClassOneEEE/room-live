package com.example.register.trans.client;

import com.example.register.utils.HttpTaskExecutorPool;
import com.example.register.process.RegistryClient;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class HttpClientInBoundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private RegistryClient app;
    private final AttributeKey<String> taskId = AttributeKey.valueOf("taskId");
    private FullHttpResponse response;

    public HttpClientInBoundHandler(RegistryClient app) {
        this.app = app;
    }

    /*
    * close channel 动作两端进行
    * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                FullHttpResponse fullHttpResponse) throws Exception {
        final String taskIdHeader = fullHttpResponse.headers().get("taskId");

        if (StringUtil.isNullOrEmpty(taskIdHeader))
            throw new RuntimeException("taskId is null or empty");
        response = fullHttpResponse;

        ctx.channel().closeFuture().sync();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final Attribute<String> attr = ctx.channel().attr(taskId);
        final String taskIdUUID = attr.get();
        final HttpTaskCarrierExecutor executor = HttpTaskExecutorPool.taskMap.get(taskIdUUID);
        ResultType error;
        if (cause instanceof ReadTimeoutException) {
            // read time out
            error = ResultType.READ_TIME_OUT;
        } else if (cause instanceof RuntimeException){
            // runtime
            error = ResultType.RUNTIME_EXCEPTION;
        } else {
            error = ResultType.UNKNOWN;
        }

        executor.syncFail(error, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Attribute<String> attr = ctx.channel().attr(taskId);
        final String taskIdUUID = attr.get();
        final HttpTaskCarrierExecutor executor = HttpTaskExecutorPool.taskMap.get(taskIdUUID);

        if (executor == null) {
            throw new RuntimeException("map haven't this task: " + taskIdUUID);
        }

        executor.syncSuccess(response);
    }
}
