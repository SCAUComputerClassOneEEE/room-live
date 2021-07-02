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
 *
 * 收到的状态码：
 * GET：discover
 * --> 200 请求成功，对方返回的数据是可解析的
 * --> 303 不存在该类数据（对于discover该资源还没有注册，或者已经下线），对方希望请求别的peer
 * --> 406 对方不支持该资源类型的请求，（server只支持json）
 * --> 500 服务器异常，重新register到别的peer，再进行discover
 * POST: register
 * --> 201 请求成功，对方注册了该资源并且将replicate到其他peers
 * --> 202 已经注册成功，不做处理
 * PUT: renew
 * --> 200 请求成功，对方接受了该资源的修改（心跳维持状态）并且将replicate到其他peers
 * --> 404 不存在该实例，对方希望发送register
 * --> 500 服务器异常，重新register到别的peer，再进行renew
 * DELETE: offline
 * --> 200 下线成功
 * --> 303 不存在该类数据（对于offline该资源还没有注册，或者已经下线）
 * --> 409 下线失败，因为对方认为你下线了别的node
 * --> 500 服务器异常，对别的peer进行offline
 */
public interface RegistryClient extends Application {



    void register();

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
     * 下线，停止服务
     * DELETE
     */
    void offline();

}
