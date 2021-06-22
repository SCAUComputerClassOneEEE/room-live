package com.example.register.serviceInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 *
 * map appName --> ServiceProvider
 *
 */
public class ServiceApplicationsTable {

    public static final String SERVER_PEER_NODE = "server-peer-node-service";
    public static final String CLIENT_APPLICATION = "client-application";

    private static final ConcurrentHashMap<String, ConcurrentSkipListSet<ServiceProvider>> doubleMarkMap = new ConcurrentHashMap<>();

    public ServiceApplicationsTable(ServiceProvidersBootConfig config, /*初始化时候的服务列表*/
                                    String selfAppName/*自己的appName*/) {
        ServiceProvider selfNode = config.getSelfNode();
        ConcurrentSkipListSet<ServiceProvider> selfSet = new ConcurrentSkipListSet<>(config.getTableSetRankComparator());
        selfSet.add(selfNode);
        doubleMarkMap.put(selfAppName, selfSet);

        ConcurrentSkipListSet<ServiceProvider> othersSet = new ConcurrentSkipListSet<>(config.getTableSetRankComparator());
        List<ServiceProvider> othersPeerServerNodes = config.getOthersPeerServerNodes();
        if (othersPeerServerNodes != null)
            othersSet.addAll(othersPeerServerNodes);
        doubleMarkMap.put(SERVER_PEER_NODE, othersSet);
    }

    public void remove(ServiceProvider.TypeServiceProvider type, String appName) {

    }

    public void put(String appName, String host, int port, ServiceProvider.TypeServiceProvider type) {

    }

    /**
     *
     *
     * @return servers for notifying
     */
    public Iterator<ServiceProvider> getServers() {
        return get(SERVER_PEER_NODE);
    }

    /**
     *
     *
     * @param appName app name
     * @return app service
     */
    public Iterator<ServiceProvider> get(String appName) {
        ConcurrentSkipListSet<ServiceProvider> appSet = doubleMarkMap.get(appName);
        if (appSet == null)
            return null;
        if (appSet.isEmpty()) return null;
        return appSet.iterator();
    }

    public static ServiceProvider getOneByMask(Iterator<ServiceProvider> iterator, int mask) {
        ServiceProvider s = null;
        while (iterator.hasNext()) {
            if (iterator.next().mask() == mask) {
                s = iterator.next();
                break;
            }
            iterator.remove();
        }
        return s;
    }
}
