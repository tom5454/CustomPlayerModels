package com.tom.cpm.shared.definition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.ResourceLoader;
import com.tom.cpm.shared.config.ResourceLoader.ResourceEncoding;
import com.tom.cpm.shared.definition.Link.ResolvedLink;
import com.tom.cpm.shared.io.ChecksumInputStream;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.SkinDataInputStream;
import com.tom.cpm.shared.loaders.GistResourceLoader;
import com.tom.cpm.shared.loaders.GithubRepoResourceLoader;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartEnd;
import com.tom.cpm.shared.parts.ModelPartSkinType;
import com.tom.cpm.shared.parts.ModelPartType;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;

public class ModelDefinitionLoader<GP> {
	public static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
	private Function<GP, Player<?, ?>> playerFactory;
	private Function<GP, UUID> getUUID;
	private final LoadingCache<Key, Player<?, ?>> cache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<Key, Player<?, ?>>() {

		@Override
		public Player<?, ?> load(Key key) throws Exception {
			Player<?, ?> player = playerFactory.apply(key.profile);
			if(serverModels.containsKey(key)) {
				player.setModelDefinition(CompletableFuture.supplyAsync(() -> loadModel(serverModels.get(key), player), THREAD_POOL));
			} else {
				player.setModelDefinition(player.getTextures().load().thenCompose(v -> player.getTextures().getTexture(TextureType.SKIN)).thenApply(skin -> {
					if(skin != null && player.getModelDefinition() == null) {
						return loadModel(skin, player);
					} else {
						return null;
					}
				}));
			}
			return player;
		}
	});
	private static final Map<String, ResourceLoader> LOADERS = new HashMap<>();
	private final Cache<Link, ResolvedLink> linkCache = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).build();
	private final Cache<Link, ResolvedLink> localCache = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).build();
	private ConcurrentHashMap<Key, byte[]> serverModels = new ConcurrentHashMap<>();
	static {
		LOADERS.put("git", new GistResourceLoader());
		LOADERS.put("gh", new GithubRepoResourceLoader());
	}
	private Image template;
	public static final int HEADER = 0x53;

	public ModelDefinitionLoader(Function<GP, Player<?, ?>> playerFactory, Function<GP, UUID> getUUID) {
		try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			this.template = Image.loadFrom(is);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		this.playerFactory = playerFactory;
		this.getUUID = getUUID;
	}

	public Player<?, ?> loadPlayer(GP player) {
		try {
			return cache.get(new Key(player));
		} catch (ExecutionException | UncheckedExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ModelDefinition loadModel(byte[] data, Player<?, ?> player) {
		try(ByteArrayInputStream in = new ByteArrayInputStream(data)) {
			return loadModel(in, player);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ModelDefinition loadModel(Image skin, Player<?, ?> player) {
		try(SkinDataInputStream in = new SkinDataInputStream(skin, template, player.getSkinType().getChannel())) {
			return loadModel(in, player);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private ModelDefinition loadModel(InputStream in, Player<?, ?> player) throws IOException {
		if(in.read() != HEADER)return null;
		ChecksumInputStream cis = new ChecksumInputStream(in);
		IOHelper din = new IOHelper(cis);
		List<IModelPart> parts = new ArrayList<>();
		while(true) {
			IModelPart part = din.readObjectBlock(ModelPartType.VALUES, (t, d) -> t.getFactory().create(d, this));
			if(part == null)continue;
			if(part instanceof ModelPartSkinType && in instanceof SkinDataInputStream) {
				SkinDataInputStream sin = (SkinDataInputStream) in;
				SkinType type = ((ModelPartSkinType)part).getSkinType();
				if(type != SkinType.UNKNOWN && type.getChannel() != sin.getChannel()) {
					sin.setChannel(type.getChannel());
					Log.debug("Mismatching skin type");
				}
			}
			if(part instanceof ModelPartEnd) {
				cis.checkSum();
				break;
			}
			parts.add(part);
		}
		ModelDefinition def = new ModelDefinition(this, parts, player);
		def.validate();
		Log.debug(def);
		return def;
	}

	public InputStream load(Link link, ResourceEncoding enc) throws IOException {
		try {
			ResolvedLink rl = linkCache.get(link, () -> load0(link, enc));
			return rl.getData();
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private ResolvedLink load0(Link link, ResourceEncoding enc) {
		try {
			ResourceLoader rl = LOADERS.get(link.loader);
			if(rl == null)throw new IOException("Couldn't find loader");
			return new ResolvedLink(rl.loadResource(link.path, enc));
		} catch (IOException e) {
			ResolvedLink rl = localCache.getIfPresent(link);
			if(rl != null)return rl;
			else return new ResolvedLink(e);
		}
	}

	public Image getTemplate() {
		return template;
	}

	public void putLocalResource(Link key, byte[] value) {
		localCache.put(key, new ResolvedLink(value));
	}

	public void clearCache() {
		linkCache.invalidateAll();
		cache.invalidateAll();
	}

	public void clearServerData() {
		serverModels.clear();
	}

	public void setModel(GP forPlayer, byte[] data, boolean forced) {
		if(data == null) {
			Key key = new Key(forPlayer);
			serverModels.remove(key);
			cache.invalidate(key);
		} else {
			Player<?, ?> player = loadPlayer(forPlayer);
			player.setModelDefinition(CompletableFuture.completedFuture(loadModel(data, player)));
			player.forcedSkin = forced;
			serverModels.put(new Key(forPlayer), data);
		}
	}

	public void execute(Runnable task) {
		THREAD_POOL.execute(task);
	}

	private class Key {
		private UUID uuid;
		private GP profile;

		public Key(GP player) {
			this.profile = player;
			this.uuid = getUUID.apply(player);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Key other = (Key) obj;
			if (uuid == null) {
				if (other.uuid != null) return false;
			} else if (!uuid.equals(other.uuid)) return false;
			return true;
		}
	}
}
