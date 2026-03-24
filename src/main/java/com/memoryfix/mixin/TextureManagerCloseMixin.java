package com.memoryfix.mixin;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(TextureManager.class)
public abstract class TextureManagerCloseMixin {
    @Shadow private Map textures;
    @Shadow private Map field_6576;
    @Shadow private List tickables;

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void memoryfix$removeClosedTextures(Identifier id, CallbackInfo ci) {
        Texture texture = (Texture) this.textures.remove(id);
        if (texture != null) {
            if (texture instanceof AbstractTexture) {
                ((AbstractTexture) texture).clearGlId();
            } else {
                net.minecraft.client.texture.TextureUtil.deleteTexture(texture.getGlId());
            }

            this.tickables.remove(texture);
            Iterator iterator = this.field_6576.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (id.equals(entry.getValue())) {
                    iterator.remove();
                }
            }
        }

        ci.cancel();
    }
}
