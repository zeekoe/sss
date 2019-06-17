package nl.ronaldteune.sss.cache;

import java.time.Instant;

public class CacheObject {
    private Instant instant;
    private Object cacheObject;

    public CacheObject(Instant instant, Object cacheObject) {
        this.instant = instant;
        this.cacheObject = cacheObject;
    }

    public Instant getInstant() {
        return instant;
    }

    public Object getCacheObject() {
        return cacheObject;
    }
}
