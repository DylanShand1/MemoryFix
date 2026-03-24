package com.memoryfix.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.class_739")
public abstract class ServerThreadMemoryFixMixin {

    @Mutable
    @Shadow
    @Final
    private MinecraftServer field_2703;

    @Inject(method = "run", at = @At("TAIL"))
    private void memoryfix$clearServerReferenceAfterStop(CallbackInfo ci) {
        this.field_2703 = null;
    }
}
