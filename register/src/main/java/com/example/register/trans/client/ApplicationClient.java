package com.example.register.trans.client;

import com.example.register.process.Application;
import com.example.register.process.RegistryClient;
import com.example.register.trans.ApplicationThread;
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
 *
 * 处理接收到的 http 响应
 */
public class ApplicationClient extends ApplicationThread<Bootstrap, Channel> {

    private RegistryClient app;

    private EventLoopGroup clientNetWorkLoop;

    private static BlockingQueue<HttpTaskCarrierExecutor> mainQueue; // public level 1
    private static BlockingQueue<HttpTaskCarrierExecutor> subQueue;

    private final int taskQueueMaxSize;
    private final int nextSize;

    private static final HttpTaskQueueConsumer runner = new HttpTaskQueueConsumer(); // 可以改成多个子执行器 list，麻烦。。。

    public ApplicationClient(int taskQueueMaxSize, int nextSize) {
        super(runner);
        this.taskQueueMaxSize = taskQueueMaxSize;
        this.nextSize = nextSize;
    }

    @Override
    public void init(Application application) throws Exception {
        if (this.isAlive()) return;

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
                                .addLast(new HttpClientInBoundHandler(app))
                                .addLast(new HttpClientOutBoundHandler(app));
                    }
                });

        mainQueue = new LinkedBlockingQueue<>(taskQueueMaxSize);
        subQueue = new LinkedBlockingQueue<>(nextSize);
        runner.init(mainQueue, subQueue);
    }

    @Override
    public void stopThread() {
//        runner.interrupt();
        if (!this.isAlive()) return;

        this.interrupt();
        if (!clientNetWorkLoop.isTerminated()) {
            clientNetWorkLoop.shutdownGracefully();
        }
        mainQueue.clear();
        subQueue.clear();
    }

    public boolean subTask(HttpTaskCarrierExecutor executor) throws Exception {
        if (!this.isAlive())
            throw new Exception("Client thread was interrupted.");
        return mainQueue.add(executor);
    }

}
