package com.memoryfix.mixin;

import net.minecraft.util.FileIoThread;
import net.minecraft.world.chunk.RegionIo;
import net.minecraft.world.level.storage.AnvilLevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AnvilLevelStorage.class)
public abstract class AnvilLevelStorageMixin {

    /**
     * Prevent client-side save storage from clearing shared region file handles
     * while the async file I/O thread still has pending chunk writes.
     */
    @Overwrite
    public void clearAll() {
        FileIoThread.INSTANCE.waitUntilComplete();
        RegionIo.clearRegionFormats();
    }
}
