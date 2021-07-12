package com.example.register.spring;

import com.example.register.process.ApplicationBootConfig;
import com.example.register.process.DiscoveryNodeProcess;
import com.example.register.process.NameCenterPeerProcess;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringNameCenterPeerProcess extends SpringDiscoverNodeProcess implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {

    public SpringNameCenterPeerProcess(ApplicationBootConfig config) {
        super(config);
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

}
