package com.tom.cpm.client;

import dev.tr7zw.firstperson.api.FirstPersonAPI;

public class FirstPersonDetector {
	public static void init() {
		try {
			PlayerProfile.inFirstPerson = FirstPersonAPI::isRenderingPlayer;
		} catch (Throwable e) {
		}
	}
}
