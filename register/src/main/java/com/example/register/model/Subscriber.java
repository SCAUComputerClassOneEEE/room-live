package com.example.register.model;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 某个Identity的订阅者，他将通过长轮询进行table数据的更新
 * 订阅者一定是Client
 */
public class Subscriber {
    private SocketChannel socketChannel;
    private FullHttpRequest request; // 这个请求暂存在这里，table有数据更新时，将重放这个请求。

    public Subscriber(SocketChannel socketChannel, FullHttpRequest request) {
        this.socketChannel = socketChannel;
        this.request = request;
    }

    /**
     * 请求超时，或回推数据
     */
    public void pushback() {

    }
}
