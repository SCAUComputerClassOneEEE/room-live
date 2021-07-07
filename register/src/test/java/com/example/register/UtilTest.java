package com.example.register;

import com.example.register.utils.CollectionUtil;

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

    public static void main(String[] args) {
        Timer timer = new Timer();
        System.out.println(new Date());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(new Date());
            }
        }, 1000, 1000);
    }
}
