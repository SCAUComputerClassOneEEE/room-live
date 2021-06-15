package com.example.register.process;

/**
 *
 * 集群中的 server，继承 Application
 */
public interface RegistryServer extends Application{

    /**
     *
     * 数据推到 peer
     */
    void replicate();

    /**
     *
     * 从 peer 拉取全部数据
     */
    void syncAll();

}
