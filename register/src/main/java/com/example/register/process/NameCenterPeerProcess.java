package com.example.register.process;


import com.example.register.serviceInfo.InstanceInfo;
import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.ApplicationClient;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.server.ApplicationServer;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
     * setup 1 对某一个 peer 发出全同步请求 syncAll() 阻塞
     *      -
     * setup 2 把自己的 InstanceInfo 同步发送出去 replicate()
     *      -
     * setup 3 数据的合并处理
     *      -
     * setup 4 准备对外服务
     */
    @Override
    public void init(ServiceProvidersBootConfig config) throws Exception {
        // initialize the table with config and myself

        table = new ServiceApplicationsTable(
                config,
                ServiceApplicationsTable.SERVER_PEER_NODE);

        client = new ApplicationClient(config.getTaskQueueMaxSize(), config.getNextSize());
        server = new ApplicationServer();
        // initialize the client and server's thread worker for working.
        client.init(this);
        server.init(this);

        client.start();
        server.start();

        if (config.getServerClusterType().equals(ClusterType.P2P)) {
            Iterator<ServiceProvider> servers = table.getServers();

            while (servers.hasNext()) {
                if (syncAll(servers.next())) break;
                servers.remove();
            }
        }
    }

    /**
     *
     * 开启对外服务
     *
     */
    @Override
    public void start() {
        // only once

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

    }

    @Override
    public boolean syncAll(ServiceProvider peerNode) throws Exception {
        String url = "/syncAll";
        HttpTaskCarrierExecutor httpTaskCarrierExecutor = HttpTaskCarrierExecutor.Builder.builder()
                .byBootstrap((Bootstrap) client.getBootstrap())
                .access(HttpMethod.GET, url)
                .connectWith(peerNode)
                .withBody("")
                .create();
        while (true) {
            if (client.subTask(httpTaskCarrierExecutor)) break;
        }
        /*List<ServiceProvider>*/
        String peerTables = httpTaskCarrierExecutor.syncGetAndTimeOutRemove(0, 50);

        try {
            List<ServiceProvider> serviceProviders = JSONUtil.readListValue(peerTables, new TypeReference<List<ServiceProvider>>() {});
        } catch (IOException ioException) {

            return false;
        }
        return true;
    }

    /**
     *
     * 检测 provider 是否活跃
     *
     * 如果 provider 是自己，直接返回 true；
     * 否则 GET http://provider::/status ，返回远端状态
     * @param provider
     * @return
     */
    @Override
    public boolean isActive(ServiceProvider provider) {
        return false;
    }

    private void lockForStatus() {

    }
}
