package com.tom.cpm.mixin;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec;

import com.tom.cpm.common.NetworkInit;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin {

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;make(Ljava/lang/Object;Ljava/util/function/Consumer;)Ljava/lang/Object;"), method = "<clinit>", index = 1)
	private static Consumer implementProperNetworking(Consumer listInit) {
		return l -> {
			listInit.accept(l);
			List<CustomPacketPayload.TypeAndCodec> lst = (List<TypeAndCodec>) l;
			NetworkInit.registerToClient((t, c) -> lst.add(new CustomPacketPayload.TypeAndCodec(t, c)));
		};
	}
}
