package com.example.register.spring.config;

import lombok.SneakyThrows;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardMethodMetadata;

public class RegisterCondition implements Condition {

    @SneakyThrows
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        ConfigurableListableBeanFactory beanFactory = conditionContext.getBeanFactory();
        if (beanFactory == null)
            throw new Exception("??");
        RegisterProperties bean = beanFactory.getBean(RegisterProperties.class);

        String serviceType = bean.getServiceType();
        StandardMethodMetadata methodMetadata = (StandardMethodMetadata) annotatedTypeMetadata;
        return  methodMetadata.getMethodName().equals(serviceType);
    }
}
