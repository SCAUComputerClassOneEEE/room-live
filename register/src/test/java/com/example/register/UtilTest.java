package com.example.register;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.utils.CollectionUtil;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class UtilTest {
//    public static void main(String[] args) {
//        HashSet<Integer> same = new HashSet<>();
//        HashSet<Integer> f = new HashSet<>();
//        HashSet<Integer> s = new HashSet<>();
//        f.add(1);
//        f.add(2);
//        for (int i = 0; i < 5; i++) {
//            s.add(i + 2);
//        }
//        CollectionUtil.saveUniqueAndDuplicates(s,f,same);
//        System.out.println(same);
//        System.out.println(f);
//        System.out.println(s);
//    }

    public static void main(String[] args) throws JsonProcessingException {
        ServiceProvider serviceProvider = new ServiceProvider("client0", "123:123:123:0", 9000);
        System.out.println(JSONUtil.writeValue(serviceProvider));
    }
}
