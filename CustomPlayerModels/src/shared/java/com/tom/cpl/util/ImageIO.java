package com.tom.cpl.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.MinecraftClientAccess;

public class ImageIO {
	private static IImageIO api;

	public static IImageIO getApi() {
		if(api == null)api = MinecraftClientAccess.get().getImageIO();
		return api;
	}

	public static CompletableFuture<Image> read(File f) {
		return getApi().readF(f);
	}

	public static Image read(InputStream f) throws IOException {
		return getApi().read(f);
	}

	public static void write(Image img, File f) throws IOException {
		getApi().write(img, f);
	}

	public static void write(Image img, OutputStream f) throws IOException {
		getApi().write(img, f);
	}

	public static Vec2i getSize(InputStream din) throws IOException {
		return getApi().getSize(din);
	}

	public static interface IImageIO {
		default CompletableFuture<Image> readF(File f) {
			CompletableFuture<Image> cf = new CompletableFuture<>();
			try {
				cf.complete(read(f));
			} catch (IOException e) {
				cf.completeExceptionally(e);
			}
			return cf;
		}

		Image read(File f) throws IOException;
		Image read(InputStream f) throws IOException;
		void write(Image img, File f) throws IOException;
		void write(Image img, OutputStream f) throws IOException;
		Vec2i getSize(InputStream din) throws IOException;
	}
}
