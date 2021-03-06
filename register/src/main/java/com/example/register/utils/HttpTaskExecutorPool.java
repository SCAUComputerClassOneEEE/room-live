package com.example.register.utils;

import com.example.register.trans.client.HttpTaskCarrierExecutor;

import java.util.concurrent.*;

/**
 *
 * 线程池和等待结果的任务池
 */
public class HttpTaskExecutorPool extends ThreadPoolExecutor {

    /*
    * when executor.connectAndSend -> put
    * when executor.getResultString -> remove
    * when channel inbound -> get
    * */
    public static final ConcurrentHashMap<String/*taskId*/, HttpTaskCarrierExecutor> taskMap = new ConcurrentHashMap<>();

    private static final int DEFAULT_CORE_SIZE = 5;
    private static final int DEFAULT_MAX_SIZE = 10;
    private static final long DEFAULT_KEEP_ALIVE = 100;

    private static volatile HttpTaskExecutorPool INSTANCE;

    public static HttpTaskExecutorPool getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpTaskExecutorPool.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpTaskExecutorPool();
                }
            }
        }
        return INSTANCE;
    }

    private HttpTaskExecutorPool() {
        super(DEFAULT_CORE_SIZE, DEFAULT_MAX_SIZE, DEFAULT_KEEP_ALIVE,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new DamonThreadFactory(),
                new RejectedExecutionHandler0());
    }

    private static class DamonThreadFactory implements ThreadFactory{
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    }

    private static class RejectedExecutionHandler0 implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                Thread.sleep(100);
                executor.submit(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
