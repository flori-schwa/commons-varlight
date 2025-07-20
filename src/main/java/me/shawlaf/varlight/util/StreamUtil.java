package me.shawlaf.varlight.util;

import java.util.stream.Stream;


public final class StreamUtil {

    private StreamUtil() {

    }

    public static <T> Stream<T> filterCast(Stream<?> any, Class<T> desiredType) {
        return any.filter(desiredType::isInstance).map(desiredType::cast);
    }

    @SuppressWarnings("unchecked")
    public static <F, T> Stream<T> ofType(Stream<F> self, Class<T> resultType) {
        return self.map(f -> (T) f);
    }

}
