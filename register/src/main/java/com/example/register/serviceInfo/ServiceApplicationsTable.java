package com.example.register.serviceInfo;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * map appName --> ServiceProvider
 *
 */
public class ServiceApplicationsTable {

    public static final String SERVER_PEER_NODE = "server-peer-node-service";

    private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, ServiceProvider>> doubleMarkMap = new ConcurrentHashMap<>();

    public ServiceApplicationsTable(ServiceProvidersBootConfig config, /*初始化时候的服务列表*/
                                    Long selfLong, /*自己的long*/
                                    String selfAppName/*自己的appName*/) {
        ServiceProvider selfNode = config.getSelfNode();
        ConcurrentHashMap<Long, ServiceProvider> selfServiceMap = new ConcurrentHashMap<>();
        selfServiceMap.put(selfLong, selfNode);
        doubleMarkMap.put(selfAppName, selfServiceMap);

        ConcurrentHashMap<Long, ServiceProvider> serverServiceMap = new ConcurrentHashMap<>();
        config.getOthersPeerServerNodes().forEach((op)-> serverServiceMap.put(new Date().getTime(), op));
        doubleMarkMap.put(SERVER_PEER_NODE, serverServiceMap);
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
        ConcurrentHashMap<Long, ServiceProvider> servers = doubleMarkMap.get(SERVER_PEER_NODE);
        Collection<ServiceProvider> values = servers.values();
        return values.iterator();
    }

    /**
     *
     *
     * @param appName app name
     * @param selfLong self app long
     * @return app service
     */
    public ServiceProvider get(String appName, long selfLong) {
        final ConcurrentHashMap<Long, ServiceProvider> appMap = doubleMarkMap.get(appName);
        if (appMap == null)
            return null;
        return appMap.get(selfLong);
    }
}
