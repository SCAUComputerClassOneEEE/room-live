package com.example.register.process;


import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.ApplicationClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * 开启一个线程，clientThread
 */
public class DiscoveryNodeProcess implements RegistryClient{
    protected static ServiceApplicationsTable table;
    protected ApplicationClient client;

    @Override
    public void init(ServiceProvidersBootConfig config) throws Exception {
        // initialize the table with config and myself
        table = new ServiceApplicationsTable(
                config,
                ServiceApplicationsTable.SERVER_PEER_NODE);

        client = new ApplicationClient(config.getTaskQueueMaxSize(), config.getNextSize());

        // initialize the client and server's thread worker for working.
        client.init(this, config);
        client.start();
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
