package me.shawlaf.varlight.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public final class CollectionUtil {
    private CollectionUtil() {

    }

    public static <E> List<E> toList(E[] array) {
        List<E> list = new ArrayList<>(array.length);

        for (int i = 0; i < array.length; i++) {
            list.add(i, array[i]);
        }

        return list;
    }

    public static <T> CountingIterator<T> createCountingIterator(Iterator<T> iterator) {
        return new CountingIterator<>(iterator);
    }
}
