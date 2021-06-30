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

public class HttpClientInBoundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private RegistryClient app;
    private final AttributeKey<String> taskId = AttributeKey.valueOf("taskId");

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
    protected void channelRead0(ChannelHandlerContext ctx,
                                FullHttpResponse fullHttpResponse/*逃不出这个方法，不需要retain*/) throws Exception {

        final String taskIdHeader = fullHttpResponse.headers().get("taskId");

        if (StringUtil.isNullOrEmpty(taskIdHeader))
            throw new RuntimeException("taskId is null or empty");

        final Attribute<String> attr = ctx.channel().attr(taskId);
        final String taskIdUUID = attr.get();
        final ConcurrentHashMap<String, HttpTaskCarrierExecutor> taskMap = HttpTaskExecutorPool.taskMap;
        final HttpTaskCarrierExecutor executor = taskMap.get(taskIdUUID);

        ObjectUtil.checkNotNull(executor, "map haven't this task" + taskId);
        executor.setResult(new HttpTaskCarrierExecutor.TaskExecuteResult(fullHttpResponse));

        /*任务完成，执行监听器的逻辑*/
        ctx.channel().closeFuture().addListener(executor.getListener());

        /*
         * 池清理
         * */
        taskMap.remove(taskIdUUID);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final Attribute<String> attr = ctx.channel().attr(taskId);
        final String taskIdUUID = attr.get();
        final HttpTaskCarrierExecutor executor = HttpTaskExecutorPool.taskMap.get(taskIdUUID);
        ResultType error = ResultType.CONNECT_TIME_OUT;
        if (cause instanceof ReadTimeoutException) {
            // read time out
            error = ResultType.READ_TIME_OUT;
        } else if (cause instanceof RuntimeException){
            // runtime
            error = ResultType.RUNTIME_EXCEPTION;
        }else {
            super.exceptionCaught(ctx, cause);
        }
        HttpTaskCarrierExecutor.TaskExecuteResult taskExecuteResult = new HttpTaskCarrierExecutor.TaskExecuteResult(error);
        taskExecuteResult.setCause(cause);
        executor.setResult(taskExecuteResult);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }
}
