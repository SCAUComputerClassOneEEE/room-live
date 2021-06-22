package com.example.register.trans;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class HttpTaskQueueConsumer implements Runnable{
    private BlockingQueue<HttpTaskCarrierExecutor> taskQueue;
    private Thread thread;

    // thread pool
    private ExecutorService pool;

    @Override
    public void run() {
        thread = Thread.currentThread();
        while (true) {
            try {
                HttpTaskCarrierExecutor take = taskQueue.take(); // 阻塞
                // thread pool submit to do
                pool.submit(take);

            } catch (InterruptedException e) {
                // thread interrupt, and while break out
                break;
            }
        }
    }

    public void setTaskQueue(BlockingQueue<HttpTaskCarrierExecutor> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void interrupt() {
        thread.interrupt();
    }

    public Thread getThread() {
        return thread;
    }
    public void setPool(ExecutorService pool) {
        this.pool = pool;
    }
}
