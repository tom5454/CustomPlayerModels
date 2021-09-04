package com.tom.cpm.shared.skin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.util.LegacySkinConverter;

public abstract class PlayerTextureLoader {
	private Map<TextureType, Texture> textures = new HashMap<>();

	private static class Texture {
		private CompletableFuture<Image> skinFuture;
		private String url;
		private UnaryOperator<Image> postProcessor;

		public Texture(TextureType type) {
			this.postProcessor = type == TextureType.SKIN ? LegacySkinConverter::processLegacySkin : UnaryOperator.identity();
		}

		public CompletableFuture<Image> get() {
			if(skinFuture != null)return skinFuture;
			skinFuture = get0();
			if(skinFuture == null)return CompletableFuture.completedFuture(null);
			return skinFuture;
		}

		private CompletableFuture<Image> get0() {
			if(MinecraftObjectHolder.DEBUGGING && new File("skin_test.png").exists()) {
				return CompletableFuture.supplyAsync(() -> {
					try {
						return Image.loadFrom(new File("skin_test.png"));
					} catch (IOException e) {
						return null;
					}
				});
			}
			if(url == null)return null;
			return Image.download(url).thenApply(postProcessor).exceptionally(e -> null);
		}
	}

	private CompletableFuture<Void> loadFuture;


	public CompletableFuture<Void> load() {
		if(loadFuture == null) {
			loadFuture = load0();
			if(MinecraftObjectHolder.DEBUGGING)
				loadFuture.complete(null);
		}
		return loadFuture;
	}

	protected abstract CompletableFuture<Void> load0();

	protected void defineTexture(TextureType type, String url) {
		textures.computeIfAbsent(type, Texture::new).url = url;
	}

	protected <Ty extends Enum<Ty>> void defineTexture(Ty type, String url) {
		defineTexture(TextureType.valueOf(type.name()), url);
	}

	protected <Ty extends Enum<Ty>, Tx> void defineAll(Map<Ty, Tx> map, Function<Tx, String> toURL) {
		for(Entry<Ty, Tx> e : map.entrySet()) {
			defineTexture(TextureType.valueOf(e.getKey().name()), toURL.apply(e.getValue()));
		}
	}

	public CompletableFuture<Image> getTexture(TextureType type) {
		return textures.computeIfAbsent(type, Texture::new).get();
	}
}
