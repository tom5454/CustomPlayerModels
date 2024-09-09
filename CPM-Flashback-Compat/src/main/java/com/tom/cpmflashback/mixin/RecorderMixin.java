package com.tom.cpmflashback.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.moulberry.flashback.record.Recorder;

import com.tom.cpmflashback.CPMPacketInjector;

@Mixin(Recorder.class)
public class RecorderMixin {

	//Custom packets are lost when sent inside the first snapshot as the replay viewer list is empty inside ReplayServer when processed
	/*@Inject(at = @At(value = "INVOKE", desc = @Desc(owner = AsyncReplaySaver.class, value = "writeGamePackets", args = {StreamCodec.class, List.class}), ordinal = 0, shift = Shift.BEFORE), method = "writeSnapshot(Z)V", remap = false)
	private void cpmflashback$onRecordStart(CallbackInfo cbi, @Local(ordinal = 1) List<Packet<? super ClientGamePacketListener>> gamePackets) {
		CPMPacketInjector.injectStartPackets(gamePackets);
	}*/

	@Inject(at = @At(value = "RETURN"), method = "writeSnapshot(Z)V", remap = false)
	private void cpmflashback$onRecordStart(CallbackInfo cbi) {
		CPMPacketInjector.injectStartPackets();
	}
}
