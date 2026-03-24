package com.memoryfix.mixin;

import net.minecraft.block.RedstoneTorchBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(RedstoneTorchBlock.class)
public abstract class RedstoneTorchBlockMemoryFixMixin {

    @Shadow private static Map turnOffEntries;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void memoryfix$useWeakTurnOffEntries(CallbackInfo ci) {
        turnOffEntries = new WeakHashMap(turnOffEntries);
    }
}
