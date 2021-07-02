package com.example.register.process;


import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.client.HttpTaskCarrierExecutor;
import com.example.register.trans.client.ProcessedRunnable;
import com.example.register.trans.client.ResultType;
import com.example.register.trans.server.ApplicationServer;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.*;

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
            syncAll(true);
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
        super.start();
        server.start();
    }

    /**
     *
     * 停止对外服务
     */
    @Override
    public void stop() {
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
        discover(myPeer, sb.toString(), sync);
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
}
