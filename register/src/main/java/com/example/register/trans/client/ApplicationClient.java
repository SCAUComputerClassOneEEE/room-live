package com.example.register.trans.client;

import com.example.register.process.Application;
import com.example.register.process.RegistryClient;
import com.example.register.serviceInfo.ServiceProvidersBootConfig;
import com.example.register.trans.ApplicationThread;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
    public void init(Application application, ServiceProvidersBootConfig config) throws Exception {
        if (this.isAlive()) return;

        if (application instanceof RegistryClient) {
            app = (RegistryClient) application;
        } else {
            throw new Exception("Client application thread init error.");
        }
        bootstrap = new Bootstrap();
        clientNetWorkLoop = new NioEventLoopGroup();
        final Bootstrap boots = (Bootstrap)bootstrap;
        final Integer connectTimeOut = config.getConnectTimeOut();
        final Integer readTimeOut = config.getReadTimeOut();
        final int maxContentLength = config.getMaxContentLength();
        boots.group(clientNetWorkLoop)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut) // connect time out 3000ms
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS)) // read time out 5000ms
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(maxContentLength))
                                .addLast("task in handler", new HttpClientInBoundHandler(app))
                                .addLast("task out handler", new HttpClientOutBoundHandler(app));
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

    public void subTask(HttpTaskCarrierExecutor executor) throws Exception {
        if (!this.isAlive())
            throw new Exception("Client thread was interrupted.");
        do {
            if (mainQueue.add(executor)) {
                break;
            }
        } while (true);

    }
}
