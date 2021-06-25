package com.example.register.trans.client;


import com.example.register.utils.HttpTaskExecutorPool;

import java.util.concurrent.BlockingQueue;

public class HttpTaskQueueConsumer implements Runnable{
    private static BlockingQueue<HttpTaskCarrierExecutor> taskQueue;
    private static final HttpTaskExecutorPool pool = HttpTaskExecutorPool.getInstance();

    private Thread thread;

    private static final long maxTolerateTimeMills = 2;

    private BlockingQueue<HttpTaskCarrierExecutor> selfNextTaskQueue;

    @Override
    public void run() {
        thread = Thread.currentThread();
        long last = System.currentTimeMillis();
        while (true) {
            try {
                HttpTaskCarrierExecutor take = taskQueue.take(); // 阻塞
                // thread pool submit to do
                if (!selfNextTaskQueue.offer(take)) { // 非阻塞
                    // 满了
                    doPacket();
                    last = System.currentTimeMillis();
                } else { // 未满
                    if (last - System.currentTimeMillis() >= maxTolerateTimeMills) {
                        // 时间到了
                        doPacket();
                        last = System.currentTimeMillis();
                    }
                }
            } catch (InterruptedException e) {
                // thread interrupt, and while break out
                break;
            }
        }
    }

    private void doPacket() {
        HttpTaskCarrierExecutor[] httpTaskCarrierExecutors = (HttpTaskCarrierExecutor[]) selfNextTaskQueue.toArray();
        pool.submit0(() -> {
            for (HttpTaskCarrierExecutor executor : httpTaskCarrierExecutors)
                executor.connectAndSend();
        });
    }

    public void init(BlockingQueue<HttpTaskCarrierExecutor> mainQueue, BlockingQueue<HttpTaskCarrierExecutor> subQueue) {
        taskQueue = mainQueue;
        selfNextTaskQueue = subQueue;
    }
//
//    public void interrupt() {
//        if (thread != null && thread.isAlive())
//            thread.interrupt();
//    }

    public Thread getThread() {
        return thread;
    }
}
