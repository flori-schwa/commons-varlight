package me.shawlaf.varlight.util;

import java.util.stream.Stream;


public final class StreamUtil {

    private StreamUtil() {

    }

    public static <T> Stream<T> checkedCast(Stream<?> any, Class<T> desiredType) {
        return cast(any.filter(desiredType::isInstance), desiredType);
    }

    public static <T> Stream<T> cast(Stream<?> self, Class<T> resultType) {
        return self.map(resultType::cast);
    }

}
