package com.example.register.serviceInfo;

import java.util.Comparator;
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

    private ConcurrentHashMap<String, ConcurrentSkipListSet<ServiceProvider>> skipListMap = new ConcurrentHashMap<>();
    private static final Comparator<ServiceProvider> comparator = (o1, o2) -> 0;

    public ServiceApplicationsTable() {

    }

    public void remove(ServiceProvider.TypeServiceProvider type, String appName) {

    }

    public void put(String appName, String host, int port, ServiceProvider.TypeServiceProvider type) {
        ConcurrentSkipListSet<ServiceProvider> serviceProviders = skipListMap.get(appName);
        if (serviceProviders == null) {
            // init key
            ServiceProvider serviceProvider = new ServiceProvider(host, port, type);

            ConcurrentSkipListSet<ServiceProvider> objects = new ConcurrentSkipListSet<>(comparator);
            objects.add(serviceProvider);
            skipListMap.put(appName,objects);
        } else {

        }
    }
}
