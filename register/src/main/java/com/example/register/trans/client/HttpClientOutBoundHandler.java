package com.example.register.trans.client;

import com.example.register.process.RegistryClient;
import com.example.register.utils.HttpTaskExecutorPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientOutBoundHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientOutBoundHandler.class);

    private RegistryClient app;
    private final AttributeKey<String> taskId = AttributeKey.valueOf("taskId");

    public HttpClientOutBoundHandler(RegistryClient app) {
        this.app = app;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest msg1 = (FullHttpRequest) msg;
            String taskIdUUID = msg1.headers().get("taskId");
            if (StringUtil.isNullOrEmpty(taskIdUUID))
                HttpTaskExecutorPool.taskMap.get(taskIdUUID)
                        .fail(ResultType.RUNTIME_EXCEPTION, new RuntimeException("no taskId"));
            else {
                logger.info(taskIdUUID + " write out");
                ctx.channel().attr(taskId).set(taskIdUUID);
                super.write(ctx, msg, promise);
            }
        }
    }
}
