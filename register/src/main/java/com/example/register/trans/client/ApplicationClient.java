package com.example.register.trans.client;

import com.example.register.process.Application;
import com.example.register.process.DiscoveryNodeProcess;
import com.example.register.process.RegistryClient;
import com.example.register.process.ApplicationBootConfig;
import com.example.register.trans.ApplicationThread;
import com.example.register.utils.HttpTaskExecutorPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
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

    private static final Logger logger = LoggerFactory.getLogger(ApplicationClient.class);

    private DiscoveryNodeProcess app;

    private static BlockingQueue<HttpTaskCarrierExecutor> mainQueue; // public level 1

    private static final HttpTaskQueueConsumer runner = new HttpTaskQueueConsumer(); // 可以改成多个子执行器 list，麻烦。。。

    public ApplicationClient(DiscoveryNodeProcess application, ApplicationBootConfig config) throws Exception {
        super(runner);
        init(application, config);
    }

    @Override
    protected void init(Application application, ApplicationBootConfig config) throws Exception {
        if (this.isAlive()) return;

        int taskQueueMaxSize = config.getTaskQueueMaxSize();
        int nextSize = config.getNextSize();
        app = (DiscoveryNodeProcess) application;
        bootstrap = new Bootstrap();

        final Bootstrap boots = (Bootstrap)bootstrap;
        final Integer connectTimeOut = config.getConnectTimeOut();
        final Integer writeTimeOut = config.getWriteTimeOut();
        final Integer readTimeOut = config.getReadTimeOut();
        final int maxContentLength = config.getMaxContentLength();
        boots.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut) // connect time out 3000ms
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS)) // read time out 5000ms
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(maxContentLength))
                                .addLast(new HttpClientInBoundHandler(app))
                                .addLast(new HttpClientOutBoundHandler(app));
                    }
                });

        mainQueue = new LinkedBlockingQueue<>(taskQueueMaxSize);
        runner.init(mainQueue, nextSize,
                config.getMaxTolerateTimeMills(),
                config.getHeartBeatIntervals(),
                app);
    }

    @Override
    public void stopThread() {
//        runner.interrupt();
        super.stopThread();
        mainQueue.clear();
    }

    public void subTask(HttpTaskCarrierExecutor executor) throws Exception {
        if (!this.isAlive())
            throw new Exception("Client thread was interrupted.");
        mainQueue.put(executor);
    }

    protected static class HttpTaskQueueConsumer implements Runnable{
        static Queue<HttpTaskCarrierExecutor> taskQueue;
        static final HttpTaskExecutorPool pool = HttpTaskExecutorPool.getInstance();
        DiscoveryNodeProcess client;
        Thread thread;

        int maxTolerateTimeMills; // 最大的等待第二队列时间
        int heartBeatIntervals;

        Queue<HttpTaskCarrierExecutor> selfNextTaskQueue;
        int subQueueSize;

        @SneakyThrows
        @Override
        public void run() {
            thread = Thread.currentThread();
            long lastDoPacket = System.currentTimeMillis();
            while (!client.isStop()) {
                long cycleStart = System.currentTimeMillis();
                client.renew(client.getMyself(), false, client.getMyself().isPeer(), false); // 心跳
                if (cycleStart - lastDoPacket >= maxTolerateTimeMills
                        && !selfNextTaskQueue.isEmpty()) {
                    // 时间到了，而且有任务
                    doPacket();
                }
                HttpTaskCarrierExecutor take = taskQueue.peek();
                if (take != null && selfNextTaskQueue.size() < subQueueSize) { // 非阻塞
                    // 满了
                    doPacket();
                    taskQueue.poll();
                }
                long cycleEnd = System.currentTimeMillis();
                lastDoPacket = cycleEnd;
                Thread.sleep(heartBeatIntervals -
                        (cycleEnd - cycleStart));
            }
        }

        void doPacket() {
            LinkedList<HttpTaskCarrierExecutor> httpTaskCarrierExecutors = new LinkedList<>();
            while (!selfNextTaskQueue.isEmpty()) {
                httpTaskCarrierExecutors.add(selfNextTaskQueue.poll());
            }
            if (httpTaskCarrierExecutors.size() == 0) {
                return;
            }
            pool.execute(() -> {
                for (HttpTaskCarrierExecutor executor : httpTaskCarrierExecutors)
                    executor.connectAndSend();
            });
        }

        void init(Queue<HttpTaskCarrierExecutor> mainQueue,
                  int sQS, int mTT, int hbI,
                  DiscoveryNodeProcess app) {
            client = app;
            heartBeatIntervals = hbI;
            taskQueue = mainQueue;
            maxTolerateTimeMills = mTT;
            selfNextTaskQueue = new LinkedList<>();
            subQueueSize = sQS;
        }
//
//    public void interrupt() {
//        if (thread != null && thread.isAlive())
//            thread.interrupt();
//    }

        Thread getThread() {
            return thread;
        }
    }
}
