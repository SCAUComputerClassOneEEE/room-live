package com.example.register.utils;

import com.example.register.trans.client.HttpTaskCarrierExecutor;

import java.util.concurrent.*;

/**
 *
 * 线程池和等待结果的任务池
 */
public class HttpTaskExecutorPool extends ThreadPoolExecutor {

    public static final ConcurrentHashMap<String/*taskId*/, HttpTaskCarrierExecutor> taskMap = new ConcurrentHashMap<>();

    private static final int DEFAULT_CORE_SIZE = 2;
    private static final int DEFAULT_MAX_SIZE = 10;
    private static final long DEFAULT_KEEP_ALIVE = 2;

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

    public void submit0(Runnable runnable) {
        INSTANCE.submit(runnable);
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
