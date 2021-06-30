package com.example.register.serviceInfo;

import com.example.register.process.RegistryServer;

import java.util.Comparator;
import java.util.List;

public class ServiceProvidersBootConfig {
    // myself
    private ServiceProvider selfNode;
    // others server
    private List<ServiceProvider> othersPeerServerNodes;

    private RegistryServer.ClusterType serverClusterType;

    /*
    * client
    * */
    private int taskQueueMaxSize;
    private int nextSize;
    private int clientSubExecutors;
    private int connectTimeOut; // mills
    private int readTimeOut; // mills

    /*
    * server
    * */
    private int writeTimeOut; // mills
    private int serverPort;
    private int maxContentLength;
    private int backLog;

    private Comparator<ServiceProvider> tableSetRankComparator;

    public int getBackLog() { return backLog; }

    public void setBackLog(int backLog) { this.backLog = backLog; }

    public int getTaskQueueMaxSize() {
        return taskQueueMaxSize;
    }

    public int getMaxContentLength() { return maxContentLength; }

    public void setMaxContentLength(int maxContentLength) { this.maxContentLength = maxContentLength; }

    public void setTaskQueueMaxSize(int taskQueueMaxSize) {
        this.taskQueueMaxSize = taskQueueMaxSize;
    }

    public RegistryServer.ClusterType getServerClusterType() { return serverClusterType; }

    public void setServerClusterType(RegistryServer.ClusterType serverClusterType) {
        this.serverClusterType = serverClusterType;
        if (serverClusterType.equals(RegistryServer.ClusterType.SINGLE))
            othersPeerServerNodes = null;
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

    public List<ServiceProvider> getOthersPeerServerNodes() {
        return othersPeerServerNodes;
    }

    public void setOthersPeerServerNodes(List<ServiceProvider> othersPeerServerNodes) {
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
