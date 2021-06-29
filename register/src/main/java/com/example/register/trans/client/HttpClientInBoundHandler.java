package com.example.register.trans.client;

import com.example.register.utils.HttpTaskExecutorPool;
import com.example.register.process.RegistryClient;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.ConcurrentHashMap;

public class HttpClientInBoundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private RegistryClient app;

    public HttpClientInBoundHandler(RegistryClient app) {
        this.app = app;
    }

    /**
     *
     * 根据server返回的结果的内容进行处理
     * 200 操作成功并完成所有流程
     * 300
     * 404
     * 500
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse) throws Exception {
        final ConcurrentHashMap<String, HttpTaskCarrierExecutor> taskMap = HttpTaskExecutorPool.taskMap;
        String taskId = fullHttpResponse.headers().get("taskId");

        if (StringUtil.isNullOrEmpty(taskId))
            throw new RuntimeException("taskId is null or empty");
        HttpTaskCarrierExecutor executor = taskMap.get(taskId);

        ObjectUtil.checkNotNull(executor, "map haven't this task" + taskId);
        executor.setResult(new HttpTaskCarrierExecutor.TaskExecuteResult(fullHttpResponse));

        taskMap.remove(taskId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            // read time out
            HttpClientOutBoundHandler taskOutHandler = (HttpClientOutBoundHandler)ctx.pipeline().get("task out handler");
            HttpTaskCarrierExecutor executor = HttpTaskExecutorPool.taskMap.get(taskOutHandler.getTaskId());
            executor.setResult(new HttpTaskCarrierExecutor.TaskExecuteResult(0));
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
