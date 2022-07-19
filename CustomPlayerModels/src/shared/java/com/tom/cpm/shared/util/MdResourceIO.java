package com.tom.cpm.shared.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.HTTPIO;

public class MdResourceIO {

	public static CompletableFuture<Image> loadImage0(String url, boolean offline) {
		return MdResourceLoader.fetch(url, offline).thenCompose(i -> {
			try {
				return CompletableFuture.completedFuture(ImageIO.read(new ByteArrayInputStream(i)));
			} catch (IOException e) {
				CompletableFuture<Image> f = new CompletableFuture<>();
				f.completeExceptionally(e);
				return f;
			}
		});
	}

	public static CompletableFuture<byte[]> fetch0(String urlIn) {
		CompletableFuture<byte[]> cf = new CompletableFuture<>();
		ModelDefinitionLoader.THREAD_POOL.execute(() -> {
			try {
				URL url = new URL(urlIn);
				InputStream web = null;
				HttpURLConnection connection = null;
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					connection = HTTPIO.createUrlConnection(url, false);
					web = connection.getInputStream();

					byte[] buffer = new byte[10240];

					int bytesJustDownloaded = 0;
					while((bytesJustDownloaded = web.read(buffer)) > 0) {
						out.write(buffer, 0, bytesJustDownloaded);
					}
					cf.complete(out.toByteArray());
				} finally {
					if(connection != null)connection.disconnect();
					if(web != null)
						try {
							web.close();
						} catch (IOException e) {
						}
				}
			} catch (Throwable e) {
				cf.completeExceptionally(e);
			}
		});
		return cf;
	}
}
