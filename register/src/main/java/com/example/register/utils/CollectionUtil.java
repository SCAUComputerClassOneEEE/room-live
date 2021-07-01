package com.example.register.utils;

import java.util.*;

public class CollectionUtil {

    private CollectionUtil() { }

    /*
    * 去掉重复，剩下自己独有的
    * duplicates
    * unique
    * */
    public static<E> void saveUniqueAndDuplicates(Collection<E> firC, Collection<E> secC, Collection<E> same) {
        if (same == null || firC == null || secC == null)
            throw new RuntimeException("collection is null");
        if (!same.isEmpty())
            throw new RuntimeException("same collection is not empty");
        if (firC.isEmpty()) {
            firC.addAll(secC);
            secC.clear();
            return;
        }
        if (secC.isEmpty()) {
            secC.addAll(firC);
            firC.clear();
            return;
        }
        Collection<E> _maxC = firC.size() >= secC.size() ? firC : secC;
        Collection<E> _minC = firC.size() < secC.size() ? firC : secC;
        Set<E> set1 = new HashSet<>(); // fir
        for (E e : _maxC) {
            if (_minC.contains(e)) {
                same.add(e);
            } else {
                set1.add(e);
                // remove out secC
            }
        }
        secC.addAll(firC);
        secC.removeAll(set1);
        secC.removeAll(same);
        firC.clear();
        firC.addAll(set1);
    }

    public static<E> Collection<E> getDifferent(Collection<E> fc, Collection<E> fs) {
        Collection<E> csReturn = new LinkedList<>();
        Collection<E> min = fs.size() < fc.size() ? fs : fc;

        Map<E, Boolean> map = maxMap(fc, fs);
        for (E e : min) {
            if (map.get(e) == null) {
                // different
                csReturn.add(e);
            } else {
                // same and then over
                map.put(e, false);
            }
        }
        for (Map.Entry<E, Boolean> entry : map.entrySet()) {
            if (entry.getValue()) {
                csReturn.add(entry.getKey());
            }
        }
        return csReturn;
    }

    public static<E> Collection<E> getSame(Collection<E> fc, Collection<E> fs) {
        Collection<E> csReturn = new LinkedList<>();
        Collection<E> min = fs.size() < fc.size() ? fs : fc;

        Map<E, Boolean> map = maxMap(fc, fs);
        for (E e : min) {
            if (map.get(e) != null) {
                // equals
                csReturn.add(e);
            }
        }
        return csReturn;
    }


    private static <E> Map<E, Boolean> maxMap(Collection<E> fc, Collection<E> fs) {
        Collection<E> max = fc.size() < fs.size() ? fs : fc;

        Map<E, Boolean> map = new HashMap<>(max.size());
        for (E e : max) {
            map.put(e, true);
        }
        return map;
    }
}

