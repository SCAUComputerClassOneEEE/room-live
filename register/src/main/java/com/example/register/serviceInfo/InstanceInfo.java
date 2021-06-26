package com.example.register.serviceInfo;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * 实例信息：
 * 1. 通信地址
 * 2. 版本号
 * 3. 。。。
 */
public class InstanceInfo implements Comparable<InstanceInfo>{

    private ConcatAddress instAdr;
    private final Timestamp version;

    public InstanceInfo() {
        version = new Timestamp(new Date().getTime());
    }

    public InstanceInfo(String host, int port) {
        version = new Timestamp(new Date().getTime());
        instAdr = new ConcatAddress(host, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceInfo that = (InstanceInfo) o;
        return Objects.equals(instAdr, that.instAdr) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instAdr, version);
    }

    public String host() {
        return instAdr.ip;
    }

    public int port() {
        return instAdr.port;
    }

    public ConcatAddress getInstAdr() {
        return instAdr;
    }

    public void setInstAdr(ConcatAddress instAdr) {
        this.instAdr = instAdr;
    }

    public long getVersion() {
        return version.getTime();
    }

    @Override
    public int compareTo(InstanceInfo o) {
        return this.version.compareTo(o.version);
    }

    public static class ConcatAddress {
        public String ip;
        public int port;

        public ConcatAddress() {
        }

        public ConcatAddress(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConcatAddress that = (ConcatAddress) o;
            return port == that.port && ip.equals(that.ip);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ip, port);
        }
    }
}
