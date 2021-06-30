package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.trans.client.HttpTaskCarrierExecutor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * 集群服务里的 client，继承 Application 接口
 * 功能：
 * 1. 注册，
 * 2. 续约，
 * 3. 暂停，主动停止服务，暂停对外提供服务
 * 4. 下线，被动停止服务，一般是进程挂掉续约断了
 */
public interface RegistryClient extends Application {

    /**
     *
     * 向 server 注册
     */
    void register(ServiceProvider peerNode, ServiceProvider which, boolean sync);

    void register(ServiceProvider peerNode, List<ServiceProvider> whichList, boolean sync);
    /**
     *
     * 向 server 续约
     */
    void renew();

    /**
     *
     * 向 server 拉取某个 provider
     */
    void discover();

    /**
     *
     * 暂停服务
     */
    void pause();

    /**
     *
     * 下线，停止服务
     */
    void offline();

}
