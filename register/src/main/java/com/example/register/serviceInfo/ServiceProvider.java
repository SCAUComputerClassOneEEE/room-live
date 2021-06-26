package com.example.register.serviceInfo;


import com.google.common.util.concurrent.AtomicDouble;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceProvider implements Serializable {

    public enum TypeServiceProvider {
        Client,
        Server,
    }

    private String appName;

    private InstanceInfo info;

    private TypeServiceProvider type;

    private AtomicInteger connectingInt; // 正在连接数

    private AtomicInteger historyInt; // 历史连接数

    private AtomicDouble avgAccess; // 平均响应时间

    public ServiceProvider() {
        connectingInt = new AtomicInteger(0);
        historyInt = new AtomicInteger(0);
        avgAccess = new AtomicDouble(0.0);
    }

    public ServiceProvider(String appName, String host, int port, TypeServiceProvider type) {
        this.appName = appName;
        info = new InstanceInfo(host, port);
        this.type = type;
        connectingInt = new AtomicInteger(0);
        historyInt = new AtomicInteger(0);
        avgAccess = new AtomicDouble(0.0);
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public InstanceInfo getInfo() {
        return info;
    }

    public void setInfo(InstanceInfo info) {
        this.info = info;
    }

    public TypeServiceProvider getType() {
        return type;
    }

    public void setType(TypeServiceProvider type) {
        this.type = type;
    }

    /*
     *
     * LB
     */

    public static class LeastConnectionComparator implements Comparator<ServiceProvider> {
        @Override
        public int compare(ServiceProvider o1, ServiceProvider o2) {
            return o1.connectingInt.get() > o2.connectingInt.get() ? 0 : 1;
        }
    }

    public static class FastestResponseComparator implements Comparator<ServiceProvider> {
        @Override
        public int compare(ServiceProvider o1, ServiceProvider o2) {
            return o1.avgAccess.get() > o2.avgAccess.get() ? 0 : 1;
        }
    }

    public int incrementConnectingInt() {
        historyInt.incrementAndGet();
        return connectingInt.incrementAndGet();
    }

    public int decrementConnectingInt() { return connectingInt.decrementAndGet(); }

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
        return info.equals(that.info) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(info, type);
    }

    public int mask() {
        String host = info.host();
        int port = info.port();
        return (host.hashCode() << 4) ^ portInterleaveHash(port);
    }

    private int portInterleaveHash(int port) {
        int i = (port & 0x8) >> 3; // 1000 >> 3 = 1
        int i1 = (port & 0x80) >> 6; // 1000 0000 >> 6 = 1
        int i2 = (port & 0x800) >> 9; // 1000 0000 0000 >> 9 = 1
        int i3 = (port & 0x8000) >> 12; // 1000 0000 0000 0000 >> 12 = 1
        return i | i1 | i2 | i3;
    }

}
