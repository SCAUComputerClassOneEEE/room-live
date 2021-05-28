package com.example.register;

import java.lang.reflect.Method;
import java.util.HashMap;

public class RPCRegister {
    public RPCRegister() {

    }

    public static final HashMap<String, Method> map = new HashMap<>();

    public static void registerMethod(Object o) {
        Method[] methods = o.getClass().getDeclaredMethods();
        for (Method m : methods) {
            RpcProvider annotation = m.getAnnotation(RpcProvider.class);
            if (annotation != null) {
                String value = annotation.value();
                String name = m.getName();
                System.out.println("value:" + value + ", name:" + name);
                map.put(value, m);
            }
        }
    }

    public static Method reference(String value) {
        return map.get(value);
    }
}
