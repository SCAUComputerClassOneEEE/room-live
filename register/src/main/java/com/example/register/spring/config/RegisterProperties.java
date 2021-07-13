package com.example.register.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "register")
public class RegisterProperties {

    private String serviceType;

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

    private int workerNThread = 1;

    private int bossNThread = 1;
    /*
     * client
     * */

    private int taskQueueMaxSize = 20;

    private int nextQueueSize = 4;

    private int connectTimeOut = 3000; // mills

    private int readTimeOut = 3000; // mills

    private int maxTolerateTimeMills = 500;

    private int heartBeatIntervals = 30000;

    /*
     * server
     * */

    private int writeTimeOut = 3000; // mills

    private int serverPort = 8000;

    private int maxContentLength = 5242880;

    private int backLog = 1024;

    @Data
    public static class Address {
        String appName = "default_client-application";
        String host;
        int port;
        String protocol = "http://";
    }
}
