package com.example.register.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表格存储
 */
public class NodeTable {
    private static final ConcurrentHashMap<Identify, List<RemoteAddress>> map = new ConcurrentHashMap<>();
    private static volatile int initFlagInt = 1;
    private static final NodeTable table = new NodeTable();

    public static NodeTable getTable() {
        return NodeTable.table;
    }

    public void remove(Identify id) {
        map.remove(id);
    }

    public void removeOneRA(Identify id, RemoteAddress ra) {
        if (doBeforeCondition(id, ra)) return;
        RemoteAddress tobeRemoved = null;
        List<RemoteAddress> remoteAddresses = map.get(id);
        if (remoteAddresses == null) return;
        for (RemoteAddress ra1 : remoteAddresses) {
            if (ra1.equals(ra)) {
                tobeRemoved = ra1;
            }
        }
        if (tobeRemoved == null) return;
        remoteAddresses.remove(tobeRemoved);
    }

    public void putV(Identify id, RemoteAddress ra) {
        if (doBeforeCondition(id, ra)) return;

        List<RemoteAddress> rAs = map.get(id);
        if (rAs == null) {
            rAs = initNdsV(id);
        }
        rAs.add(ra);
    }

    private boolean doBeforeCondition(Identify id, RemoteAddress n) {
        if (id == null | n == null) return true;
        if (id.getAppName() == null || id.getAppName().equals("")) return true;

        return false;
    }

    private List<RemoteAddress> initNdsV(Identify id) {
        List<RemoteAddress> rAs = map.get(id);
        if (rAs == null) {
            synchronized (this) {
                rAs = new ArrayList<>();
                map.put(id, rAs);
                initFlagInt <<= 1; // volatile字段防止ns的指针空内存
            }
        }
        return rAs;
    }

    public List<RemoteAddress> getV(Identify id) {
        if (id == null) return null;
        List<RemoteAddress> remoteAddresses = map.get(id);
        if (remoteAddresses == null) return null;
        if (remoteAddresses.isEmpty()) return null;
        return remoteAddresses;
    }

}
