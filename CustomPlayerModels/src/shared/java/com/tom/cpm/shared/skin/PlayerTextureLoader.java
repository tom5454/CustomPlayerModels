package com.tom.cpm.shared.skin;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.util.LegacySkinConverter;

public abstract class PlayerTextureLoader {
	private static final LoadingCache<String, CompletableFuture<Image>> cache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.HOURS).build(CacheLoader.from(Image::download));
	private Map<TextureType, Texture> textures = new ConcurrentHashMap<>();
	private final File skinsDirectory;

	private static class Texture {
		private final TextureType type;
		private CompletableFuture<Image> skinFuture;
		private String url;
		private File cachedFile;
		private UnaryOperator<Image> postProcessor;

		public Texture(TextureType type) {
			this.postProcessor = type == TextureType.SKIN ? LegacySkinConverter::processLegacySkin : UnaryOperator.identity();
			this.type = type;
		}

		public CompletableFuture<Image> get() {
			if(skinFuture != null)return skinFuture;
			skinFuture = get0();
			if(skinFuture == null)return CompletableFuture.completedFuture(null);
			return skinFuture;
		}

		private CompletableFuture<Image> get0() {
			if(MinecraftObjectHolder.DEBUGGING && new File(type.name().toLowerCase(Locale.ROOT) + "_test.png").exists()) {
				return Image.loadFrom(new File(type.name().toLowerCase(Locale.ROOT) + "_test.png")).thenApply(postProcessor);
			}
			if(url == null)return null;
			if(cachedFile != null && cachedFile.isFile())
				return Image.loadFrom(cachedFile).thenApply(postProcessor).exceptionally(e -> null);
			return cache.getUnchecked(url).thenApply(postProcessor).exceptionally(e -> null);
		}
	}

	private CompletableFuture<Void> loadFuture;

	public CompletableFuture<Void> load() {
		if(loadFuture == null) {
			loadFuture = load0();
			/*if(MinecraftObjectHolder.DEBUGGING)
				loadFuture.complete(null);*/
		}
		return loadFuture;
	}

	public PlayerTextureLoader() {
		this.skinsDirectory = null;
	}

	public PlayerTextureLoader(File skinsDirectory) {
		this.skinsDirectory = skinsDirectory;
	}

	protected abstract CompletableFuture<Void> load0();

	protected void defineTexture(TextureType type, String url) {
		textures.computeIfAbsent(type, Texture::new).url = url;
	}

	protected void defineTexture(TextureType type, String url, String hash) {
		Texture tx = textures.computeIfAbsent(type, Texture::new);
		tx.url = url;
		if(skinsDirectory != null) {
			String s = Hashing.sha1().hashUnencodedChars(hash).toString();
			File file1 = new File(this.skinsDirectory, s.length() > 2 ? s.substring(0, 2) : "xx");
			tx.cachedFile = new File(file1, s);
		}
	}

	protected void defineTexture(TextureType type, String url, File file) {
		Texture tx = textures.computeIfAbsent(type, Texture::new);
		tx.url = url;
		tx.cachedFile = file;
	}

	protected <Ty extends Enum<Ty>> void defineTexture(Ty type, String url) {
		defineTexture(TextureType.valueOf(type.name()), url);
	}

	protected <Ty extends Enum<Ty>> void defineTexture(Ty type, String url, String hash) {
		defineTexture(TextureType.valueOf(type.name()), url, hash);
	}

	protected <Ty extends Enum<Ty>, Tx> void defineAll(Map<Ty, Tx> map, Function<Tx, String> toURL) {
		for(Entry<Ty, Tx> e : map.entrySet()) {
			defineTexture(TextureType.valueOf(e.getKey().name()), toURL.apply(e.getValue()));
		}
	}

	protected <Ty extends Enum<Ty>, Tx> void defineAll(Map<Ty, Tx> map, Function<Tx, String> toURL, Function<Tx, String> toHash) {
		for(Entry<Ty, Tx> e : map.entrySet()) {
			defineTexture(TextureType.valueOf(e.getKey().name()), toURL.apply(e.getValue()), toHash.apply(e.getValue()));
		}
	}

	public CompletableFuture<Image> getTexture(TextureType type) {
		return textures.computeIfAbsent(type, Texture::new).get();
	}

	public boolean hasTexture(TextureType type) {
		Texture t = textures.get(type);
		if (t == null)return false;
		return t.url != null;
	}
}
