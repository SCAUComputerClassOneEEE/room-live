package com.example.register.serviceInfo;


import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicInteger;

public class ServiceProvider {

    public static enum TypeServiceProvider {
        Client,
        Server,
    }

    private InstanceInfo info;

    private TypeServiceProvider type;

    private final AtomicInteger connectingInt = new AtomicInteger(0); // 正在连接数

    private final AtomicInteger historyInt = new AtomicInteger(0); // 历史连接数

    private final AtomicDouble avgAccess = new AtomicDouble(0.0); // 平均响应时间

    public ServiceProvider(String host, int port, TypeServiceProvider type) {
        info = new InstanceInfo(host, port);
        this.type = type;
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

    public int getThisHash() {
        String host = info.host();
        int port = info.port();
        return (host.hashCode() << 4) ^ (port & 0xf);
    }
}
