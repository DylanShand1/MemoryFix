package com.memoryfix.mixin;

import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(PersistentStateManager.class)
public interface PersistentStateManagerAccessor {
    @Accessor("stateMap")
    Map memoryfix$getStateMap();

    @Accessor("states")
    List memoryfix$getStates();

    @Accessor("idCounts")
    Map memoryfix$getIdCounts();
}
