package com.memoryfix.mixin;

import net.minecraft.server.PlayerWorldManager;
import net.minecraft.util.collection.LongObjectStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerWorldManager.class)
public interface PlayerWorldManagerAccessor {
    @Accessor("players")
    List memoryfix$getPlayers();

    @Accessor("playerInstancesById")
    LongObjectStorage memoryfix$getPlayerInstancesById();

    @Accessor("field_2792")
    List memoryfix$getPendingPlayerInstances();

    @Accessor("playerInstances")
    List memoryfix$getPlayerInstances();
}
