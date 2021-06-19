package com.example.register.serviceInfo;

import com.example.register.process.RegistryServer;

import java.util.List;

public class ServiceProvidersBootConfig {
    // myself
    private ServiceProvider selfNode;
    // others server
    private List<ServiceProvider> othersPeerServerNodes;

    private RegistryServer.ClusterType serverClusterType;

    public RegistryServer.ClusterType getServerClusterType() { return serverClusterType; }

    public void setServerClusterType(RegistryServer.ClusterType serverClusterType) {
        this.serverClusterType = serverClusterType;
        if (serverClusterType.equals(RegistryServer.ClusterType.SINGLE))
            othersPeerServerNodes = null;
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
}
