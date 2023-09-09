package com.tom.cpm.client;

import dev.tr7zw.firstperson.FirstPersonModelCore;

public class FirstPersonDetector {

	public static void init() {
		try {
			PlayerProfile.inFirstPerson = () -> FirstPersonModelCore.isRenderingPlayer;
		} catch (Throwable e) {
		}
	}

}
