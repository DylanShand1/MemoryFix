package com.memoryfix.mixin;

import com.memoryfix.MemoryFixSupport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMemoryFixMixin {

    @Shadow private static MinecraftServer instance;
    @Shadow public ServerWorld[] worlds;
    @Shadow public long[][] field_3858;
    @Shadow private PlayerManager playerManager;
    @Shadow private List tickables;
    @Shadow private String serverOperation;
    @Shadow @Final private ServerNetworkIo networkIo;

    @Inject(method = "stopServer", at = @At("RETURN"))
    private void memoryfix$releaseStoppedServerRoots(CallbackInfo ci) {
        this.tickables.clear();
        this.playerManager = null;
        this.serverOperation = null;

        if (this.worlds != null) {
            Arrays.fill(this.worlds, null);
            this.worlds = null;
        }

        this.field_3858 = null;
        MemoryFixSupport.clearServerNetworkIo(this.networkIo);

        if (instance == (MinecraftServer) (Object) this) {
            instance = null;
        }
    }
}
