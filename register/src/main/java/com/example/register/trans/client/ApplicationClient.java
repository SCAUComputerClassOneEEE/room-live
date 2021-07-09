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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
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

    private RegistryClient app;

    private static BlockingQueue<HttpTaskCarrierExecutor> mainQueue; // public level 1
    private static BlockingQueue<HttpTaskCarrierExecutor> subQueue;

    private static final HttpTaskQueueConsumer runner = new HttpTaskQueueConsumer(); // 可以改成多个子执行器 list，麻烦。。。

    public ApplicationClient(Application application, ApplicationBootConfig config) throws Exception {
        super(runner);
        init(application, config);
    }

    @Override
    protected void init(Application application, ApplicationBootConfig config) throws Exception {
        if (this.isAlive()) return;

        if (application instanceof DiscoveryNodeProcess) {
            app = (DiscoveryNodeProcess) application;
        } else {
            throw new Exception("Client application thread init error.");
        }
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
//                                .addLast(new HttpObjectAggregator(maxContentLength))
                                .addLast(new HttpClientInBoundHandler(app))
                                .addLast(new HttpClientOutBoundHandler(app));
                    }
                });

        mainQueue = new LinkedBlockingQueue<>(config.getTaskQueueMaxSize());
        subQueue = new LinkedBlockingQueue<>(config.getNextSize());
        runner.init(mainQueue, subQueue,
                config.getMaxTolerateTimeMills(),
                config.getHeartBeatIntervals(),
                app);
    }

    @Override
    public void stopThread() {
//        runner.interrupt();
        super.stopThread();
        mainQueue.clear();
        subQueue.clear();
    }

    public void subTask(HttpTaskCarrierExecutor executor) throws Exception {
        if (!this.isAlive())
            throw new Exception("Client thread was interrupted.");
        do {
            if (mainQueue.add(executor)) {
                logger.debug(executor.getTaskId() + " queue subTask successfully.");
                break;
            }
        } while (true);
    }

    protected static class HttpTaskQueueConsumer implements Runnable{
        static BlockingQueue<HttpTaskCarrierExecutor> taskQueue;
        static final HttpTaskExecutorPool pool = HttpTaskExecutorPool.getInstance();
        RegistryClient client;
        Thread thread;

        int maxTolerateTimeMills; // 最大的等待第二队列时间
        int heartBeatIntervals;

        BlockingQueue<HttpTaskCarrierExecutor> selfNextTaskQueue;

        @Override
        public void run() {
            thread = Thread.currentThread();
            long lastDoPacket = System.currentTimeMillis();
            long lastRenewStamp = System.currentTimeMillis();
            while (true) {
                try {
//                    logger.info("waiting if queue has any task.");
                    HttpTaskCarrierExecutor take = taskQueue.poll(); // 阻塞
                    // thread pool submit to do
                    if (take != null && !selfNextTaskQueue.offer(take)) { // 非阻塞
                        // 满了
                        //logger.debug("doPacket selfNextTaskQueue is full.");
                        doPacket();
                        lastDoPacket = System.currentTimeMillis();
                    }
                    if (System.currentTimeMillis() - lastDoPacket >= maxTolerateTimeMills) {
                        // 时间到了
                        //logger.debug("doPacket maxTolerateTimeMills is full.");
                        doPacket();
                        lastDoPacket = System.currentTimeMillis();
                    }

                    if (System.currentTimeMillis() - lastRenewStamp >= heartBeatIntervals) {
                        client.renew(client.getMyself(), false, client.getMyself().isPeer(), false); // 心跳
                        lastRenewStamp = System.currentTimeMillis();
                    }
                } catch (Exception e) {
                    // thread interrupt, and while break out
                    break;
                }
            }
        }

        void doPacket() throws InterruptedException {
            LinkedList<HttpTaskCarrierExecutor> httpTaskCarrierExecutors = new LinkedList<>();
            while (!selfNextTaskQueue.isEmpty()) {
                httpTaskCarrierExecutors.add(selfNextTaskQueue.take());
            }
            if (httpTaskCarrierExecutors.size() == 0) {
                return;
            }
            pool.execute(() -> {
                for (HttpTaskCarrierExecutor executor : httpTaskCarrierExecutors)
                    executor.connectAndSend();
            });
        }

        void init(BlockingQueue<HttpTaskCarrierExecutor> mainQueue,
                  BlockingQueue<HttpTaskCarrierExecutor> subQueue,
                  int mTT,
                  int hbI,
                  RegistryClient app) {
            client = app;
            heartBeatIntervals = hbI;
            taskQueue = mainQueue;
            maxTolerateTimeMills = mTT;
            selfNextTaskQueue = subQueue;
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
