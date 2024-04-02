package com.tom.cpm.client;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.network.MultiplayerClientPlayerEntity;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.event.resource.language.TranslationInvalidationEvent;
import net.modificationstation.stationapi.api.registry.Registry;

import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH;

public class CPMClient {

	@EventListener
	public static void initKeybinds(KeyBindingRegisterEvent evt) {
		KeyBindings.init(evt);
	}

	@EventListener
	public static void registerClientListeners(MessageListenerRegistryEvent ev) {
		CustomPlayerModelsClient.netHandler.registerOut(r -> {
			Registry.register(ev.registry, r, (p, m) -> {
				CustomPlayerModelsClient.netHandler.receiveClient(r, new FastByteArrayInputStream(m.bytes), (NetH) ((MultiplayerClientPlayerEntity) p).networkHandler);
			});
		});
	}

	@EventListener
	public static void reloadLang(TranslationInvalidationEvent ev) {
		Lang.init();
	}
}
