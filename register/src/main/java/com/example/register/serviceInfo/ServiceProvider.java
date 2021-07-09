package com.example.register.serviceInfo;


import com.example.register.utils.JSONUtil;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceProvider implements Serializable, Cloneable, Comparable<ServiceProvider>, Coverable<ServiceProvider> {

    private String appName;
    private String mask;
    private String host;
    private int port;

    private transient Timestamp lastRenewStamp;
    private AtomicInteger connectingInt; // 正在连接数
    private AtomicInteger historyInt; // 历史连接数
    private AtomicDouble avgAccess; // 平均响应时间

    public ServiceProvider() {
        lastRenewStamp = new Timestamp(new Date().getTime());
    }

    public ServiceProvider(String appName, String host, int port) {
        this.host = host;
        this.port = port;
        this.appName = appName;
        connectingInt = new AtomicInteger(0);
        historyInt = new AtomicInteger(0);
        avgAccess = new AtomicDouble(0.0);
        lastRenewStamp = new Timestamp(new Date().getTime());
        mask = String.valueOf(mask())/*UUID.randomUUID().toString()*/;
    }

    public boolean isPeer() {
        return appName.equals(ServiceApplicationsTable.SERVER_PEER_NODE);
    }

    public String getMask() { return mask; }

    public String getAppName() {
        return appName;
    }

    public String getHost() {
        return host;
    }

    public int port() {
        return port;
    }

    public AtomicInteger getConnectingInt() { return connectingInt; }

    public AtomicDouble getAvgAccess() { return avgAccess; }

    /**
     * 只在renew的函数中发生
     * */
    public void newVersion() {
        lastRenewStamp = new Timestamp(new Date().getTime());
    }

    public long heartBeatGap() {
        return System.currentTimeMillis() - lastRenewStamp.getTime();
    }

    /*版本比较器*/
    @Override
    public int compareTo(ServiceProvider o) {
        return lastRenewStamp.compareTo(o.lastRenewStamp);
    }

    @Override
    public boolean cover(ServiceProvider newCover) {
        if (newCover == null)
            return false;
        if (lastRenewStamp.after(newCover.lastRenewStamp))
            return false;
        if (!mask.equals(newCover.mask))
            return false;
        if (!appName.equals(newCover.appName))
            return false;
        connectingInt.set(newCover.connectingInt.get());
        historyInt.set(newCover.historyInt.get());
        avgAccess.set(newCover.avgAccess.doubleValue());
        return true;
    }

    public void incrementConnectingInt() {
        historyInt.incrementAndGet();
        connectingInt.incrementAndGet();
    }

    public void decrementConnectingInt() {
        connectingInt.decrementAndGet();
    }

    public double fixAccessAvg(double newAccess) {
        final int nowHistoryInt = historyInt.get();
        final double oldAvg = avgAccess.doubleValue();
        final double newAvg = (oldAvg * (nowHistoryInt - 1) + newAccess) / nowHistoryInt;
        if (avgAccess.compareAndSet(oldAvg, newAvg)) {
            return avgAccess.doubleValue();
        }
        else {
            return fixAccessAvg(newAccess);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceProvider that = (ServiceProvider) o;
        return mask.equals(that.mask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, mask);
    }

    public int mask() {
        String host = getHost();
        int port = port();
        return (host.hashCode() << 4) ^ portInterleaveHash(port);
    }

    private int portInterleaveHash(int port) {
        int i = (port & 0x8) >> 3; // 1000 >> 3 = 1
        int i1 = (port & 0x80) >> 6; // 1000 0000 >> 6 = 1
        int i2 = (port & 0x800) >> 9; // 1000 0000 0000 >> 9 = 1
        int i3 = (port & 0x8000) >> 12; // 1000 0000 0000 0000 >> 12 = 1
        return i | i1 | i2 | i3;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ServiceProvider cSP = (ServiceProvider)super.clone();
        cSP.mask = mask;
        cSP.appName = appName;
        cSP.lastRenewStamp = lastRenewStamp;
        cSP.avgAccess = new AtomicDouble(avgAccess.doubleValue());
        cSP.connectingInt = new AtomicInteger(connectingInt.get());
        cSP.historyInt = new AtomicInteger(historyInt.get());
        return cSP;
    }

    @SneakyThrows
    @Override
    public String toString() {
        return "service provider: " +
                "appName<" + appName + ">, " +
                "mask<" + mask + ">, " +
                "info<" + host + ", " + port + ", " + lastRenewStamp + ">";
    }
}
