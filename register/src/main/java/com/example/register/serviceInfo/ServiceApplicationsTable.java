package com.example.register.serviceInfo;

import com.example.register.utils.CollectionUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * map appName --> ServiceProvider
 */
public class ServiceApplicationsTable {

    public static final String SERVER_PEER_NODE = "server-peer-node-service";
    public static final String DEFAULT_CLIENT_APPLICATION = "default_client-application";

    /*
     * LB
     */
    public static class LeastConnectionComparator implements Comparator<ServiceProvider> {
        @Override
        public int compare(ServiceProvider o1, ServiceProvider o2) {
            return o1.getConnectingInt().get() > o2.getConnectingInt().get() ? 0 : 1;
        }
    }

    /*
    * 降序
    * */
    public static class FastestResponseComparator implements Comparator<ServiceProvider> {
        @Override
        public int compare(ServiceProvider o1, ServiceProvider o2) {
            return o1.getAvgAccess().get() > o2.getAvgAccess().get() ? 0 : 1;
        }
    }

    /*
    *
    * 双索引的哈希表
    * x-----x-----x-----x-----x-----x-----x-----x
    * | app | app | ... | ... | ... | app | ... |
    * x-----x-----x-----x-----x-----x-----x-----x
    *    |     |                       |
    *    |    \|/                     \|/
    *    |     x-----x-----x-----x     x-----x-----x-----x
    *    |     | msk | ... | ... |     | ... | msk | ... |
    *    |     x-----x-----x-----x     x-----x-----x-----x
    *   \|/
    *    x-----x-----x-----x
    *    | msk | msk | ... |
    *    x-----x-----x-----x
    * 一级索引存放appName，一类服务的name
    * 二级索引存放mask，serviceProvider的唯一id
    *
    * 持久化依靠数据库。
    * */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceProvider>> doubleMarkMap;

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

    public void removeApps(String appName) {
        doubleMarkMap.remove(appName);
    }

    public void removeApp(ServiceProvider val) {
        Map<String, ServiceProvider> thisApps = doubleMarkMap.get(val.getAppName());
        if (thisApps == null)
            return;
        thisApps.remove(val.getMask());
    }

    /**
     *
     * @param appName can be null, no char or
     * @param newVal
     */
    public void putAppIfAbsent(String appName, ServiceProvider newVal) {
        ConcurrentHashMap<String, ServiceProvider> thisApps;
        if (appName == null || appName.equals("")) {
            thisApps = defaultApps();
        } else {
            thisApps = doubleMarkMap.get(appName);
            if (thisApps == null) {
                thisApps = putNewAppsIfAbsent(appName);
            }
        }
        thisApps.putIfAbsent(newVal.getMask(), newVal);
    }

    private ConcurrentHashMap<String, ServiceProvider> putNewAppsIfAbsent(String newAppName) {
        ConcurrentHashMap<String, ServiceProvider> newApps = doubleMarkMap.get(newAppName);
        if (newApps == null) {
            newApps = new ConcurrentHashMap<>();
            doubleMarkMap.put(newAppName, newApps);
        }

        return newApps;
    }

    private ConcurrentHashMap<String, ServiceProvider> defaultApps() {
        return putNewAppsIfAbsent(DEFAULT_CLIENT_APPLICATION);
    }

    /**
     * for json or compareAndUpdate
     * */
    public Map<String/*appName*/, Set<ServiceProvider>/*instances*/> getAllAsMapSet() {
        long ml = doubleMarkMap.mappingCount(); Enumeration<String> apps = doubleMarkMap.keys();
        Map<String, Set<ServiceProvider>> getMapList = new HashMap<>();
        while (apps.hasMoreElements() && ml -- > 0) {
            String s = apps.nextElement();
            Set<ServiceProvider> asList = getAppsAsSet(s);
            getMapList.put(s, asList);
        }
        return getMapList;
    }

    public boolean hasAnyApps(String appName) {
        return doubleMarkMap.get(appName).size() > 0;
    }

    public ServiceProvider getOptimalServer() {
        return getOptimal(SERVER_PEER_NODE);
    }

    /**
     *
     * 选择最优的serviceProvider
     */
    public ServiceProvider getOptimal(String appName) {
        Set<ServiceProvider> appsAsSet = getAppsAsSet(appName);
        if (appsAsSet.size() == 0)
            return null;
        Object[] objects = appsAsSet.stream().sorted(new FastestResponseComparator()).distinct().toArray();
        return ((ServiceProvider[]) objects)[0];
    }

    public Iterator<ServiceProvider> getServers() {
        return getAppsAsIterator(SERVER_PEER_NODE);
    }

    /**
     * @param appName app name
     * @return app service
     */
    public Iterator<ServiceProvider> getAppsAsIterator(String appName) {
        return getAppsAsSet(appName).iterator();
    }

    public Set<ServiceProvider> getAppsAsSet(String appName) {
        Set<ServiceProvider> set = new HashSet<>();
        final Map<String, ServiceProvider> appSet = doubleMarkMap.get(appName);
        if (appSet == null || appSet.size() <= 0)
            return set;
        appSet.forEach((s, serviceProvider) -> set.add(serviceProvider));
        return set;
    }

    public Set<ServiceProvider> getAppsAsClonedSet(String appName) throws CloneNotSupportedException {
        final Set<ServiceProvider> asList = getAppsAsSet(appName);
        Set<ServiceProvider> rls = new HashSet<>();

        for (ServiceProvider r : asList) {
            rls.add((ServiceProvider)r.clone());
        }
        return rls;
    }

    public ServiceProvider getApp(String appName, String mask) {
        ServiceProvider rSP;
        if (appName == null || appName.equals(""))
            return null;
        if (mask == null || mask.equals(""))
            return null;
        Map<String, ServiceProvider> appSet = doubleMarkMap.get(appName);
        rSP = appSet.get(mask);
        return rSP;
    }

    public void renewApp(ServiceProvider value) {
        ServiceProvider app = getApp(value.getAppName(), value.getMask());
        if (app == null)
            return;
        app.newVersion();
        app.cover(value);
    }


    /**
     * 与自身的比较并更新，并且返回返回版本更加高的，自身有而second没有的
     * @param second appName和service provider的集合的键值映射
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
