package com.railwayteam.railways.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class MixinBasinBlockEntity extends BlockEntity {
    public MixinBasinBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    private Level railways$lastLevel;

    @Inject(method = "setLevel", at = @At("HEAD"), cancellable = true, remap = true)
    private void railways$skipRedundantSetLevel(Level level, CallbackInfo ci) {
        // Create's contraption rendering calls setLevel every frame. 
        // We skip re-initialization if the level is the same (even if it's a WrappedLevel).
        if (this.level == level || (railways$lastLevel != null && railways$lastLevel == level)) {
            ci.cancel();
            return;
        }
        railways$lastLevel = level;
    }
}
