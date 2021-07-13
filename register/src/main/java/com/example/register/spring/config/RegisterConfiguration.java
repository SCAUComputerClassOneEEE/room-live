package com.example.register.spring.config;

import com.example.register.process.ApplicationBootConfig;
import com.example.register.spring.SpringDiscoverNodeProcess;
import com.example.register.spring.SpringNameCenterPeerProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(RegisterProperties.class)
@ConditionalOnProperty(prefix = "register", name = "serviceType")
public class RegisterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RegisterConfiguration.class);

    @Resource
    private RegisterProperties registerProperties;

    @Bean
    @Scope(value = "singleton")
//    @Conditional({RegisterCondition.class})
    public SpringNameCenterPeerProcess peer() {
        if (registerProperties.getServiceType().equals("peer")) {
            logger.info("SpringNameCenterPeerProcess initialized");
            ApplicationBootConfig config = new ApplicationBootConfig(registerProperties);
            return new SpringNameCenterPeerProcess(config);
        }
        return new SpringNameCenterPeerProcess(null);
    }

    @Bean
    @Scope(value = "singleton")
//    @Conditional({RegisterCondition.class})
    public SpringDiscoverNodeProcess node() {
        if (registerProperties.getServiceType().equals("node")) {
            logger.info("SpringDiscoverNodeProcess initialized");
            ApplicationBootConfig config = new ApplicationBootConfig(registerProperties);
            return new SpringDiscoverNodeProcess(config);
        }
        return new SpringDiscoverNodeProcess(null);
    }
}
