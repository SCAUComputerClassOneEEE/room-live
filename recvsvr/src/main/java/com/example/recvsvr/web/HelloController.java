package com.example.recvsvr.web;

import com.example.register.spring.SpringDiscoverNodeProcess;
import com.example.register.spring.SpringNameCenterPeerProcess;
import com.example.register.spring.annotation.RegisterMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {
    @Resource
    private SpringNameCenterPeerProcess nameCenterPeerProcess;

    @Resource
    private SpringDiscoverNodeProcess springDiscoverNodeProcess;

    @RegisterMapping(value = "/test", name = "testApi")
    public void test() {

    }
}
