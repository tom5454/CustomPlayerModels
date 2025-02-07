package com.tom.cpm.client;

public class RealCameraDetector {
	public static boolean realCameraRendering;

	public static void init() {
		PlayerProfile.addInFirstPerson(() -> realCameraRendering);
	}
}
