package com.memoryfix.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("field_1648")
    void memoryfix$setField1648(int value);

    @Accessor("field_1649")
    void memoryfix$setField1649(int value);

    @Accessor("field_1650")
    void memoryfix$setField1650(int value);

    @Accessor("selectedStack")
    void memoryfix$setSelectedStack(ItemStack selectedStack);

    @Accessor("currentBreakingProgress")
    void memoryfix$setCurrentBreakingProgress(float currentBreakingProgress);

    @Accessor("blockBreakingSoundCooldown")
    void memoryfix$setBlockBreakingSoundCooldown(float blockBreakingSoundCooldown);

    @Accessor("blockBreakingCooldown")
    void memoryfix$setBlockBreakingCooldown(int blockBreakingCooldown);

    @Accessor("breakingBlock")
    void memoryfix$setBreakingBlock(boolean breakingBlock);

    @Accessor("lastSelectedSlot")
    void memoryfix$setLastSelectedSlot(int lastSelectedSlot);
}
