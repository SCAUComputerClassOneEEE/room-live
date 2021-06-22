package com.example.register.trans;


import com.example.register.process.Application;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;

public abstract class ApplicationThread<B extends AbstractBootstrap<B, C>, C extends Channel> extends Thread {
    protected AbstractBootstrap<B, C> bootstrap;

    public ApplicationThread(Runnable runnable) {
        super(runnable);
    }
    public abstract void init(Application application) throws Exception;
    public abstract void stopThread();

    public AbstractBootstrap<B, C> getBootstrap() {
        return bootstrap;
    }
}
