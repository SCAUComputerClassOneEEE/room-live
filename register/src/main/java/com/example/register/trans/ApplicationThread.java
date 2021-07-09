package com.example.register.trans;


import com.example.register.process.Application;
import com.example.register.process.ApplicationBootConfig;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public abstract class ApplicationThread<B extends AbstractBootstrap<B, C>, C extends Channel> extends Thread {
    protected static final EventLoopGroup workerGroup = new NioEventLoopGroup(1);
    protected AbstractBootstrap<B, C> bootstrap;
    protected ChannelFuture future;

    public ApplicationThread(Runnable runnable) {
        super(runnable);
    }
    protected abstract void init(Application application, ApplicationBootConfig config) throws Exception;
    public void stopThread() {
        if (future != null && future.channel().isActive()) {
            try {
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!this.isAlive())
            return;
        this.interrupt();
        if (!workerGroup.isTerminated())
            workerGroup.shutdownGracefully();
    }

    public final AbstractBootstrap<B, C> getBootstrap() {
        return bootstrap;
    }
}
