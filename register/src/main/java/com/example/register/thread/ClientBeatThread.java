package com.example.register.thread;

public class ClientBeatThread {
    private static final ClientBeatThread instance = new ClientBeatThread();
    private Thread beatThread;
    private volatile boolean isStop = true; //beat is stop

    public ClientBeatThread getInstance() {
        return instance;
    }

    public void start() {
        beatThread = new Thread(()->{
            while (!isStop) {
                // beat to server
            }
        });
        beatThread.start();

        isStop = false;
    }

    public void stop() {
        isStop = true;
        if (beatThread != null) {
            beatThread.interrupt();
            try {
                beatThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // ...
            }
        }
    }
}
