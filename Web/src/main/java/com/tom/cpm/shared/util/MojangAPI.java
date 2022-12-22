package com.tom.cpm.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.web.client.util.ImageIO;

import elemental2.core.Uint8Array;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

public class MojangAPI {

	public MojangAPI() {
	}

	public void uploadSkin(SkinType skinType, Image skin) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.save(skin, baos);
		upload(baos.toByteArray(), DomGlobal.window::open);
	}

	public static Promise<Void> upload(byte[] data, Consumer<String> urlC) {
		RequestInit ri = RequestInit.create();
		ri.setMethod("POST");
		ri.setBody(new Uint8Array(Js.<double[]>uncheckedCast(data)));
		return DomGlobal.fetch(System.getProperty("cpm.webApiEndpoint") + "/skinUp", ri).then(Response::text).then(r -> {
			String url = "https://www.minecraft.net/profile/skin/remote?url=" + System.getProperty("cpm.webApiEndpoint") + "/skin?v=" + r;
			urlC.accept(url);
			return null;
		});
	}

	public static String fromUUID(final UUID value) {
		return value.toString().replace("-", "");
	}

	public String getName() {
		return "Web";
	}
}
