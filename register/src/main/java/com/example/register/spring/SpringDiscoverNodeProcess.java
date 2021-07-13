package com.example.register.spring;

import com.example.register.process.ApplicationBootConfig;
import com.example.register.process.DiscoveryNodeProcess;
import com.example.register.serviceInfo.MethodInstance;
import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.spring.annotation.RegisterMapping;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringDiscoverNodeProcess extends DiscoveryNodeProcess implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static ApplicationContext applicationContext;
    protected static List<MethodInstance> methodMappingCache;

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
//                Method mappingMd = mappingEntry.getKey();
                RegisterMapping rm = mappingEntry.getValue();
                if (rm == null) continue;
                if (rm.path().length == 0 && rm.value().length == 0) continue;
                MethodInstance instance = new MethodInstance();
                instance.setMethod(rm.method());
                instance.setConsumes(rm.consumes());
                instance.setHeaders(rm.headers());
                instance.setName(rm.name());
                instance.setParams(rm.params());
                instance.setPath(rm.path().length == 0 ? rm.value() : rm.path());
                instance.setProduces(rm.produces());
                instance.setValue(rm.value().length == 0 ? rm.path() : rm.value());
                mySelf.addMethod(instance);
            }
        }
        methodMappingCache = mySelf.getMethodMappingList();
        /* register myself */
        register(null,true, false, false);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public List<MethodInstance>/*url*/ filterMethodForApplying(String appName, RequestMethod method, String p) throws Exception {
        final ServiceProvider serviceProvider = new ServiceProvider();
        MethodInstance[] appMethodsInfo = getAllMethodsMapping(appName, serviceProvider);
        if (appMethodsInfo == null) {
            discover(null, appName, true, null);
            appMethodsInfo = getAllMethodsMapping(appName, serviceProvider);
            if (appMethodsInfo == null) throw new Exception("No such app service or method.");
        }
        return Arrays.stream(appMethodsInfo)
                .filter(methodInstance -> Arrays.asList(methodInstance.getMethod()).contains(method))
                .filter(methodInstance -> Arrays.asList(methodInstance.getPath()).contains(p))
                .collect(Collectors.toList());
    }
}
