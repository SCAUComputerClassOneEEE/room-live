package com.example.register.process;


/**
 *
 * 单例
 *
 * 开启两个线程，serverThread 和 clientThread
 */
public class NameCenterPeerProcess implements RegistryServer, RegistryClient{

    private static final NameCenterPeerProcess INSTANCE = new NameCenterPeerProcess();

    // 基础通信：client 和 server

    // 注册表

    private NameCenterPeerProcess() { }

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
    public void init() {
        // only once
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
    public void export() {

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

    /**
     *
     * 同步数据，对自己的数据表更新，同时向peers更新
     * 如果是client的更新就发出对peers的同步；
     * 否则不在向远同步。
     */
    @Override
    public void replicate() {

    }

    @Override
    public void syncAll() {

    }
}
