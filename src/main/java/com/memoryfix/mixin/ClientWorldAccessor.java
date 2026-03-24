package com.memoryfix.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.collection.IntObjectStorage;
import net.minecraft.world.chunk.ClientChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Accessor("clientNetHandler")
    void memoryfix$setClientNetHandler(ClientPlayNetworkHandler clientNetHandler);

    @Accessor("clientChunkCache")
    ClientChunkProvider memoryfix$getClientChunkCache();

    @Accessor("clientChunkCache")
    void memoryfix$setClientChunkCache(ClientChunkProvider clientChunkCache);

    @Accessor("field_1663")
    IntObjectStorage memoryfix$getEntityIndex();

    @Accessor("field_1663")
    void memoryfix$setEntityIndex(IntObjectStorage entityIndex);

    @Accessor("world")
    void memoryfix$setTrackedEntities(Set trackedEntities);

    @Accessor("entitiesForSpawn")
    void memoryfix$setPendingSpawnEntities(Set entitiesForSpawn);

    @Accessor("previousChunkPos")
    Set memoryfix$getPreviousChunkPos();
}
