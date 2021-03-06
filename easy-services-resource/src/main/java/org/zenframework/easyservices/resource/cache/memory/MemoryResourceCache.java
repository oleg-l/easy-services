package org.zenframework.easyservices.resource.cache.memory;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.easyservices.resource.cache.AbstractResourceCache;
import org.zenframework.easyservices.resource.cache.CacheItem;

public class MemoryResourceCache extends AbstractResourceCache {

    private final Map<String, MemoryCacheItem> items = new HashMap<String, MemoryCacheItem>();

    @Override
    public CacheItem getCacheItem(String resourcePath) {
        synchronized (items) {
            MemoryCacheItem item = items.get(resourcePath);
            if (item == null) {
                item = new MemoryCacheItem(resourcePath);
                items.put(resourcePath, item);
            }
            return item;
        }
    }

}
