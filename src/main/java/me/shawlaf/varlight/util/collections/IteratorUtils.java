package me.shawlaf.varlight.util.collections;

import java.util.Iterator;
import java.util.stream.Collector;

public final class IteratorUtils {

    private IteratorUtils() {

    }

    public static <T, A, R> R collectFromIterator(Iterator<T> iterator, Collector<T, A, R> collector) {
        A resultContainer = collector.supplier().get();

        while (iterator.hasNext()) {
            T next = iterator.next();

            collector.accumulator().accept(resultContainer, next);
        }

        return collector.finisher().apply(resultContainer);
    }

}
