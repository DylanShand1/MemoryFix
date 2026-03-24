package com.memoryfix.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.LongObjectStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ServerChunkProvider;
import net.minecraft.world.chunk.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Set;

@Mixin(ServerChunkProvider.class)
public interface ServerChunkProviderAccessor {
    @Accessor("chunksToUnload")
    Set memoryfix$getChunksToUnload();

    @Accessor("empty")
    Chunk memoryfix$getEmptyChunk();

    @Accessor("empty")
    void memoryfix$setEmptyChunk(Chunk emptyChunk);

    @Accessor("chunkGenerator")
    void memoryfix$setChunkGenerator(ChunkProvider chunkGenerator);

    @Accessor("chunkWriter")
    void memoryfix$setChunkWriter(ChunkStorage chunkWriter);

    @Accessor("chunkStorage")
    LongObjectStorage memoryfix$getChunkStorage();

    @Accessor("chunks")
    List memoryfix$getChunks();

    @Accessor("chunks")
    void memoryfix$setChunks(List chunks);

    @Accessor("world")
    void memoryfix$setWorld(ServerWorld world);
}
