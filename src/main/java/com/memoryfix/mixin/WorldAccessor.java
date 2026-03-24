package com.memoryfix.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("loadedEntities")
    List memoryfix$getLoadedEntities();

    @Accessor("loadedEntities")
    void memoryfix$setLoadedEntities(List loadedEntities);

    @Accessor("unloadedEntities")
    List memoryfix$getUnloadedEntities();

    @Accessor("unloadedEntities")
    void memoryfix$setUnloadedEntities(List unloadedEntities);

    @Accessor("blockEntities")
    List memoryfix$getBlockEntities();

    @Accessor("blockEntities")
    void memoryfix$setBlockEntities(List blockEntities);

    @Accessor("pendingBlockEntities")
    List memoryfix$getPendingBlockEntities();

    @Accessor("pendingBlockEntities")
    void memoryfix$setPendingBlockEntities(List pendingBlockEntities);

    @Accessor("unloadedBlockEntities")
    List memoryfix$getUnloadedBlockEntities();

    @Accessor("unloadedBlockEntities")
    void memoryfix$setUnloadedBlockEntities(List unloadedBlockEntities);

    @Accessor("playerEntities")
    List memoryfix$getPlayerEntities();

    @Accessor("playerEntities")
    void memoryfix$setPlayerEntities(List playerEntities);

    @Accessor("entities")
    List memoryfix$getEntities();

    @Accessor("entities")
    void memoryfix$setEntities(List entities);

    @Accessor("eventListeners")
    List memoryfix$getEventListeners();

    @Accessor("eventListeners")
    void memoryfix$setEventListeners(List eventListeners);

    @Accessor("chunkProvider")
    ChunkProvider memoryfix$getChunkProvider();

    @Accessor("chunkProvider")
    void memoryfix$setChunkProvider(ChunkProvider chunkProvider);

    @Accessor("persistentStateManager")
    PersistentStateManager memoryfix$getPersistentStateManager();

    @Accessor("persistentStateManager")
    void memoryfix$setPersistentStateManager(PersistentStateManager persistentStateManager);

    @Accessor("scoreboard")
    Scoreboard memoryfix$getScoreboard();

    @Accessor("scoreboard")
    void memoryfix$setScoreboard(Scoreboard scoreboard);

    @Accessor("field_4530")
    Set memoryfix$getActiveChunks();

    @Accessor("field_4530")
    void memoryfix$setActiveChunks(Set activeChunks);

    @Accessor("field_4539")
    ArrayList memoryfix$getCollisionBoxes();

    @Accessor("field_4539")
    void memoryfix$setCollisionBoxes(ArrayList collisionBoxes);
}
