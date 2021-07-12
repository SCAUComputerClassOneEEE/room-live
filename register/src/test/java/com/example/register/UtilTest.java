package com.example.register;

import com.example.register.serviceInfo.ServiceProvider;
import com.example.register.spring.annotation.RegisterMapping;
import com.example.register.utils.CollectionUtil;
import com.example.register.utils.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

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

//    public static void main(String[] args) throws JsonProcessingException {
//        ServiceProvider serviceProvider = new ServiceProvider("client0", "123:123:123:0", 9000);
//        System.out.println(JSONUtil.writeValue(serviceProvider));
//    }

    public static void main(String[] args) throws IOException {
        ServiceProvider f = new ServiceProvider("haha", "localhost", 8000);

        f.incrementConnectingInt();
        f.fixAccessAvg(50);
        f.incrementConnectingInt();
        f.fixAccessAvg(0);
        f.decrementConnectingInt();
        f.decrementConnectingInt();
        String s = JSONUtil.writeValue(f);
        System.out.println(s);

        ServiceProvider serviceProvider = JSONUtil.readValue(s, ServiceProvider.class);
        System.out.println(serviceProvider);
    }

/*    public static void main(String[] args) {
        System.out.println(Arrays.toString(GetMapping.class.getAnnotations()));
        System.out.println(Arrays.toString(PostMapping.class.getAnnotations()));
        System.out.println(Arrays.toString(RestController.class.getAnnotations()));
    }*/
}
