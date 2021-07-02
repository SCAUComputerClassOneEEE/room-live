package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvider;

public interface RegistryClient extends Application, MethodNestable {

    enum ReplicationAction {
        REGISTER,
        RENEW,
        OFFLINE
    }

    void register() throws Exception;

    void renew(boolean sync) throws Exception;

    void discover(ServiceProvider peer, String appName, boolean sync) throws Exception;

    void replicate(ServiceProvider peerNode, ServiceProvider which, ReplicationAction action,  boolean sync) throws Exception;

    void offline();

}
