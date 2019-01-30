package net.dumbcode.dumblibrary.server.utils;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

import java.util.List;

/* Made using https://bit.ly/2G8n1xu */
public class DumbCache<K, V> {

    private long timeToLive;
    private LRUMap cache;

    /**
     * Makes a new instance of DumbCache and starts a thread for the cache.
     * @param timeToLive How long each item should be in the cache.
     * @param timeBetweenCleanup The interval at which the thread runs.
     * @param maxItems Max items that can be in the cache.
     */
    public DumbCache(long timeToLive, long timeBetweenCleanup, int maxItems)
    {
        this.timeToLive = timeToLive;
        this.cache = new LRUMap(maxItems);

        if(timeToLive > 0 && timeBetweenCleanup > 0)
        {
            Thread thread = new Thread(() -> {
                while(true)
                {
                    try {
                        Thread.sleep(timeBetweenCleanup * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cleanCache();
                }
            });
            thread.setDaemon(true); // Allows the program to exit even when the cache is running.
            thread.start();
        }
    }

    public void put(K key, V value)
    {
        synchronized (cache) { // Allows one thread to access the object at a time.
            cache.put(key, new CacheObject(value));
        }
    }

    public V get(K key)
    {
        synchronized (cache)
        {
            CacheObject obj = (CacheObject) cache.get(key);
            if(obj != null)
            {
                obj.lastUsed = System.currentTimeMillis();
                return obj.value;
            }
        }
        return null;
    }

    public void cleanCache()
    {
        long currentTime = System.currentTimeMillis();
        List<K> objectsToDelete;

        // Finding all the objects that need to be removed from the cache.
        synchronized (cache)
        {
            MapIterator iterator = cache.mapIterator();
            objectsToDelete = Lists.newArrayList();
            K key;
            CacheObject object;

            while(iterator.hasNext())
            {
                key = (K) iterator.next();
                object = (CacheObject) iterator.getValue();

                if(object != null && currentTime > (object.lastUsed + timeToLive))
                {
                    objectsToDelete.add(key);
                }
            }
        }

        // Removing the found objects.
        for(K key : objectsToDelete) {
            synchronized (cache)
            {
                cache.remove(key);
            }
        }
    }

    public void remove(K key)
    {
        synchronized (cache)
        {
            cache.remove(key);
        }
    }

    public int size()
    {
        synchronized (cache)
        {
            return cache.size();
        }
    }

    /**
     * Class representing the value of the object we're looking for.
     * This is needed to keep track of the time the object was last queried.
     */
    protected class CacheObject
    {
        private long lastUsed = System.currentTimeMillis();
        private V value;

        protected CacheObject(V value)
        {
            this.value = value;
        }
    }
}
