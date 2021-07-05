package com.example.register.process;

import com.example.register.serviceInfo.ServiceProvidersBootConfig;

/**
 *
 * 对集群服务的所有机器进程的抽象接口
 * 实现有两方面：client 和 server，他们有共同的行为：初始化，启动，关闭
 */
public interface Application {
    void start();
    void stop() throws Exception;
    boolean isRunning();
}
