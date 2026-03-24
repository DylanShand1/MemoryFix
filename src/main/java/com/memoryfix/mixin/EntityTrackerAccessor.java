package com.memoryfix.mixin;

import net.minecraft.entity.EntityTracker;
import net.minecraft.util.collection.IntObjectStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor("trackedEntities")
    Set memoryfix$getTrackedEntities();

    @Accessor("trackedEntities")
    void memoryfix$setTrackedEntities(Set trackedEntities);

    @Accessor("trackedEntityIds")
    IntObjectStorage memoryfix$getTrackedEntityIds();

    @Accessor("trackedEntityIds")
    void memoryfix$setTrackedEntityIds(IntObjectStorage trackedEntityIds);
}
