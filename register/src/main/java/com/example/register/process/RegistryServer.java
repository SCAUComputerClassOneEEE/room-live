package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvider;


public interface RegistryServer extends Application, MethodNestable {

    enum ClusterType {
        SINGLE, P2P
    }

    void syncAll(boolean sync) throws Exception;

    boolean isActive(ServiceProvider provider, boolean sync);

}
