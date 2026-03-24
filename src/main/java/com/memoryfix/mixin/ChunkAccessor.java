package com.memoryfix.mixin;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(Chunk.class)
public interface ChunkAccessor {
    @Accessor("chunkSections")
    void memoryfix$setChunkSections(ChunkSection[] chunkSections);

    @Accessor("biomeArray")
    void memoryfix$setBiomeArray(byte[] biomeArray);

    @Accessor("surfaceCache")
    void memoryfix$setSurfaceCache(int[] surfaceCache);

    @Accessor("columnSkyLightOutdated")
    void memoryfix$setColumnSkyLightOutdated(boolean[] columnSkyLightOutdated);

    @Accessor("loaded")
    void memoryfix$setLoaded(boolean loaded);

    @Accessor("world")
    void memoryfix$setWorld(World world);

    @Accessor("heightmap")
    void memoryfix$setHeightmap(int[] heightmap);

    @Accessor("blockEntities")
    void memoryfix$setBlockEntities(Map blockEntities);

    @Accessor("entities")
    void memoryfix$setEntities(List[] entities);

    @Accessor("modified")
    void memoryfix$setModified(boolean modified);

    @Accessor("containsEntities")
    void memoryfix$setContainsEntities(boolean containsEntities);
}
