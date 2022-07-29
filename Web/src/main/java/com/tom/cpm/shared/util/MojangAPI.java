package com.tom.cpm.shared.util;

import java.io.IOException;
import java.util.UUID;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.model.SkinType;

public class MojangAPI {

	public MojangAPI() {
	}

	public void uploadSkin(SkinType skinType, Image skin) throws IOException {
		throw new IOException("Missing auth info");
	}

	public void joinServer(String serverId) throws IOException {
		throw new IOException("Missing auth info");
	}

	public static String fromUUID(final UUID value) {
		return value.toString().replace("-", "");
	}

	public static void clearYggdrasilCache(Object yss) {}

	public String getName() {
		return "Web";
	}
}
