package com.memoryfix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("world")
    void memoryfix$setWorld(World world);

    @Accessor("vehicle")
    void memoryfix$setVehicle(Entity vehicle);

    @Accessor("rider")
    void memoryfix$setRider(Entity rider);
}
