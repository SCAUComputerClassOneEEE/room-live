package com.example.register.serviceInfo;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 *
 * map appName --> ServiceProvider
 *
 */
public class ServiceApplicationsTable {

    private static final String SERVER_PEER_NODE = "server-peer-node-service";

    private ConcurrentHashMap<String, ConcurrentHashMap<Long, ServiceProvider>> doubleMarkMap = new ConcurrentHashMap<>();

    public ServiceApplicationsTable(ServiceProvidersBootConfig config, String selfAppName) {
        ServiceProvider selfNode = config.getSelfNode();
        ConcurrentHashMap<Long, ServiceProvider> selfServiceMap = new ConcurrentHashMap<>();
        selfServiceMap.put(new Date().getTime(), selfNode);
        doubleMarkMap.put(selfAppName, selfServiceMap);

        ConcurrentHashMap<Long, ServiceProvider> appServiceMap = new ConcurrentHashMap<>();
        config.getOthersPeerServerNodes().forEach((op)-> appServiceMap.put(new Date().getTime(), op));
        doubleMarkMap.put(SERVER_PEER_NODE, appServiceMap);
    }

    public void remove(ServiceProvider.TypeServiceProvider type, String appName) {

    }

    public void put(String appName, String host, int port, ServiceProvider.TypeServiceProvider type) {

    }
}
