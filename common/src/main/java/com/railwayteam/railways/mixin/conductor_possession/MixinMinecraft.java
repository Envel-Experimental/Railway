/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow public HitResult hitResult;
    @Unique private static long railways$lastClientInteract = 0;

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void railways$throttleClientInteracts(CallbackInfo ci) {
        long now = System.currentTimeMillis();
        // Global throttle for interaction spam (60ms = ~1.2 ticks) to prevent "Hold RMB" lag
        if (now - railways$lastClientInteract < 60) {
            ci.cancel();
            return;
        }
        railways$lastClientInteract = now;
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void railways$handleStart(CallbackInfo ci) {
        ConductorPossessionController.onHandleKeybinds((Minecraft) (Object) this, true);
    }

    @Inject(method = "handleKeybinds", at = @At("RETURN"))
    private void railways$handleEnd(CallbackInfo ci) {
        ConductorPossessionController.onHandleKeybinds((Minecraft) (Object) this, false);
    }
}
