package com.tom.cpm.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.LocalPlayer;

import com.tom.cpm.client.ClientProxy;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;noPhysics:Z", opcode = Opcodes.GETFIELD),
			method = "aiStep()V")
	public void onAiStep(CallbackInfo cbi) {
		ClientProxy.INSTANCE.updateJump();
	}
}
