package com.tom.cpmpvc.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tom.cpmpvc.CPMPVC;

import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.client.audio.source.BaseClientAudioSource;
import su.plo.voice.client.audio.source.ClientPlayerSource;

@Mixin(BaseClientAudioSource.class)
public class BaseClientAudioSourceMixin {

	@Inject(at = @At("HEAD"), method = "write", remap = false)
	private void onWrite(short[] data, CallbackInfo cbi) {
		if ((ClientAudioSource<?>) this instanceof ClientPlayerSource s) {
			UUID uuid = s.getSourceInfo().getPlayerInfo().getPlayerId();
			CPMPVC.handle(uuid, data);
		}
	}
}
