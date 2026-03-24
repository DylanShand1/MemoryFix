package com.memoryfix.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TitleScreen.class)
public interface TitleScreenAccessor {
    @Accessor("backgroundTextureId")
    Identifier memoryfix$getBackgroundTextureId();

    @Accessor("backgroundTextureId")
    void memoryfix$setBackgroundTextureId(Identifier id);

    @Accessor("backgroundTexture")
    void memoryfix$setBackgroundTexture(NativeImageBackedTexture texture);
}
