package com.example.register;

import com.example.register.process.ApplicationBootConfig;
import com.example.register.process.DiscoveryNodeProcess;
import com.example.register.process.NameCenterPeerProcess;
import com.example.register.process.RegistryServer;
import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

public class ClientBootTest {
    static final ServiceProvider selfNode;
    static final Map<String, ServiceProvider> otherPeers = new HashMap<>();

    static {
        selfNode = new ServiceProvider(ServiceApplicationsTable.DEFAULT_CLIENT_APPLICATION, "localhost", 8080);
        ServiceProvider peer = new ServiceProvider(ServiceApplicationsTable.SERVER_PEER_NODE, "localhost", 8000);
        otherPeers.put(peer.getMask(), peer);
    }

    public static void main(String[] args) throws Exception {
        ApplicationBootConfig applicationBootConfig = new ApplicationBootConfig();

        applicationBootConfig.setBackLog(1024);
        applicationBootConfig.setConnectTimeOut(3* 1000);
        applicationBootConfig.setClientSubExecutors(1); //
        applicationBootConfig.setHeartBeatIntervals(/*10 * */1000);
        applicationBootConfig.setMaxContentLength(5 * 1024 * 1024);
        applicationBootConfig.setNextSize(2);
        applicationBootConfig.setMaxTolerateTimeMills(500);
        applicationBootConfig.setReadTimeOut(2 * 1000);
        applicationBootConfig.setWriteTimeOut(2 * 1000);
        applicationBootConfig.setTaskQueueMaxSize(100);

        applicationBootConfig.setServerPort(8080);
        applicationBootConfig.setTableSetRankComparator(new ServiceApplicationsTable.FastestResponseComparator());
        applicationBootConfig.setSelfNode(selfNode);
        applicationBootConfig.setOthersPeerServerNodes(otherPeers);
        applicationBootConfig.setServerClusterType(RegistryServer.ClusterType.SINGLE);

        DiscoveryNodeProcess discoveryNodeProcess = new DiscoveryNodeProcess(applicationBootConfig);
        discoveryNodeProcess.start();
    }
}
