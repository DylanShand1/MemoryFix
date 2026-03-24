package com.memoryfix.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.IntBuffer;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("field_1917")
    int memoryfix$getChunkRenderListBase();

    @Accessor("field_1917")
    void memoryfix$setChunkRenderListBase(int baseId);

    @Accessor("field_7969")
    int memoryfix$getStaticEntityRenderList();

    @Accessor("field_7969")
    void memoryfix$setStaticEntityRenderList(int listId);

    @Accessor("field_1920")
    IntBuffer memoryfix$getOcclusionQueryIds();

    @Accessor("field_1920")
    void memoryfix$setOcclusionQueryIds(IntBuffer queryIds);

    @Accessor("field_1921")
    boolean memoryfix$usesOcclusionQueries();
}
