package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.spring.config.RegisterProperties;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationBootConfig {
    // myself
    private ServiceProvider selfNode;
    // others server
    private Map<String, ServiceProvider> othersPeerServerNodes;

    private RegistryServer.ClusterType serverClusterType;

    /*thread*/
    private int workerNThread;
    private int bossNThread;
    /*
    * client
    * */
    private int taskQueueMaxSize;
    private int nextSize;
    private int clientSubExecutors;
    private int connectTimeOut; // mills
    private int readTimeOut; // mills
    private int maxTolerateTimeMills;
    private int heartBeatIntervals;

    /*
    * server
    * */
    private int writeTimeOut; // mills
    private int serverPort;
    private int maxContentLength;
    private int backLog;

    private Comparator<ServiceProvider> tableSetRankComparator;

    public ApplicationBootConfig() { }

    public ApplicationBootConfig(RegisterProperties properties) {
        RegisterProperties.Address self = properties.getSelf();
        selfNode = new ServiceProvider(self.getAppName(), self.getHost(), self.getPort());
        selfNode.setProtocol(self.getProtocol());
        othersPeerServerNodes = new HashMap<>();
        List<RegisterProperties.Address> servers = properties.getPeers();
        for (RegisterProperties.Address peerAd : servers) {
            String appName = peerAd.getAppName();
            String host = peerAd.getHost();
            int port = peerAd.getPort();
            String protocol = peerAd.getProtocol();

            ServiceProvider peer = new ServiceProvider(appName, host, port);
            peer.setProtocol(protocol);
            othersPeerServerNodes.put(appName, peer);
        }
        serverClusterType = othersPeerServerNodes.size() > 1 ?
                RegistryServer.ClusterType.P2P : RegistryServer.ClusterType.SINGLE;

        workerNThread = properties.getWorkerNThread();
        bossNThread = properties.getBossNThread();

        taskQueueMaxSize = properties.getTaskQueueMaxSize();
        nextSize = properties.getNextQueueSize();
        connectTimeOut = properties.getConnectTimeOut();
        readTimeOut = properties.getReadTimeOut();
        maxTolerateTimeMills = properties.getMaxTolerateTimeMills();
        heartBeatIntervals = properties.getHeartBeatIntervals();

        writeTimeOut = properties.getWriteTimeOut();
        serverPort = properties.getServerPort();
        maxContentLength = properties.getMaxContentLength();
        backLog = properties.getBackLog();
    }

    public int getWorkerNThread() {
        return workerNThread;
    }

    public void setWorkerNThread(int workerNThread) {
        this.workerNThread = workerNThread;
    }

    public int getBossNThread() {
        return bossNThread;
    }

    public void setBossNThread(int bossNThread) {
        this.bossNThread = bossNThread;
    }

    public int getBackLog() { return backLog; }

    public int getHeartBeatIntervals() { return heartBeatIntervals; }

    public void setHeartBeatIntervals(int heartBeatIntervals) { this.heartBeatIntervals = heartBeatIntervals; }

    public int getMaxTolerateTimeMills() { return maxTolerateTimeMills; }

    public void setMaxTolerateTimeMills(int maxTolerateTimeMills) { this.maxTolerateTimeMills = maxTolerateTimeMills; }

    public void setBackLog(int backLog) { this.backLog = backLog; }

    public int getTaskQueueMaxSize() {
        return taskQueueMaxSize;
    }

    public int getMaxContentLength() { return maxContentLength; }

    public void setMaxContentLength(int maxContentLength) { this.maxContentLength = maxContentLength; }

    public void setTaskQueueMaxSize(int taskQueueMaxSize) {
        this.taskQueueMaxSize = taskQueueMaxSize;
    }

    public RegistryServer.ClusterType getServerClusterType() {
        if (serverClusterType == null)
            return RegistryServer.ClusterType.SINGLE;
        return serverClusterType;
    }

    public void setServerClusterType(RegistryServer.ClusterType serverClusterType) {
        this.serverClusterType = serverClusterType;
    }

    public Comparator<ServiceProvider> getTableSetRankComparator() {
        return tableSetRankComparator;
    }

    public void setTableSetRankComparator(Comparator<ServiceProvider> tableSetRankComparator) {
        this.tableSetRankComparator = tableSetRankComparator;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public int getWriteTimeOut() {
        return writeTimeOut;
    }

    public void setWriteTimeOut(int writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
    }

    public ServiceProvider getSelfNode() {
        return selfNode;
    }

    public void setSelfNode(ServiceProvider selfNode) {
        this.selfNode = selfNode;
    }

    public Map<String, ServiceProvider> getOthersPeerServerNodes() {
        return othersPeerServerNodes;
    }

    public void setOthersPeerServerNodes(Map<String, ServiceProvider> othersPeerServerNodes) {
        serverClusterType = RegistryServer.ClusterType.P2P;
        this.othersPeerServerNodes = othersPeerServerNodes;
    }

    public int getNextSize() {
        return nextSize;
    }

    public void setNextSize(int nextSize) {
        this.nextSize = nextSize;
    }

    public int getClientSubExecutors() {
        return clientSubExecutors;
    }

    public void setClientSubExecutors(int clientSubExecutors) {
        this.clientSubExecutors = clientSubExecutors;
    }
}
