package com.memoryfix.mixin;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ClientChunkProvider;
import net.minecraft.util.collection.LongObjectStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientChunkProvider.class)
public interface ClientChunkProviderAccessor {
    @Accessor("emptyChunk")
    Chunk memoryfix$getEmptyChunk();

    @Accessor("emptyChunk")
    void memoryfix$setEmptyChunk(Chunk emptyChunk);

    @Accessor("chunkStorage")
    LongObjectStorage memoryfix$getChunkStorage();

    @Accessor("chunks")
    List memoryfix$getChunks();

    @Accessor("chunks")
    void memoryfix$setChunks(List chunks);

    @Accessor("world")
    void memoryfix$setWorld(World world);
}
