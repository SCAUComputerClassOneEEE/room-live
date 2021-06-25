package com.example.register;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;


class ServiceCenterPeerClientTests {


    void contextLoads() {

    }

    public static void main(String[] args) throws IOException {
        ServiceProvider localhost = new ServiceProvider("aa", "localhost", 8080, ServiceProvider.TypeServiceProvider.Server);
        localhost.incrementConnectingInt();
        localhost.incrementConnectingInt();
        localhost.incrementConnectingInt();
        localhost.fixAccessAvg(77.0);
        localhost.fixAccessAvg(76.0);
        localhost.fixAccessAvg(75.0);


        String s = JSONUtil.writeValue(localhost);

        System.out.println(s);
        ServiceProvider serviceProvider = JSONUtil.readValue(s, ServiceProvider.class);
        serviceProvider.getInfo().incrementVersion();

        String s1 = JSONUtil.writeValue(serviceProvider);

        System.out.println(s1);

    }

}
