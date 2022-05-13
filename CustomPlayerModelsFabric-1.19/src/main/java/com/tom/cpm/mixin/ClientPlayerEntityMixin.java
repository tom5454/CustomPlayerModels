package com.tom.cpm.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayerEntity;

import com.tom.cpm.client.CustomPlayerModelsClient;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;noClip:Z", opcode = Opcodes.GETFIELD),
			method = "tickMovement()V")
	public void onTickMovement(CallbackInfo cbi) {
		CustomPlayerModelsClient.INSTANCE.updateJump();
	}
}
