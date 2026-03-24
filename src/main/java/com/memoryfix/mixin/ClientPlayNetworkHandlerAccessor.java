package com.memoryfix.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor("world")
    void memoryfix$setWorld(ClientWorld world);

    @Accessor("positionLookSetup")
    void memoryfix$setPositionLookSetup(boolean positionLookSetup);

    @Accessor("stateManager")
    PersistentStateManager memoryfix$getStateManager();

    @Accessor("playerListEntries")
    Map memoryfix$getPlayerListEntries();

    @Accessor("field_7911")
    List memoryfix$getTabEntries();

    @Accessor("field_7920")
    void memoryfix$setInventoryAchievementHintShown(boolean value);
}
