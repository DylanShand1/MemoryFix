package com.memoryfix.mixin;

import net.minecraft.util.collection.IntArrayCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(IntArrayCache.class)
public abstract class IntArrayCacheMixin {

    @Shadow private static int size;
    @Shadow private static List<int[]> cache;
    @Shadow private static List<int[]> allocated;
    @Shadow private static List<int[]> tcache;
    @Shadow private static List<int[]> tallocated;

    /**
     * Rebuild the cache from the arrays used by the latest biome generation pass
     * instead of retaining older oversized arrays across world changes.
     */
    @Overwrite
    public static synchronized void clear() {
        int nextSize = 256;
        if (!allocated.isEmpty()) {
            nextSize = allocated.get(0).length;
        }

        cache.clear();
        tcache.clear();
        cache.addAll(allocated);
        tcache.addAll(tallocated);
        allocated.clear();
        tallocated.clear();
        size = nextSize;
    }
}
