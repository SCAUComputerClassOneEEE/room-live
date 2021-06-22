package com.example.register.trans.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @author hiluyx
 * @since 2021/6/19 16:49
 **/
public class HttpTaskResult extends DefaultFullHttpResponse {

    public HttpTaskResult(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }
}
