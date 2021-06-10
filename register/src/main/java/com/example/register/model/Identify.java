package com.example.register.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class Identify {
    /**
     * 一类服务提供者的标识，不可以更改！！！
     */
    private final String appName;
    /**
     * 版本号，每一个有关对appName对应的ra集合修改都会递增这个字段。
     * 从0开始，只增不减。
     * Client第一次拉取某个appName的注册信息时，携带Version = -1 。
     *
     * 前来拉取该appName注册服务的所有请求都必须携带一个版本号 ClientVersion ，它和服务器的 ServerVersion 进行比较：
     * 1. 如果 ClientVersion < ServerVersion，长轮询直接返回appName对应的ra集合；
     * 2. 如果 ClientVersion = ServerVersion，长轮询hold住（保留httpRequest），直到ClientVersion < ServerVersion或者httpRequest超时；
     * 3. 如果 ClientVersion > ServerVersion，直接返回500；（你怎么可以这么频繁。。。
     *
     * version的控制与长轮询由thread包实现。
     */
    private AtomicLong version = new AtomicLong(0);

    /**
     * 还不知道干嘛。。。
     */
    private List<Subscriber> ss = new LinkedList<>();


    public Identify(String appName) {
        this.appName = appName;
    }

    /**
     *
     * @return 该appName下的List<RA> 任何一个更新后，版本递增
     */
    public long incrementVersion() {
        return version.incrementAndGet();
    }
}
