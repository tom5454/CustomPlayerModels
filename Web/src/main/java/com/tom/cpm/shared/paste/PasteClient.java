package com.tom.cpm.shared.paste;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.LocalizedIOException;
import com.tom.cpm.shared.util.MojangAPI;

public class PasteClient {
	public static final String URL = "https://paste.tom5454.com";
	public static final String URL_CF = "https://cf-paste.tom5454.com";

	public PasteClient(MojangAPI mojang, String url, String fallback) {
	}

	private static <T> CompletableFuture<T> error() {
		CompletableFuture<T> cf = new CompletableFuture<>();
		cf.completeExceptionally(new LocalizedIOException("Unknown error", new FormatText("error.paste.unknown")));
		return cf;
	}

	public CompletableFuture<Void> connect() {
		//WebMC.getInstance().getGui().getFrame().openPopup(new PastePopup(WebMC.getInstance().getGui()));
		return error();
	}

	public CompletableFuture<List<Paste>> listFiles() {
		return error();
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

	public CompletableFuture<String> uploadFile(String name, byte[] content) {
		return error();
	}

	public CompletableFuture<Void> updateFile(String id, byte[] content) {
		return error();
	}

	public CompletableFuture<Void> deleteFile(String id) {
		return error();
	}

	public CompletableFuture<String> createBrowserLoginURL() {
		return error();
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
