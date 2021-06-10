package com.example.register.model;

import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;

@Data
public class RemoteAddress {
    private String host;
    private int port;

    public RemoteAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
