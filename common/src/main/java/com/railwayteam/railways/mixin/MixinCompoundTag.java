package com.railwayteam.railways.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompoundTag.class)
public abstract class MixinCompoundTag {
    @Unique private int railways$nbtDepth = 0;
    @Unique private long railways$lastMergeStart = 0;
    @Unique private int railways$nbtOpCount = 0;

    @Inject(method = "put", at = @At("HEAD"), cancellable = true)
    private void railways$guardMerge(String key, Tag value, CallbackInfoReturnable<Tag> cir) {
        if (railways$nbtDepth == 0) {
            railways$lastMergeStart = System.currentTimeMillis();
            railways$nbtOpCount = 0;
        }
        railways$nbtDepth++;
        railways$nbtOpCount++;

        // Only check time every 1024 calls to avoid native method overhead
        if (railways$nbtDepth > 256 || (railways$nbtOpCount > 1024 && System.currentTimeMillis() - railways$lastMergeStart > 10)) {
            cir.setReturnValue(value);
            return;
        }
    }

    @Inject(method = "put", at = @At("RETURN"))
    private void railways$endGuardMerge(String key, Tag value, CallbackInfoReturnable<Tag> cir) {
        if (railways$nbtDepth > 0) railways$nbtDepth--;
    }

    @Inject(method = "merge", at = @At("HEAD"))
    private void railways$startMerge(CompoundTag other, CallbackInfoReturnable<CompoundTag> cir) {
        if (railways$nbtDepth == 0) {
            railways$lastMergeStart = System.currentTimeMillis();
            railways$nbtOpCount = 0;
        }
    }
}
