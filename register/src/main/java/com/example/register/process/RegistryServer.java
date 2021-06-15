package com.example.register.process;

/**
 *
 * 集群中的 server，继承 Application
 */
public interface RegistryServer extends Application{

    void replicate();

    void syncAll();

}
