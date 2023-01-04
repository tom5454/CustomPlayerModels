package com.tom.cpm.shared.paste;

import java.io.IOException;
import java.util.List;

import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.LocalizedIOException;
import com.tom.cpm.shared.util.MojangAPI;

public class PasteClient {
	public static final String URL = "https://paste.tom5454.com";
	public static final String URL_CF = "https://cf-paste.tom5454.com";

	public PasteClient(MojangAPI mojang, String url, String fallback) {
	}

	public void connect() throws IOException {
		//WebMC.getInstance().getGui().getFrame().openPopup(new PastePopup(WebMC.getInstance().getGui()));
		throw new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown"));
	}

	public List<Paste> listFiles() throws IOException {
		throw new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown"));
	}

	public static class Paste {
		public final String id, name;
		public final long time;

		public Paste(String id, String name, long time) {
			this.id = id;
			this.name = name;
			this.time = time;
		}
	}

	public String uploadFile(String name, byte[] content) throws IOException {
		throw new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown"));
	}

	public void updateFile(String id, byte[] content) throws IOException {
		throw new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown"));
	}

	public void deleteFile(String id) throws IOException {
		throw new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown"));
	}

	public String createBrowserLoginURL() throws IOException {
		throw new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown"));
	}

	public boolean isConnected() {
		return false;
	}

	public int getMaxPastes() {
		return 0;
	}

	public int getMaxSize() {
		return 0;
	}

	public String getUrl() {
		return URL;
	}
}
