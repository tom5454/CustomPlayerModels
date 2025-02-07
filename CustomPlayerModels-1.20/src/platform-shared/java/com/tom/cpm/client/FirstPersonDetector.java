package com.tom.cpm.client;

import dev.tr7zw.firstperson.api.FirstPersonAPI;

public class FirstPersonDetector {

	public static void init() {
		try {
			PlayerProfile.addInFirstPerson(FirstPersonAPI::isRenderingPlayer);
		} catch (Throwable e) {
		}
	}

}
