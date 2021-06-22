package com.example.register;

import com.example.register.serviceInfo.ServiceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


class ServiceCenterPeerClientTests {


    void contextLoads() {

    }

    public static void main(String[] args) {
        ServiceProvider localhost = new ServiceProvider("localhost", 8080, ServiceProvider.TypeServiceProvider.Server);
        System.out.println(Integer.toBinaryString(localhost.hashCode()));
    }

}
