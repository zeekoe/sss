package nl.ronaldteune.sss.cache;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** @noinspection unchecked*/
public class Cache<T> {
    private static Clock clock = Clock.systemUTC();
    private Map<String, CacheObject> objects = new HashMap<>();

    public T get(String identifier, Supplier<T> method) {
        if(objects.containsKey(identifier)) {
            System.out.println("from cache: " + identifier);
            return (T) objects.get(identifier).getCacheObject();
        }
        T result = method.get();
        objects.put(
                identifier,
                new CacheObject(clock.instant(), result)
        );
        System.out.println("from real: " + identifier);
        return result;
    }
}
