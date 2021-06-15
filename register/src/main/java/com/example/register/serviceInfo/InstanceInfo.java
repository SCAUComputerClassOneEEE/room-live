package com.example.register.serviceInfo;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * 实例信息：
 * 1. 通信地址
 * 2. 版本号
 * 3. 。。。
 */
public class InstanceInfo {

    private ConcatAddress instAdr;
    private final AtomicLong version = new AtomicLong(0);




    public ConcatAddress getInstAdr() {
        return instAdr;
    }

    public void setInstAdr(ConcatAddress instAdr) {
        this.instAdr = instAdr;
    }

    public AtomicLong getVersion() {
        return version;
    }

    public static class ConcatAddress {
        public String ip;
        public int port;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}