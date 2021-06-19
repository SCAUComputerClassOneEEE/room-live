package com.example.register.serviceInfo;

import java.util.List;

public class ServiceProvidersBootConfig {
    // myself
    private ServiceProvider selfNode;
    // others server
    private List<ServiceProvider> othersPeerServerNodes;

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
        this.othersPeerServerNodes = othersPeerServerNodes;
    }
}
