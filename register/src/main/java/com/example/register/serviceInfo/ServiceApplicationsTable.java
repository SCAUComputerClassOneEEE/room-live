package com.example.register.serviceInfo;

import com.example.register.utils.CollectionUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 *
 *
 * map appName --> ServiceProvider
 *
 */
public class ServiceApplicationsTable {

    public static final String SERVER_PEER_NODE = "server-peer-node-service";
    public static final String DEFAULT_CLIENT_APPLICATION = "default_client-application";

    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProvider>> doubleMarkMap;

    public ServiceApplicationsTable() {

    }

    public ServiceApplicationsTable(ServiceProvidersBootConfig config, /*初始化时候的服务列表*/
                                    String selfAppName/*自己的appName*/) {
        doubleMarkMap = new ConcurrentHashMap<>();
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
     * for json or compareAndUpdate
     * */
    public Map<String/*appName*/, Set<ServiceProvider>/*instances*/> getAllAsMapSet() {
        long ml = doubleMarkMap.mappingCount(); Enumeration<String> apps = doubleMarkMap.keys();
        Map<String, Set<ServiceProvider>> getMapList = new HashMap<>();
        while (apps.hasMoreElements() && ml -- > 0) {
            String s = apps.nextElement();
            Set<ServiceProvider> asList = getAsSet(s);
            getMapList.put(s, asList);
        }
        return getMapList;
    }

    /**
     * @return servers for notifying
     */
    public Iterator<ServiceProvider> getServers() {
        return getAsIterator(SERVER_PEER_NODE);
    }

    /**
     * @param appName app name
     * @return app service
     */
    public Iterator<ServiceProvider> getAsIterator(String appName) {
        return getAsSet(appName).iterator();
    }

    public Set<ServiceProvider> getAsSet(String appName) {
        final Map<String, ServiceProvider> appSet = doubleMarkMap.get(appName);
        Set<ServiceProvider> list = new HashSet<>();
        if (appSet == null) return list;
        appSet.forEach((s, serviceProvider) -> list.add(serviceProvider));
        return list;
    }


    public Set<ServiceProvider> getAsClonedSet(String appName) throws CloneNotSupportedException {
        final Set<ServiceProvider> asList = getAsSet(appName);
        Set<ServiceProvider> rls = new HashSet<>();

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
     * 与自身的比较并更新，并且返回返回版本更加高的，自身有而second没有的
     * @param second 全同步的来源
     * @return 返回版本更加高的
     */
    public Map<String, Set<ServiceProvider>> compareAndReturnUpdate(Map<String, Set<ServiceProvider>> second) {
        Map<String, Set<ServiceProvider>> updateMap = new HashMap<>();

        Map<String, Set<ServiceProvider>> first = getAllAsMapSet();
        second.forEach((s, sSet) -> {
            Set<ServiceProvider> fSet = first.get(s);
            Set<ServiceProvider> uSet = compareAndReturnUpdate(fSet, sSet);
            if (!uSet.isEmpty()) {
                updateMap.put(s, uSet);
            }
        });
        return updateMap;
    }

    private Set<ServiceProvider> compareAndReturnUpdate(Set<ServiceProvider> first/*self*/, Set<ServiceProvider> second) {
        Collection<ServiceProvider> same = new HashSet<>();
        CollectionUtil.saveUniqueAndDuplicates(
                first/*first有second没有的，返回*/,
                second/*first没有second有的，自身更新*/,
                same/*都有的，比较版本，旧的返回，新的自身更新*/ /*same的来源是，长度最大的set*/
        );
        // return set
        Set<ServiceProvider> updateSet = new HashSet<>(first);
        // update self
        for (ServiceProvider newOne : second) {
            ConcurrentHashMap<String, ServiceProvider> subMap = doubleMarkMap.get(newOne.getAppName());
            subMap.put(newOne.getMask(), newOne);
        }
        Set<ServiceProvider> sameFromSecond;
        if (first.size() >= second.size()) {
            // same set belong to first set
            sameFromSecond = new HashSet<>();
            for (ServiceProvider sameSourceOne : second) {
                if (same.contains(sameSourceOne)) sameFromSecond.add(sameSourceOne);
            }
        } else {
            // same set belong to second set
            sameFromSecond = new HashSet<>(same);
        }
        // update(try to cover) with same set
        for (ServiceProvider secondS : sameFromSecond) {
            boolean cover = doubleMarkMap.get(secondS.getAppName()).get(secondS.getMask()).cover(secondS);
            if (!cover) {
                // old version
                updateSet.add(secondS);
            }
        }

        // same update
        return updateSet;
    }
}
