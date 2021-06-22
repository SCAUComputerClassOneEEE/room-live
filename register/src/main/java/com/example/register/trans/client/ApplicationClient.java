package com.example.register.trans.client;

import com.example.register.process.Application;
import com.example.register.process.RegistryClient;
import com.example.register.trans.ApplicationThread;
import com.example.register.trans.HttpTaskCarrierExecutor;
import com.example.register.trans.HttpTaskQueueConsumer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * queue for task
 * 独立的任务 tasker 处理线程。
 */
public class ApplicationClient extends ApplicationThread<Bootstrap, Channel> {

    private RegistryClient app;

    private EventLoopGroup clientNetWorkLoop;

    private BlockingQueue<HttpTaskCarrierExecutor> taskQueue;

    private final int taskQueueMaxSize;

    private static final HttpTaskQueueConsumer runnable = new HttpTaskQueueConsumer();

    public ApplicationClient(int taskQueueMaxSize) {
        super(runnable);
        this.taskQueueMaxSize = taskQueueMaxSize;
    }

    @Override
    public void init(Application application) throws Exception {
        if (application instanceof RegistryClient) {
            app = (RegistryClient) application;
        } else {
            throw new Exception("Client application thread init error.");
        }
        bootstrap = new Bootstrap();
        clientNetWorkLoop = new NioEventLoopGroup();
        final Bootstrap boots = (Bootstrap)bootstrap;
        boots.group(clientNetWorkLoop)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                .addLast(new HttpClientHandler(app));
                    }
                });
        taskQueue = new LinkedBlockingQueue<>(taskQueueMaxSize);
        runnable.setTaskQueue(taskQueue);
    }

    @Override
    public void stopThread() {
        if (runnable.getThread() != null && runnable.getThread().isAlive()){
            runnable.interrupt();
        }
        if (!clientNetWorkLoop.isTerminated()) {
            clientNetWorkLoop.shutdownGracefully();
        }
    }

}
