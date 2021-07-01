package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * 同步数据，对自己的数据表更新，同时向 peer 推更新：
     * - 如果是来自client的更新就发出对peers的同步；
     * - 否则不再向远同步。
     * POST /replicate
     */
    void replicate(ServiceProvider peerNode, ServiceProvider which, boolean sync) throws Exception;

    /**
     * 心跳
     * 向 server 续约更新，版本迭代
     * PUT /renew
     */
    void renew(ServiceProvider provider/*just myself*/, boolean sync);

    /**
     *
     * 向 server 拉取某个 app
     * GET /discover
     */
    void discover();

    /**
     *
     * 暂停服务
     *
     */
    void pause();

    /**
     *
     * 下线，停止服务
     */
    void offline();

}
