package com.example.register.serviceInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * map appName --> ServiceProvider
 *
 */
public class ServiceApplicationsTable {

    public static final String SERVER_PEER_NODE = "server-peer-node-service";
    public static final String DEFAULT_CLIENT_APPLICATION = "default_client-application";

    private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProvider>> doubleMarkMap = new ConcurrentHashMap<>();

    public ServiceApplicationsTable(ServiceProvidersBootConfig config, /*初始化时候的服务列表*/
                                    String selfAppName/*自己的appName*/) {
        ServiceProvider selfNode = config.getSelfNode();
        ConcurrentHashMap<String,ServiceProvider> selfSet = new ConcurrentHashMap<>();
        selfSet.put(selfNode.getMask(), selfNode);
        doubleMarkMap.put(selfAppName, selfSet);

        ConcurrentHashMap<String,ServiceProvider> othersSet = new ConcurrentHashMap<>();
        Map<String, ServiceProvider> othersPeerServerNodes = config.getOthersPeerServerNodes();
        if (othersPeerServerNodes != null)
            othersSet.putAll(othersPeerServerNodes);
        doubleMarkMap.put(SERVER_PEER_NODE, othersSet);
    }

    public void remove(String appName) {
        doubleMarkMap.remove(appName);
    }

    public void remove(ServiceProvider serviceProvider) {
        Map<String, ServiceProvider> serviceProviders = doubleMarkMap.get(serviceProvider.getAppName());
    }

    public void put(String appName, String host, int port) {

    }

    public void put(String appName, ServiceProvider value) {

    }

    /**
     *
     *
     * @return servers for notifying
     */
    public Iterator<ServiceProvider> getServers() {
        return getAsIterator(SERVER_PEER_NODE);
    }

    /**
     *
     *
     * @param appName app name
     * @return app service
     */
    public Iterator<ServiceProvider> getAsIterator(String appName) {
        return getAsList(appName).iterator();
    }

    public List<ServiceProvider> getAsList(String appName) {
        final Map<String, ServiceProvider> appSet = doubleMarkMap.get(appName);
        if (appSet == null) return null;
        List<ServiceProvider> list = new LinkedList<>();
        appSet.forEach((s, serviceProvider) -> list.add(serviceProvider));
        return list;
    }

    public Enumeration<String> getAllAppNameEnum() {
        return doubleMarkMap.keys();
    }

    public List<ServiceProvider> getAsClonedList(String appName) throws CloneNotSupportedException {
        final List<ServiceProvider> asList = getAsList(appName);
        List<ServiceProvider> rls = new LinkedList<>();

        for (ServiceProvider r : asList) {
            rls.add((ServiceProvider)r.clone());
        }
        return rls;
    }

    public ServiceProvider get(String appName, String mask) {
        ServiceProvider rSP;
        if (appName == null || appName.equals("")) {
            if (mask == null || mask.equals(""))
                return null;
            Collection<ConcurrentHashMap<String, ServiceProvider>> values = doubleMarkMap.values();

            List<Map.Entry<String, ConcurrentHashMap<String, ServiceProvider>>> all = new LinkedList<>(doubleMarkMap.entrySet());
            for (Map.Entry<String, ConcurrentHashMap<String, ServiceProvider>> e : all) {
                ServiceProvider serviceProvider = e.getValue().get(mask);
                if (serviceProvider != null)
                    return serviceProvider;
            }
        }
        if (mask == null || mask.equals(""))
            return null;
        Map<String, ServiceProvider> appSet = doubleMarkMap.get(appName);
        rSP = appSet.get(mask);
        return rSP;
    }

    /**
     * 与自身的比较，并且返回返回版本更加高的
     * @param second 全同步的来源
     * @return 返回版本更加高的
     */
    public List<ServiceProvider> compareAndUpdate(List<ServiceProvider> second) {
        try {
            final ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProvider>> newTable = new ConcurrentHashMap<>();
            Enumeration<String> allAppNameEnum = getAllAppNameEnum();
            while (allAppNameEnum.hasMoreElements()) {
                String s = allAppNameEnum.nextElement();
                List<ServiceProvider> asClonedList = getAsClonedList(s);

            }
            synchronized (this) {
                doubleMarkMap = newTable;
            }
        } catch (Exception e) {

        }
        return null;
    }
}
