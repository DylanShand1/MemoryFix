package com.memoryfix.mixin;

import com.memoryfix.MemoryFixSupport;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMemoryFixMixin {

    @Inject(method = "setWorld", at = @At("TAIL"))
    private void memoryfix$releaseOldRenderChunks(ClientWorld world, CallbackInfo ci) {
        if (world != null) {
            return;
        }

        MemoryFixSupport.clearWorldRendererState(this);
    }
}
