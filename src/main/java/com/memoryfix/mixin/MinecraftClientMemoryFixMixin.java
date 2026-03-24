package com.memoryfix.mixin;

import com.memoryfix.MemoryFixSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ControllablePlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorageAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMemoryFixMixin {

    @Shadow @Nullable public ClientWorld world;
    @Shadow public Screen currentScreen;
    @Shadow public WorldRenderer worldRenderer;
    @Shadow public ParticleManager particleManager;
    @Shadow private IntegratedServer server;
    @Shadow public Entity targetedEntity;
    @Shadow public ControllablePlayerEntity field_3805;
    @Shadow public ClientPlayerInteractionManager interactionManager;
    @Shadow public BlockHitResult result;
    @Shadow private LevelStorageAccess currentSave;
    @Shadow public abstract ClientPlayNetworkHandler getNetworkHandler();
    @Unique @Nullable private IntegratedServer memoryfix$pendingShutdownServer;
    @Unique private boolean memoryfix$disconnectingIntegratedServer;
    @Unique private boolean memoryfix$deferSaveStorageClear;

    @Inject(method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("HEAD"))
    private void memoryfix$clearWorldReferences(@Nullable ClientWorld targetWorld, String loadingMessage, CallbackInfo ci) {
        if (targetWorld != null || this.world == null) {
            return;
        }

        ClientWorld disconnectingWorld = this.world;
        ClientPlayNetworkHandler disconnectingHandler = this.getNetworkHandler();
        ClientPlayerInteractionManager disconnectingInteractionManager = this.interactionManager;
        ControllablePlayerEntity disconnectingPlayer = this.field_3805;

        this.memoryfix$disconnectingIntegratedServer = this.server != null;
        if (this.memoryfix$disconnectingIntegratedServer) {
            this.memoryfix$pendingShutdownServer = this.server;
        }

        if (this.worldRenderer != null) {
            this.worldRenderer.setWorld(null);
        }
        if (this.particleManager != null) {
            this.particleManager.setWorld(null);
        }

        this.targetedEntity = null;
        this.result = null;
        EntityRenderDispatcher.INSTANCE.setWorld(null);
        EntityRenderDispatcher.INSTANCE.field_6482 = null;
        EntityRenderDispatcher.INSTANCE.field_7998 = null;
        BlockEntityRenderDispatcher.INSTANCE.setWorld(null);
        BlockEntityRenderDispatcher.INSTANCE.field_6603 = null;
        MemoryFixSupport.clearClientWorldState(disconnectingWorld);
        MemoryFixSupport.clearClientNetworkHandlerState(disconnectingHandler);
        MemoryFixSupport.clearClientInteractionManagerState(disconnectingInteractionManager);
        if (this.memoryfix$disconnectingIntegratedServer) {
            MemoryFixSupport.clearClientPlayerState(disconnectingPlayer);
        }
    }

    @Redirect(
            method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageAccess;clearAll()V")
    )
    private void memoryfix$deferLevelStorageClear(LevelStorageAccess saveStorage) {
        if (this.memoryfix$disconnectingIntegratedServer) {
            this.memoryfix$deferSaveStorageClear = true;
            return;
        }

        saveStorage.clearAll();
    }

    @Unique
    private boolean memoryfix$completePendingShutdown(boolean waitForStop) {
        IntegratedServer pendingServer = this.memoryfix$pendingShutdownServer;
        if (pendingServer == null) {
            return false;
        }

        while (!((MinecraftServerAccessor) pendingServer).memoryfix$isStopped()) {
            if (!waitForStop) {
                return false;
            }

            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        if (this.memoryfix$deferSaveStorageClear && this.currentSave != null) {
            this.currentSave.clearAll();
        }

        this.memoryfix$deferSaveStorageClear = false;
        this.memoryfix$pendingShutdownServer = null;
        return true;
    }

    @Unique
    private void memoryfix$releaseMemory() {
        MemoryFixSupport.releaseMemory();
    }

    @Inject(
            method = "startIntegratedServer",
            at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V", shift = At.Shift.BEFORE)
    )
    private void memoryfix$waitForPreviousIntegratedServer(String worldName, String levelName, @Nullable LevelInfo levelInfo, CallbackInfo ci) {
        if (this.memoryfix$completePendingShutdown(true)) {
            this.memoryfix$releaseMemory();
        }
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void memoryfix$pollPendingShutdown(CallbackInfo ci) {
        if (this.memoryfix$completePendingShutdown(false)) {
            this.memoryfix$releaseMemory();
        }
    }

    @Inject(method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("TAIL"))
    private void memoryfix$finishDisconnect(@Nullable ClientWorld targetWorld, String loadingMessage, CallbackInfo ci) {
        if (targetWorld == null && this.memoryfix$pendingShutdownServer == null) {
            this.memoryfix$releaseMemory();
        }

        if (targetWorld == null) {
            this.result = null;
        }

        this.memoryfix$disconnectingIntegratedServer = false;
    }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"))
    private void memoryfix$closeTitleBackgroundTexture(@Nullable Screen nextScreen, CallbackInfo ci) {
        if (!(this.currentScreen instanceof TitleScreen)) {
            return;
        }

        TitleScreenAccessor accessor = (TitleScreenAccessor) this.currentScreen;
        if (accessor.memoryfix$getBackgroundTextureId() != null) {
            ((MinecraftClient) (Object) this).getTextureManager().close(accessor.memoryfix$getBackgroundTextureId());
            accessor.memoryfix$setBackgroundTextureId(null);
            accessor.memoryfix$setBackgroundTexture(null);
        }
    }
}
