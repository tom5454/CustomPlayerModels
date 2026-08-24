package com.tom.cpmflashback.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import com.moulberry.flashback.record.Recorder;

import com.tom.cpmflashback.CPMPacketInjector;

@Mixin(Recorder.class)
public class RecorderMixin {

	@Inject(at = @At(value = "RETURN"), method = "writeCustomSnapshot(Ljava/util/function/Consumer;)V", remap = false)
	private void cpmflashback$onRecordStart(Consumer<Packet<? super ClientGamePacketListener>> packetConsumer, CallbackInfo cbi) {
		CPMPacketInjector.injectStartPackets();
	}
}
