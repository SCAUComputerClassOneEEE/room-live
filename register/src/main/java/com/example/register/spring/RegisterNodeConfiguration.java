package com.example.register.spring;

import com.example.register.process.ApplicationBootConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(RegisterProperties.class)
@ConditionalOnProperty(prefix = "register.serviceType", havingValue = "node")
public class RegisterNodeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RegisterNodeConfiguration.class);

    @Resource
    private RegisterProperties registerProperties;

    @Bean
    @Scope(value = "singleton")
    public SpringDiscoverNodeProcess springDiscoverNodeProcess() {
        logger.info("SpringDiscoverNodeProcess init...");
        ApplicationBootConfig config = new ApplicationBootConfig(registerProperties);
        SpringDiscoverNodeProcess nodeProcess = new SpringDiscoverNodeProcess(config);
        nodeProcess.start();
        logger.info("SpringDiscoverNodeProcess start...");
        return nodeProcess;
    }
}
