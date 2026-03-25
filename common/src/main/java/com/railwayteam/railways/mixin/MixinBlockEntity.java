package com.railwayteam.railways.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity {
    @Shadow @Nullable protected Level level;

    @Inject(method = "setLevel", at = @At("HEAD"), cancellable = true)
    private void railways$skipRedundantSetLevel(Level level, CallbackInfo ci) {
        if (this.level == level) {
            ci.cancel();
        }
    }
}
