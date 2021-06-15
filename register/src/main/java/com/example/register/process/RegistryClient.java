package com.example.register.process;

/**
 *
 *
 * 集群服务里的 client，继承 Application 接口
 * 功能：
 * 1. 注册，
 * 2. 续约，
 * 3. 暂停，主动停止服务，暂停对外提供服务
 * 4. 下线，被动停止服务，一般是进程挂掉续约断了
 */
public interface RegistryClient extends Application{

    void export();

    void renew();

    void pause();

    void offline();

}
