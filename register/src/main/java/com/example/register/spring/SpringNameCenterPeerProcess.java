package com.example.register.spring;

import com.example.register.process.ApplicationBootConfig;
import com.example.register.process.NameCenterPeerProcess;
import com.example.register.serviceInfo.MethodInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

public class SpringNameCenterPeerProcess extends NameCenterPeerProcess implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(SpringDiscoverNodeProcess.class);
    private static ApplicationContext applicationContext;
    private static List<MethodInstance> methodMappingCache;
    public SpringNameCenterPeerProcess(ApplicationBootConfig config) {
        super(config);
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!initialized) return;
        logger.debug("Scanning the request mapping");
        methodMappingCache = SpringDiscoverNodeProcess.initMethodMappingCache(applicationContext, mySelf);
        /* register myself */
        start();
        logger.info("Starting NameCenterPeerProcess");
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}
