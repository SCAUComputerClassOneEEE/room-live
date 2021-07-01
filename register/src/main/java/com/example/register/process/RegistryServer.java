package com.example.register.process;

import com.example.register.serviceInfo.ServiceApplicationsTable;
import com.example.register.serviceInfo.ServiceProvider;

/**
 *
 * 集群中的 server，继承 Application
 */
public interface RegistryServer extends Application {

    enum ClusterType {
        SINGLE, P2P
    }

    /**
     *
     * 从 peer 拉取全部数据
     * GET /syncAll
     */
    void syncAll(ServiceProvider peerNode, boolean sync) throws Exception;

    /**
     * GET /isActive
     */
    boolean isActive(ServiceProvider provider, boolean sync);

}
