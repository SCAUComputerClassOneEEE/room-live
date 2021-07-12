package com.example.register.spring;

import com.example.register.process.ApplicationBootConfig;
import com.example.register.process.DiscoveryNodeProcess;
import com.example.register.serviceInfo.MethodInstance;
import com.example.register.spring.annotation.RegisterMapping;
import com.sun.istack.internal.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;

public class SpringDiscoverNodeProcess extends DiscoveryNodeProcess implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static ApplicationContext applicationContext;

    public SpringDiscoverNodeProcess(ApplicationBootConfig config) {
        super(config);
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);
            Map<Method, RegisterMapping> registerMappingMap;
            registerMappingMap = MethodIntrospector.selectMethods(
                    bean.getClass(),
                    (MethodIntrospector.MetadataLookup<RegisterMapping>) method
                            -> AnnotatedElementUtils.findMergedAnnotation(method, RegisterMapping.class)
            );
            if (registerMappingMap.isEmpty()) {
                continue;
            }
            for (Map.Entry<Method, RegisterMapping> mappingEntry : registerMappingMap.entrySet()) {
                Method mappingMd = mappingEntry.getKey();
                RegisterMapping rm = mappingEntry.getValue();
                if (rm == null) continue;
                MethodInstance instance = new MethodInstance();
                instance.setMethod(rm.method());
                instance.setConsumes(rm.consumes());
                instance.setHeaders(rm.headers());
                instance.setName(rm.name());
                instance.setParams(rm.params());
                instance.setPath(rm.path());
                instance.setProduces(rm.produces());
                instance.setValue(rm.value());
                mySelf.addMethod(instance);
            }
        }
        /*
         * register myself
         * */
        register(mySelf,true, false, false);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

}
