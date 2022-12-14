package com.tom.cpmsvcc;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent.EntitySound;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent.LocationalSound;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent.StaticSound;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent;

@ForgeVoicechatPlugin
public class CPMVoicechatPlugin implements VoicechatPlugin {

	/**
	 * @return the unique ID for this voice chat plugin
	 */
	@Override
	public String getPluginId() {
		return CPMSVCC.MOD_ID;
	}

	/**
	 * Called when the voice chat initializes the plugin.
	 *
	 * @param api the voice chat API
	 */
	@Override
	public void initialize(VoicechatApi api) {
		CPMSVCC.LOGGER.info("CPM voice chat plugin initialized!");
	}

	/**
	 * Called once by the voice chat to register all events.
	 *
	 * @param registration the event registration
	 */
	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(EntitySound.class, e -> CPMSVCC.handle(e.getId(), e.getRawAudio()));
		registration.registerEvent(LocationalSound.class, e -> CPMSVCC.handle(e.getId(), e.getRawAudio()));
		registration.registerEvent(StaticSound.class, e -> CPMSVCC.handle(e.getId(), e.getRawAudio()));
		registration.registerEvent(ClientSoundEvent.class, e -> CPMSVCC.handle(e.getRawAudio()));
		registration.registerEvent(MicrophoneMuteEvent.class, e -> CPMSVCC.setMuted(e.isDisabled()));
		registration.registerEvent(ClientVoicechatConnectionEvent.class, e -> CPMSVCC.muted.clear());
	}
}
