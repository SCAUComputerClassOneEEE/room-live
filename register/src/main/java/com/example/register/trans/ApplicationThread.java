package com.example.register.trans;


import com.example.register.process.Application;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;

public abstract class ApplicationThread<B extends AbstractBootstrap<B, C>, C extends Channel> implements Runnable {
    protected AbstractBootstrap<B, C> bootstrap;

    public abstract void init(Application application) throws Exception;
}
