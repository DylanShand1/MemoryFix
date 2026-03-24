package com.memoryfix.mixin;

import com.memoryfix.MemoryFixSupport;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldCloseMemoryFixMixin {

    @Inject(method = "close", at = @At("RETURN"))
    private void memoryfix$clearClosedWorldState(CallbackInfo ci) {
        MemoryFixSupport.clearServerWorldState(this);
    }
}
