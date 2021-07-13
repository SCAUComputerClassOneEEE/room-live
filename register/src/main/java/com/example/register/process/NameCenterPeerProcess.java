package com.example.register.process;


import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.trans.server.ApplicationServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(NameCenterPeerProcess.class);
    private final RegistryServer.ClusterType clusterType;

    private final ApplicationServer server;
    private final int heartBeatIntervals;

    public NameCenterPeerProcess(ApplicationBootConfig config) {
        super(config);
        clusterType = config.getServerClusterType();
        server = new ApplicationServer(this, config);
        heartBeatIntervals = config.getHeartBeatIntervals();
        if (config.getServerClusterType().equals(ClusterType.P2P)) {
            try {
                syncAll(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*开启对外服务*/
    @Override
    public void start() {
        /* start server */
        stop = false;
        server.start();
        if (clusterType.equals(ClusterType.P2P))
            register(null, false, true, false);
    }

    @Override
    public void stop() throws Exception {
        if (stop) return;
        stop = true;
        offline(mySelf,true, true, false);
        client.stopThread();
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
        discover(null, sb.toString(), sync, null);
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
