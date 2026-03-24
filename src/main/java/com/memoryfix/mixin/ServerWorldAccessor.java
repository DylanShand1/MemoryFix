package com.memoryfix.mixin;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.PortalTeleporter;
import net.minecraft.server.PlayerWorldManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.IntObjectStorage;
import net.minecraft.world.chunk.ServerChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
    @Accessor("entityTracker")
    EntityTracker memoryfix$getEntityTracker();

    @Accessor("playerWorldManager")
    PlayerWorldManager memoryfix$getPlayerWorldManager();

    @Accessor("chunkCache")
    ServerChunkProvider memoryfix$getChunkCache();

    @Accessor("chunkCache")
    void memoryfix$setChunkCache(ServerChunkProvider chunkCache);

    @Accessor("portalTeleporter")
    PortalTeleporter memoryfix$getPortalTeleporter();

    @Accessor("field_2818")
    IntObjectStorage memoryfix$getEntityIdIndex();

    @Accessor("field_2818")
    void memoryfix$setEntityIdIndex(IntObjectStorage entityIdIndex);
}
