package com.example.register.process;


import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.client.ProcessedRunnable;
import com.example.register.trans.server.ApplicationServer;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

/**
 *
 * 单例
 *
 * 开启两个线程，serverThread 和 clientThread
 */
public class NameCenterPeerProcess extends DiscoveryNodeProcess implements RegistryServer {

    private ApplicationServer server;

    // 基础通信：client 和 server

    // 注册表

    public NameCenterPeerProcess(ServiceProvidersBootConfig config) throws Exception {
        super(config);
        init(config);
    }

    /**
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
    protected void init(ServiceProvidersBootConfig config) throws Exception {
        if (config.getServerClusterType().equals(ClusterType.P2P)) {
            syncAll(myPeerNode, true);
        }
        /*
        *
        * start server
        * */
        server = new ApplicationServer(this, config);
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
    public void syncAll(ServiceProvider peerNode, boolean sync) throws Exception {
        HttpTaskCarrierExecutor executor = HttpTaskCarrierExecutor.Builder.builder()
                .byClient(client)
                .access(HttpMethod.GET, "/syncAll")
                .connectWith(peerNode)
                .done(new ProcessedRunnable() {
                    @Override
                    public void successAndThen(HttpTaskCarrierExecutor process, String resultString) throws Exception {
                        Map<String, Set<ServiceProvider>> serviceProviders = JSONUtil.readMapSetValue(resultString,
                                new TypeReference<Map<String, Set<ServiceProvider>>>() {});
                        Map<String, Set<ServiceProvider>> selfHigherList = table.compareAndReturnUpdate(serviceProviders);
                        if (!selfHigherList.isEmpty()) {
                            // replicate
                        }
                    }

                    @Override
                    public void failAndThen(HttpTaskCarrierExecutor process, String resultString) {

                    }
                }).create();
        /*block to sub taskQueue*/
        executor.sub();
        if (sync) {
            executor.sync();
        }
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
}
