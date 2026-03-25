package com.railwayteam.railways.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.FenceGateMovingInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(value = FenceGateMovingInteraction.class, remap = false)
public class MixinFenceGateMovingInteraction {
    @Unique
    private static final Map<Player, Long> railways$lastInteraction = new WeakHashMap<>();

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void railways$throttleInteraction(Player player, Contraption contraption, BlockPos pos,
                                              BlockState currentState, CallbackInfoReturnable<BlockState> cir) {
        if (player == null) return;

        // Restriction 0: OP Only
        if (!player.hasPermissions(2)) {
            cir.setReturnValue(currentState);
            return;
        }

        // Restriction 1: No interacting while moving
        if (contraption.entity != null && contraption.entity.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            cir.setReturnValue(currentState);
            return;
        }

        // Restriction 2: 5-second cooldown
        long now = System.currentTimeMillis();
        long last = railways$lastInteraction.getOrDefault(player, 0L);
        if (now - last < 5000) {
            cir.setReturnValue(currentState);
            return;
        }
        railways$lastInteraction.put(player, now);
    }
}
