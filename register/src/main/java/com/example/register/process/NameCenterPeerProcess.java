package com.example.register.process;


import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.trans.server.ApplicationServer;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * 集群中的 server，继承 Application
 *
 * GET：syncAll
 *
 * --> 200 请求成功，对方返回的数据是可解析的
 *
 * --> 303 不存在该类数据（对于syncAll对方尚未全同步结束），对方希望请求别的peer
 * --> 500 服务器异常，重新register到别的peer，再进行syncAll
 *
 *
 * HEAD: isActive
 *
 * --> 200 请求成功，对方返回的请求头包含active内容
 *
 * --> 404 对方不存在需要检测的数据
 * --> 500 服务器异常，重新register到别的peer，再进行isActive
 */
public class NameCenterPeerProcess extends DiscoveryNodeProcess implements RegistryServer {

    private ApplicationServer server;
    private int heartBeatIntervals;

    // 基础通信：client 和 server

    // 注册表

    public NameCenterPeerProcess(ApplicationBootConfig config) throws Exception {
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
    protected void init(ApplicationBootConfig config) throws Exception {
        if (config.getServerClusterType().equals(ClusterType.P2P)) {
            syncAll(true);
        }
        /*
        *
        * start server
        * */
        server = new ApplicationServer(this, config);
        heartBeatIntervals = config.getHeartBeatIntervals();
    }

    /**
     *
     * 开启对外服务
     *
     */
    @Override
    public void start() {
        // only once
        super.start();
        server.start();
    }

    /**
     *
     * 停止对外服务
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        server.stopThread();
    }

    @Override
    public void syncAll(boolean sync) throws Exception {
        Set<String> apps = table.getAllAsMapSet().keySet();
        StringBuilder sb = new StringBuilder();
        for (String app : apps) {
            sb.append(app).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        discover(null, sb.toString(), sync);
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
    public Map<String, Set<ServiceProvider>> scan() {
        Map<String, Set<ServiceProvider>> allAsMapSet = table.getAllAsMapSet();
        Map<String, Set<ServiceProvider>> noHeartBeat = new HashMap<>();
        allAsMapSet.forEach((appName, set)->{
            Set<ServiceProvider> collect = set.stream()
                    .sorted().filter(serviceProvider -> serviceProvider.heartBeatGap() > heartBeatIntervals)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            noHeartBeat.put(appName, collect);
        });
        return noHeartBeat;
    }

    @Override
    public boolean isRunning() {
        return client.isAlive() && server.isAlive();
    }
}
