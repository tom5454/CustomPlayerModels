package com.tom.cpm.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.cpl.util.Image;
import com.tom.cpl.util.MarkdownParser;
import com.tom.cpl.util.MarkdownRenderer;
import com.tom.cpl.util.MarkdownRenderer.MarkdownResourceLoader;
import com.tom.cpm.shared.io.IOHelper;

public class MdResourceLoader implements MarkdownResourceLoader {
	public static final String RAW_WIKI_ROOT = "https://raw.githubusercontent.com/wiki/tom5454/CustomPlayerModels/";
	public static final String RAW_IMG_ROOT = RAW_WIKI_ROOT + "images/";
	private static final String WIKI_ROOT = "https://github.com/tom5454/CustomPlayerModels/wiki";
	private static final String IMAGES_ROOT = WIKI_ROOT + "/images/";
	private static final String LOCALE_ROOT = WIKI_ROOT + "/locale/";
	public static final Pattern LOCALE_EXT = Pattern.compile("[\\w-/.:]+\\/([\\w-]+)-([a-z]{2,3}-[A-Z]{2,3})");

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
		return fetch(url, false, true).thenApply(pg -> new String(pg, StandardCharsets.UTF_8));
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
		if(offline)return fetch(url, true, true).thenApply(pg -> new String(pg, StandardCharsets.UTF_8));
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
		if(url.equals(WIKI_ROOT)) {//Home.md
			browse(rd, "https://github.com/tom5454/CustomPlayerModels/wiki/Home.md");
		} else if(url.startsWith(WIKI_ROOT)) {
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

	public static CompletableFuture<byte[]> fetch(String url, boolean offline, boolean logWarn) {
		Log.debug("Wiki fetching: " + url);
		if(url.equals(WIKI_ROOT)) {//Home.md
			return fetch("https://github.com/tom5454/CustomPlayerModels/wiki/Home.md", offline, logWarn);
		}
		if(url.startsWith(WIKI_ROOT) && url.endsWith(".md")) {
			String p = url.substring(WIKI_ROOT.length() + 1);
			if(offline) {
				return asset0("/assets/cpm/wiki/pages/" + p);
			} else {
				return fetchOnline(RAW_WIKI_ROOT + p, "/assets/cpm/wiki/pages/" + p, logWarn);
			}
		} else if(url.startsWith(IMAGES_ROOT)) {
			String p = url.substring(IMAGES_ROOT.length());
			if(offline) {
				return asset0("/assets/cpm/wiki/images/" + p);
			} else {
				return fetchOnline(RAW_IMG_ROOT + p, "/assets/cpm/wiki/images/" + p, logWarn);
			}
		} else if(url.startsWith(WIKI_ROOT)) {
			Matcher m = LOCALE_EXT.matcher(url);
			if (m.matches()) {
				CompletableFuture<byte[]> cf;
				String b = m.group(1);
				String lang = m.group(2);
				if (b.equals("_Sidebar"))
					cf = fetch(LOCALE_ROOT + lang + "/_Sidebar.md", offline, false);
				else
					cf = fetch(LOCALE_ROOT + lang + "/" + b + "-" + lang + ".md", offline, false);
				return cf.exceptionally(ex -> null).thenCompose(d -> {
					if (d == null)return fetch(WIKI_ROOT + "/" + b + ".md", offline, logWarn);
					else return CompletableFuture.completedFuture(d);
				});
			}
			return fetch(url + ".md", offline, logWarn);
		} else {
			CompletableFuture<byte[]> f = new CompletableFuture<>();
			f.completeExceptionally(new IOException("Unknown url: " + url));
			return f;
		}
	}

	private static CompletableFuture<byte[]> fetchOnline(String url, String asset, boolean logWarn) {
		return MdResourceIO.fetch0(url).handle((d, e) -> {
			if(d != null && e == null)return CompletableFuture.completedFuture(d);
			if(logWarn)Log.warn("Failed to load page, loading local backup", e);
			return asset0(asset).handle((a, e2) -> {
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
			if (is == null)throw new FileNotFoundException(path);
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
