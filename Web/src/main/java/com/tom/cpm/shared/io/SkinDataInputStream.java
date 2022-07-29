package com.tom.cpm.shared.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.tom.cpl.util.Image;

import elemental2.dom.DomGlobal;

public class SkinDataInputStream extends InputStream {
	public static Map<String, String> decodedURL = new HashMap<>();
	public static Map<Image, String> decodedData = new HashMap<>();
	private ByteArrayInputStream in;

	public SkinDataInputStream(Image img, Image template, int channel) {
		String dt = SkinDataInputStream.decodedData.get(img);
		if(dt == null) {
			DomGlobal.console.warn("No decoded data for image");
			in = new ByteArrayInputStream(new byte[0]);
		} else
			in = new ByteArrayInputStream(Base64.getDecoder().decode(dt));
	}

	@Override
	public int read() {
		return in.read();
	}

	public void setChannel(int channel) {
	}

	public int getChannel() {
		return 0;
	}

	@Override
	public void close() {}
}
