package com.example.register;

import com.example.register.utils.CollectionUtil;

import java.util.HashSet;

public class UtilTest {
    public static void main(String[] args) {
        HashSet<Integer> same = new HashSet<>();
        HashSet<Integer> f = new HashSet<>();
        HashSet<Integer> s = new HashSet<>();
        f.add(1);
        f.add(2);
        for (int i = 0; i < 5; i++) {
            s.add(i + 2);
        }
        CollectionUtil.saveUniqueAndDuplicates(s,f,same);
        System.out.println(same);
        System.out.println(f);
        System.out.println(s);
    }
}
