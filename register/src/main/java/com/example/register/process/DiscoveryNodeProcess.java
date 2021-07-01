package com.example.register.process;


import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.ApplicationClient;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.client.ProcessedRunnable;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Iterator;
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
    protected ServiceProvider myPeerNode; /*向他发送心跳 hb/30s*/
    protected ServiceProvider mySelf;

    public DiscoveryNodeProcess(ServiceProvidersBootConfig config) throws Exception {
        init(config);
    }

    protected void init(ServiceProvidersBootConfig config) throws Exception {
        mySelf = config.getSelfNode();
        // initialize the table with config and myself
        table = new ServiceApplicationsTable(
                config,
                ServiceApplicationsTable.SERVER_PEER_NODE);
        // initialize the client and server's thread worker for working.
        client = new ApplicationClient(this, config);
        client.start();
        Iterator<ServiceProvider> servers = table.getServers();
        /*
        * register myself
        * */
        while (servers.hasNext()) {
            ServiceProvider next = servers.next();
            replicate(next, mySelf, true);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void replicate(ServiceProvider peerNode, ServiceProvider which, boolean sync) throws Exception {
        /*
        * myPeerNode = peerNode;
        * */
        HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                .byClient(client)
                .access(HttpMethod.POST, "/replicate")
                .connectWith(peerNode)
                .withBody(JSONUtil.writeValue(which))
                .done(new ProcessedRunnable() {
                    @Override
                    public void successAndThen(HttpTaskCarrierExecutor process, String resultString) throws Exception {

                    }

                    @Override
                    public void failAndThen(HttpTaskCarrierExecutor process, String resultString) {

                    }
                }).create();
        executor.sub();
        if (sync) {
            executor.sync();
        }

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
