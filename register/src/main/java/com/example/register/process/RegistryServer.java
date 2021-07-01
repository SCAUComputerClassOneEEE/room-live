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
     * 同步数据，对自己的数据表更新，同时向 peers 推更新：
     * - 如果是来自client的更新就发出对peers的同步；
     * - 否则不再向远同步。
     * POST /replicate
     */
    void replicate();

    /**
     *
     * 从 peer 拉取全部数据
     * GET /syncAll
     */
    boolean syncAll(ServiceProvider peerNode, boolean sync) throws Exception;

    /**
     * GET /isActive
     */
    boolean isActive(ServiceProvider provider, boolean sync);

}
