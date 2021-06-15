package com.example.register.process;


import com.example.register.trans.client.ApplicationClient;
import com.example.register.trans.server.ApplicationServer;

/**
 *
 * 单例
 *
 * 开启两个线程，serverThread 和 clientThread
 */
public class NameCenterPeerProcess implements RegistryServer, RegistryClient{

    private static final NameCenterPeerProcess INSTANCE = new NameCenterPeerProcess();

    /**
     *
     * 1 - inactive
     * 2 - init ing
     * 4 - start ing
     * 8 - running
     * 16- stopping
     * 32- terminated
     */
    private static volatile short status = 1;

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
     * setup 0 初始化 peers 列表
     *      -
     * setup 1 对某一个 peer 发出全同步请求 syncAll()
     *      -
     * setup 2 把自己的 InstanceInfo 同步发送出去 replicate()
     *      -
     * setup 3 数据的合并处理
     *      -
     * setup 4 准备对外服务
     */
    @Override
    public void init() throws Exception {
        // only once
        if (status == 1) {
            synchronized (this) {
                if (status == 1)
                    status <<= 1;
                else return;
            }
            client.init(this);
            server.init(this);

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
    public void syncAll() {

    }
}
