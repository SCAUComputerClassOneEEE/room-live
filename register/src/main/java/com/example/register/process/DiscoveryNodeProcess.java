package com.example.register.process;


import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * 开启一个线程，clientThread
 */
public class DiscoveryNodeProcess implements RegistryClient{
    @Override
    public void init(ServiceProvidersBootConfig config) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void register(ServiceProvider peerNode, ServiceProvider which, boolean sync) {

    }

    @Override
    public void register(ServiceProvider peerNode, Map<String, Set<ServiceProvider>> whichList, boolean sync) {

    }

    @Override
    public void renew(ServiceProvider provider, boolean sync) {

    }

    @Override
    public void discover() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void offline() {

    }
}
