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
public class InstanceInfo implements Comparable<InstanceInfo>, Cloneable, Coverable<InstanceInfo> {

    private ConcatAddress instAdr;
    private Timestamp version;

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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        InstanceInfo cIf = (InstanceInfo) super.clone();
        cIf.instAdr = (ConcatAddress) instAdr.clone();
        cIf.version = (Timestamp) version.clone();
        return cIf;
    }

    @Override
    public boolean cover(InstanceInfo instanceInfo) {
        if (instanceInfo.version.before(version)) return false;
        if (!instAdr.cover(instanceInfo.instAdr)) return false;
        version = new Timestamp(instanceInfo.version.getTime());
        return true;
    }

    public static class ConcatAddress implements Cloneable, Coverable<ConcatAddress> {
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
        protected Object clone() throws CloneNotSupportedException {
            ConcatAddress clone = (ConcatAddress)super.clone();
            clone.ip = ip;
            clone.port = port;
            return clone;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ip, port);
        }

        @Override
        public boolean cover(ConcatAddress concatAddress) {
            if (concatAddress == null) return false;
            ip = concatAddress.ip;
            port = concatAddress.port;
            return true;
        }
    }
}
