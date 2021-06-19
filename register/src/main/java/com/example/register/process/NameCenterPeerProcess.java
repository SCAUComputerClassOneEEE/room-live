package com.example.register.process;


import com.example.register.serviceInfo.InstanceInfo;
import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.ApplicationClient;
import com.example.register.trans.server.ApplicationServer;

import javax.naming.directory.AttributeModificationException;
import java.util.Date;
import java.util.List;

/**
 *
 * 单例
 *
 * 开启两个线程，serverThread 和 clientThread
 */
public class NameCenterPeerProcess implements RegistryServer, RegistryClient {

    private static final NameCenterPeerProcess INSTANCE = new NameCenterPeerProcess();
    private static final Long myself;
    private static ServiceApplicationsTable table;

    static {
        myself = new Date().getTime();
    }

    private final ApplicationClient client;
    private final ApplicationServer server;

    // 基础通信：client 和 server

    // 注册表

    private NameCenterPeerProcess() {
        client = new ApplicationClient();
        server = new ApplicationServer();
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
                config, myself, ServiceApplicationsTable.SERVER_PEER_NODE);
        List<ServiceProvider> othersPeerServerNodes = config.getOthersPeerServerNodes();
        for (ServiceProvider othersPeerServerNode : othersPeerServerNodes) {
            if (syncAll(othersPeerServerNode)) break;
        }

        // initialize the client and server's thread worker for working.
        client.init(this);
        server.init(this);
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
    public boolean syncAll(ServiceProvider peerNode) {
        InstanceInfo info = peerNode.getInfo();
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
