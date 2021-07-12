package com.example.register.serviceInfo;


import com.example.register.utils.JSONUtil;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@ToString
public class ServiceProvider implements Serializable, Comparable<ServiceProvider>, Coverable<ServiceProvider> {

    // register instance info
    private String appName;
    private String host;
    private int port;
    private final List<MethodInstance> methodMappingList;

    private final String mask;
    private Timestamp lastRenewStamp;
    private final AtomicInteger connectingInt; // 正在连接数
    private final AtomicInteger historyInt; // 历史连接数
    private final AtomicDouble avgAccess; // 平均响应时间

    public ServiceProvider() {
        lastRenewStamp = new Timestamp(new Date().getTime());
        connectingInt = new AtomicInteger(0);
        historyInt = new AtomicInteger(0);
        avgAccess = new AtomicDouble(0.0);
        mask = String.valueOf(mask())/*UUID.randomUUID().toString()*/;
        methodMappingList = new LinkedList<>();
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
        methodMappingList = new LinkedList<>();
    }

    public void addMethod(MethodInstance m) {
        methodMappingList.add(m);
    }

    public void removeMethod(String name) {
        if (name == null || name.equals("")) return;
        for (MethodInstance methodInstance : methodMappingList) {
            if (methodInstance.getName().equals(name)) {
                return;
            }
        }
    }

    public List<MethodInstance> getMethodMappingList() {
        return methodMappingList;
    }

    public AtomicInteger getHistoryInt() {
        return historyInt;
    }

    public Timestamp getLastRenewStamp() {
        return lastRenewStamp;
    }

    public void setLastRenewStamp(Timestamp lastRenewStamp) {
        this.lastRenewStamp = lastRenewStamp;
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

    public int getPort() {
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
        int port = getPort();
        return Objects.hash(host, port);
    }

    private int portInterleaveHash(int port) {
        int i = (port & 0x8) >> 3; // 1000 >> 3 = 1
        int i1 = (port & 0x80) >> 6; // 1000 0000 >> 6 = 1
        int i2 = (port & 0x800) >> 9; // 1000 0000 0000 >> 9 = 1
        int i3 = (port & 0x8000) >> 12; // 1000 0000 0000 0000 >> 12 = 1
        return i | i1 | i2 | i3;
    }
}
