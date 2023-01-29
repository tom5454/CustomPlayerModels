package com.tom.cpm.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.cpl.util.Image;
import com.tom.cpl.util.MarkdownParser;
import com.tom.cpl.util.MarkdownRenderer;
import com.tom.cpl.util.MarkdownRenderer.MarkdownResourceLoader;
import com.tom.cpm.shared.io.IOHelper;

public class MdResourceLoader implements MarkdownResourceLoader {
	private static final LoadingCache<String, CompletableFuture<Image>> imageCache = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).build(CacheLoader.from(MdResourceLoader::loadImage0));
	private static final LoadingCache<String, CompletableFuture<String>> pageCache = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).build(CacheLoader.from(MdResourceLoader::loadPage0));
	private boolean offline;
	private Consumer<String> openURL, nextPage;

	public MdResourceLoader(Consumer<String> openURL, Consumer<String> nextPage, boolean offline) {
		this.offline = offline;
		this.openURL = openURL;
		this.nextPage = nextPage;
	}

	private static CompletableFuture<Image> loadImage0(String url) {
		return MdResourceIO.loadImage0(url, false);
	}

	private static CompletableFuture<String> loadPage0(String url) {
		return fetch(url, false).thenApply(pg -> new String(pg, StandardCharsets.UTF_8));
	}

	@Override
	public CompletableFuture<Image> loadImage(String url) {
		if(offline)return MdResourceIO.loadImage0(url, true);
		try {
			return imageCache.get(url);
		} catch (ExecutionException e) {
			CompletableFuture<Image> f = new CompletableFuture<>();
			f.completeExceptionally(e.getCause());
			return f;
		}
	}

	private CompletableFuture<String> loadPage(String url) {
		if(offline)return fetch(url, true).thenApply(pg -> new String(pg, StandardCharsets.UTF_8));
		try {
			return pageCache.get(url);
		} catch (ExecutionException e) {
			CompletableFuture<String> f = new CompletableFuture<>();
			f.completeExceptionally(e.getCause());
			return f;
		}
	}

	@Override
	public void browse(MarkdownRenderer rd, String url) {
		if(url.equals("https://github.com/tom5454/CustomPlayerModels/wiki")) {//Home.md
			browse(rd, "https://github.com/tom5454/CustomPlayerModels/wiki/Home.md");
		} else if(url.startsWith("https://github.com/tom5454/CustomPlayerModels/wiki/")) {
			if(nextPage != null)nextPage.accept(url);
			String nm = url.substring(url.lastIndexOf('/') + 1);
			loadPage(nm.contains("#") ? url.substring(0, url.lastIndexOf('#')) : url).thenAcceptAsync(pg -> {
				rd.setContent(new MarkdownParser(pg));

				if(nm.contains("#")) {
					rd.browse(nm.substring(nm.indexOf('#')));
				}
			}, rd.getGui()::executeLater).exceptionally(e -> {
				rd.getGui().executeLater(() -> {
					rd.setContent(MarkdownParser.makeErrorPage(rd.getGui(), e));
				});
				return null;
			});
		} else if(openURL != null) {
			openURL.accept(url);
		}
	}

	public static CompletableFuture<byte[]> fetch(String url, boolean offline) {
		if(url.equals("https://github.com/tom5454/CustomPlayerModels/wiki")) {//Home.md
			return fetch("https://github.com/tom5454/CustomPlayerModels/wiki/Home.md", offline);
		} else if(url.startsWith("https://github.com/tom5454/CustomPlayerModels/wiki/") && url.endsWith(".md")) {
			if(offline) {
				return asset0("/assets/cpm/wiki/pages/" + url.substring(url.lastIndexOf('/') + 1));
			} else {
				return fetchOnline(url);
			}
		} else if(url.startsWith("https://github.com/tom5454/CustomPlayerModels/wiki/images/")) {
			if(offline) {
				return asset0("/assets/cpm/wiki/images/" + url.substring(url.lastIndexOf('/') + 1));
			} else {
				return fetchOnline("https://github.com/tom5454/CustomPlayerModels/wiki/images/" + url.substring(url.lastIndexOf('/') + 1));
			}
		} else if(url.startsWith("https://github.com/tom5454/CustomPlayerModels/wiki/")) {
			return fetch(url + ".md", offline);
		} else {
			CompletableFuture<byte[]> f = new CompletableFuture<>();
			f.completeExceptionally(new IOException("Unknown url: " + url));
			return f;
		}
	}

	private static CompletableFuture<byte[]> fetchOnline(String url) {
		return MdResourceIO.fetch0(url).handle((d, e) -> {
			if(d != null && e == null)return CompletableFuture.completedFuture(d);
			Log.warn("Failed to load page, loading local backup", e);
			return fetch(url, true).handle((a, e2) -> {
				if(a != null && e2 == null)return CompletableFuture.completedFuture(a);
				CompletableFuture<byte[]> cf = new CompletableFuture<>();
				e2.addSuppressed(e);
				cf.completeExceptionally(e2);
				return cf;
			}).thenCompose(Function.identity());
		}).thenCompose(Function.identity());
	}

	private static CompletableFuture<byte[]> asset0(String path) {
		try(InputStream is = MdResourceLoader.class.getResourceAsStream(path)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOHelper.copy(is, baos);
			return CompletableFuture.completedFuture(baos.toByteArray());
		} catch (IOException e) {
			CompletableFuture<byte[]> f = new CompletableFuture<>();
			f.completeExceptionally(e);
			return f;
		}
	}
}
