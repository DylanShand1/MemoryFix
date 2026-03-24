package com.memoryfix.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ServerNetworkIo.class)
public interface ServerNetworkIoAccessor {
    @Accessor("channels")
    List memoryfix$getChannels();

    @Accessor("connections")
    List memoryfix$getConnections();

    @Mutable
    @Accessor("server")
    void memoryfix$setServer(MinecraftServer server);
}
