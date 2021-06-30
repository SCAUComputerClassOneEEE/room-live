package com.example.register.process;


import com.example.register.serviceInfo.InstanceInfo;
import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.ApplicationClient;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.server.ApplicationServer;
import com.example.register.utils.HttpTaskExecutorPool;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 *
 * 单例
 *
 * 开启两个线程，serverThread 和 clientThread
 */
public class NameCenterPeerProcess implements RegistryServer, RegistryClient {

    private static final NameCenterPeerProcess INSTANCE = new NameCenterPeerProcess();
    private static ServiceApplicationsTable table;

    private ApplicationClient client;
    private ApplicationServer server;

    // 基础通信：client 和 server

    // 注册表

    private NameCenterPeerProcess() {

    }

    public static NameCenterPeerProcess getInstance() {
        return INSTANCE;
    }
    /**
     *
     *
     * 初始化，启动数据的远端同步
     * setup 0 从参数 config 中初始化 peers 列表
     *      -
     * setup 1 对peers列表的每一个peer发出isActive探测
     *
     * setup 2 对某一个 peer 发出全同步请求 syncAll() 阻塞，并更新到table中
     *      -
     * setup 2 把自己的 InstanceInfo 同步发送给每一个peer replicate()
     *      -
     * setup 3 准备对外服务
     */
    @Override
    public void init(ServiceProvidersBootConfig config) throws Exception {
        /*
        * lockForStatus...
        * */
        if (!lockForStatus())
            return;
        // initialize the table with config and myself
        table = new ServiceApplicationsTable(
                config,
                ServiceApplicationsTable.SERVER_PEER_NODE);

        client = new ApplicationClient(config.getTaskQueueMaxSize(), config.getNextSize());
        server = new ApplicationServer();
        // initialize the client and server's thread worker for working.
        client.init(this, config);
        server.init(this, config);

        client.start();

        if (config.getServerClusterType().equals(ClusterType.P2P)) {
            Iterator<ServiceProvider> servers = table.getServers();
            /*
             * peers all is active?
             */
            while (servers.hasNext()) {
                /*
                * if one peer is inactive, remove from table
                * */
                if (!isActive(servers.next(), true)) {
                    table.remove(servers.next());
                }
                servers.remove();
            }
            Iterator<ServiceProvider> servers1 = table.getServers();
            while (servers1.hasNext()) {
                if (syncAll(table, servers1.next())) break;
                servers1.remove();
                /*
                * because of server's inactivation
                * table remove servers.next()
                * */
            }
        }

        /*
        *
        * start server
        * */
    }

    /**
     *
     * 开启对外服务
     *
     */
    @Override
    public void start() {
        // only once
        server.start();
    }

    /**
     *
     * 停止对外服务
     */
    @Override
    public void stop() {

    }


    /**
     *
     * 该服务peer注册集群中到其他的peer中
     */
    @Override
    public void register() {

    }

    @Override
    public void renew() {

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

    @Override
    public void replicate() {
        String url = "replicate";
        String taskId = UUID.randomUUID().toString();
    }

    @Override
    public boolean syncAll(ServiceApplicationsTable table, ServiceProvider peerNode) throws Exception {
        boolean reB = true;
        String url = "/syncAll";
        List<ServiceProvider> serviceProviders;
        String taskId = UUID.randomUUID().toString();
        HttpTaskCarrierExecutor httpTaskCarrierExecutor = HttpTaskCarrierExecutor.Builder.builder()
                .byBootstrap((Bootstrap) client.getBootstrap())
                .access(HttpMethod.GET, url)
                .connectWith(peerNode)
                .addHeader("taskId", taskId)
                .withBody("")
                .create();
        /*block to sub taskQueue*/
        client.subTask(httpTaskCarrierExecutor);
        HttpTaskExecutorPool.taskMap.put(taskId, httpTaskCarrierExecutor);

        /*sync for List<ServiceProvider>*/
        String peerTables = httpTaskCarrierExecutor.syncGetAndTimeOutRemove();
        if (StringUtil.isNullOrEmpty(peerTables)) {
            reB = false;
        }

        try {
            serviceProviders = JSONUtil.readListValue(peerTables, new TypeReference<List<ServiceProvider>>() {});

        } catch (IOException ioException) {
            reB = false;
        }
        HttpTaskExecutorPool.taskMap.remove(taskId);
        return reB;
    }

    @Override
    public boolean isActive(ServiceProvider provider, boolean sync) {
        String url = "isActive";
        String taskId = UUID.randomUUID().toString();
        return false;
    }

    private boolean lockForStatus() {
        return true;
    }
}
