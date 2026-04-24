package services;

import Model.PetType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory cache for active pet types with a 1-hour TTL.
 * Singleton accessed via getInstance().
 */
public class PetTypeCache {

    private static final long TTL_SECONDS = 3600;
    private static final PetTypeCache INSTANCE = new PetTypeCache();

    private final CopyOnWriteArrayList<PetType> cached = new CopyOnWriteArrayList<>();
    private final AtomicLong lastPopulated = new AtomicLong(0);

    private PetTypeCache() {}

    public static PetTypeCache getInstance() {
        return INSTANCE;
    }

    /**
     * Returns true if the cache is stale (not populated or older than TTL).
     */
    public boolean isStale() {
        long ageSeconds = (System.currentTimeMillis() - lastPopulated.get()) / 1000;
        return lastPopulated.get() == 0 || ageSeconds > TTL_SECONDS;
    }

    /**
     * Update the cache with fresh data.
     */
    public void update(List<PetType> petTypes) {
        cached.clear();
        if (petTypes != null) {
            cached.addAll(petTypes);
        }
        lastPopulated.set(System.currentTimeMillis());
    }

    /**
     * Get cached pet types. Returns empty list if cache is empty.
     */
    public List<PetType> get() {
        return Collections.unmodifiableList(cached);
    }

    /**
     * Invalidate the cache so the next request triggers a reload.
     */
    public void invalidate() {
        lastPopulated.set(0);
    }
}
