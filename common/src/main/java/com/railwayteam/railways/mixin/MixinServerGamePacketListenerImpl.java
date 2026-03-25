package com.railwayteam.railways.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    @Unique
    private int railways$entityPositionPacketsThisTick = 0;
    @Unique
    private long railways$lastTickPackets = -1;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void railways$limitEntityPackets(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundMoveEntityPacket || packet instanceof ClientboundSetEntityDataPacket) {
            long currentTick = player.level.getGameTime();
            if (railways$lastTickPackets != currentTick) {
                railways$lastTickPackets = currentTick;
                railways$entityPositionPacketsThisTick = 0;
            }

            if (railways$entityPositionPacketsThisTick > 100) {
                ci.cancel();
                return;
            }
            railways$entityPositionPacketsThisTick++;
        }
    }
}
