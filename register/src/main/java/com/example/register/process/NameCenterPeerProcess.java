package com.example.register.process;


import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.client.ProcessedRunnable;
import com.example.register.trans.server.ApplicationServer;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

/**
 *
 * 单例
 *
 * 开启两个线程，serverThread 和 clientThread
 */
public class NameCenterPeerProcess extends DiscoveryNodeProcess implements RegistryServer {

    private static final NameCenterPeerProcess INSTANCE = new NameCenterPeerProcess();

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
     * 只能对table的初始化和读，不能写！！！
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
        super.init(config);
        if (config.getServerClusterType().equals(ClusterType.P2P)) {
            Iterator<ServiceProvider> servers = table.getServers();
            while (servers.hasNext()) {
                if (syncAll(servers.next(), true)) break;
                servers.remove();
            }
        }
        /*
        *
        * start server
        * */
        server = new ApplicationServer();
        server.init(this, config);
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

    @Override
    public void replicate() {
        String url = "replicate";
        String taskId = UUID.randomUUID().toString();
    }

    @Override
    public boolean syncAll(ServiceProvider peerNode, boolean sync) throws Exception {
        HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                .byBootstrap((Bootstrap) client.getBootstrap())
                .access(HttpMethod.GET, "/syncAll")
                .connectWith(peerNode)
                .addHeader("taskId", UUID.randomUUID().toString())
                .done(new ProcessedRunnable<HttpTaskCarrierExecutor>() {
                    @Override
                    public void processed(HttpTaskCarrierExecutor executor) {
                        if (executor.success()) {
                            String peerTables = executor.getResultString();
                            try {
                                Map<String, Set<ServiceProvider>> serviceProviders = JSONUtil.readMapSetValue(peerTables,
                                        new TypeReference<Map<String, Set<ServiceProvider>>>() {});

                                Map<String, Set<ServiceProvider>> selfHigherList = table.compareAndReturnUpdate(serviceProviders);
                                if (!selfHigherList.isEmpty()) {
                                    // replicate
                                }
                            } catch (Exception e) {
                                executor.setParseSuccess(false);
                            }
                        }
                    }
                }).withBody("").create();
        /*block to sub taskQueue*/
        client.subTask(executor);
        if (sync)
            executor.sync();
        else return false;
        return executor.success() & executor.isParseSuccess();
    }

    @Override
    public boolean isActive(ServiceProvider provider, boolean sync) {
        String url = "isActive";
        String taskId = UUID.randomUUID().toString();
        /*
         * if one peer is inactive, remove from table
         * */
        return false;
    }

    private boolean lockForStatus() {
        return true;
    }
}
