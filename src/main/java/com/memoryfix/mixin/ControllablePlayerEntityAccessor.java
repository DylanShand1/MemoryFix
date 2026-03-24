package com.memoryfix.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.ControllablePlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ControllablePlayerEntity.class)
public interface ControllablePlayerEntityAccessor {
    @Mutable
    @Accessor("field_1667")
    void memoryfix$setNetworkHandler(ClientPlayNetworkHandler networkHandler);
}
