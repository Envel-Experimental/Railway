package com.railwayteam.railways.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Unique
    private static long railways$lastUpdateTick = -1;
    @Unique
    private static int railways$updatesThisTick = 0;
    @Unique
    private static long railways$lastDensityCheckTick = -1;
    @Unique
    private static int railways$lastDensityCount = 0;
    @Unique
    private static final Map<Player, Long> railways$failedInteracts = new WeakHashMap<>();

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void railways$throttleInteraction(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Object self = (Object) this;
        if (!(self instanceof AbstractContraptionEntity entity)) return;
        if (entity.getContraption() == null) return;

        long now_ms = System.currentTimeMillis();
        Long lastFail = railways$failedInteracts.get(player);
        if (lastFail != null && now_ms - lastFail < 500) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        // Restriction 0: OP Only (User request) - Root cause fix to prevent interaction logic spikes
        if (!player.hasPermissions(2)) {
            // Check if looking at a seat
            Vec3 localHit = entity.toLocalVector(player.getEyePosition(), 1.0f);
            BlockPos localPos = new BlockPos(Mth.floor(localHit.x), Mth.floor(localHit.y), Mth.floor(localHit.z));
            if (!entity.getContraption().getSeats().contains(localPos)) {
                railways$failedInteracts.put(player, now_ms);
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
        }

        // Block interaction if moving to prevent "Entity Bomb" source
        if (entity.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            railways$failedInteracts.put(player, now_ms);
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        long currentTick = player.level.getGameTime();
        if (railways$lastUpdateTick != currentTick) {
            railways$lastUpdateTick = currentTick;
            railways$updatesThisTick = 0;
        }

        // Only do expensive density check if we hit the soft limit
        if (railways$updatesThisTick > 5) {
            if (railways$lastDensityCheckTick != currentTick) {
                railways$lastDensityCheckTick = currentTick;
                double radius = 64.0;
                AABB area = entity.getBoundingBox().inflate(radius);
                // Use the level from the entity
                railways$lastDensityCount = entity.level.getEntities((Entity) null, area, e -> true).size();
            }

            if (railways$lastDensityCount > 500) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
        }
        railways$updatesThisTick++;
    }
}
