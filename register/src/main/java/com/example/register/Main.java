package com.example.register;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        // 注册方法
        RPCRegister.registerMethod(DemoTest.class.newInstance());

        // 调用方法
        Method test = RPCRegister.reference("test");
        Method test1 = RPCRegister.reference("test1");
        Method test2 = RPCRegister.reference("test2");


        // 方法执行
        test.invoke(DemoTest.class.newInstance());
        test1.invoke(DemoTest.class.newInstance());
        test2.invoke(DemoTest.class.newInstance());
    }
}
