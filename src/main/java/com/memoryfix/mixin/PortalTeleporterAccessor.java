package com.memoryfix.mixin;

import net.minecraft.entity.PortalTeleporter;
import net.minecraft.util.collection.LongObjectStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PortalTeleporter.class)
public interface PortalTeleporterAccessor {
    @Accessor("field_5472")
    LongObjectStorage memoryfix$getPortalCache();

    @Accessor("field_5473")
    List memoryfix$getPortalPositions();
}
