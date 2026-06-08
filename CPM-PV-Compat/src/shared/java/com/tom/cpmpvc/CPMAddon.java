package com.tom.cpmpvc;

import java.util.Optional;
import java.util.UUID;

import com.tom.cpm.shared.MinecraftClientAccess;

import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.AddonLoaderScope;
import su.plo.voice.api.addon.ClientAddonsLoader;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureProcessedEvent;
import su.plo.voice.api.client.event.audio.source.AudioSourceWriteEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.source.ClientPlayerSource;
import su.plo.voice.proto.data.player.VoicePlayerInfo;

@Addon(id = CPMPVC.MOD_ID, scope = AddonLoaderScope.CLIENT, version = "2.1.2", authors = "tom5454")
public class CPMAddon implements AddonInitializer {
	public static final CPMAddon INSTANCE = new CPMAddon();

	private CPMAddon() {
	}

	@InjectPlasmoVoice
	private PlasmoVoiceClient voiceClient;

	@Override
	public void onAddonInitialize() {
		CPMPVC.LOGGER.info("CPM Plasmo Voice addon initialized!");
	}

	public static void init() {
		ClientAddonsLoader.INSTANCE.load(INSTANCE);
	}

	@EventSubscribe
	public void onCaptureProcessed(AudioCaptureProcessedEvent event) {
		// AudioCaptureProcessedEvent triggers even when audio is not actually sent to the server
		// so we have to check if any activation is active right now to see if player is speaking
		boolean isSpeaking = voiceClient.getActivationManager()
				.getActivations()
				.stream().anyMatch(ClientActivation::isActive);

		CPMPVC.handle(isSpeaking ? event.getProcessed().getMono() : null);
	}

	@EventSubscribe
	public void onAudioWriteEvent(AudioSourceWriteEvent event) {
		if (event.getSource() instanceof ClientPlayerSource) {
			UUID uuid = ((ClientPlayerSource) event.getSource()).getSourceInfo().getPlayerInfo().getPlayerId();
			CPMPVC.handle(uuid, event.getSamples());
		}
	}

	public boolean isMuted(UUID player) {
		Optional<ServerConnection> connection = voiceClient.getServerConnection();
		if (!connection.isPresent()) return false;

		Optional<VoicePlayerInfo> playerInfo = connection.get().getPlayerById(player);
		if (!playerInfo.isPresent()) return false;

		boolean isMutedOnServer = playerInfo.get().isMuted();
		boolean inMutedOnClient = voiceClient.getConfig()
				.getVoice()
				.getVolumes()
				.getMute("source_" + player)
				.value();

		if (isMutedOnServer || inMutedOnClient) return true;

		if (MinecraftClientAccess.get().getCurrentClientPlayer().getUUID().equals(player)) {
			return voiceClient.getConfig().getVoice().getMicrophoneDisabled().value();
		}

		return false;
	}
}
