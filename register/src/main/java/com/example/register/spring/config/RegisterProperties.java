package com.example.register.spring.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "register")
public class RegisterProperties {

    private String enabled;

    /*
    * self:
    *   host:
    *   port:
    * */
    private Address self;
    /*
    * peers:
    *   - host:
    *     port:
    *   - host:
    *     port:
    *   - ...
    * */
    private List<Address> peers;

    /*thread*/
    @Value("register.workerNThread:1")
    private int workerNThread;
    @Value("register.bossNThread:1")
    private int bossNThread;
    /*
     * client
     * */
    @Value("register.taskQueueMaxSize:20")
    private int taskQueueMaxSize;
    @Value("register.nextQueueSize:4")
    private int nextQueueSize;
    @Value("register.connectTimeOut:3000")
    private int connectTimeOut; // mills
    @Value("register.readTimeOut:3000")
    private int readTimeOut; // mills
    @Value("register.maxTolerateTimeMills:500")
    private int maxTolerateTimeMills;
    @Value("register.heartBeatIntervals:30000")
    private int heartBeatIntervals;

    /*
     * server
     * */
    @Value("register.writeTimeOut:3000")
    private int writeTimeOut; // mills
    @Value("register.writeTimeOut:8080")
    private int serverPort;
    @Value("register.maxContentLength:5242880")
    private int maxContentLength;
    @Value("register.backLog:1024")
    private int backLog;

    @Data
    public static class Address {
        String appName = "default_client-application";
        String host;
        int port;
        String protocol = "http://";
    }
}
