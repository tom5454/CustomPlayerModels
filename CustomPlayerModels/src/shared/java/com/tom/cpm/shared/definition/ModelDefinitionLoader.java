package com.tom.cpm.shared.definition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.ResourceLoader;
import com.tom.cpm.shared.config.ResourceLoader.ResourceEncoding;
import com.tom.cpm.shared.config.SocialConfig;
import com.tom.cpm.shared.definition.Link.ResolvedLink;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.io.ChecksumInputStream;
import com.tom.cpm.shared.io.ChecksumOutputStream;
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
	private Function<GP, String> getName;
	private final LoadingCache<Key, Player<?, ?>> cache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).removalListener(new RemovalListener<Key, Player<?, ?>>() {

		@Override
		public void onRemoval(RemovalNotification<ModelDefinitionLoader<GP>.Key, Player<?, ?>> notification) {
			notification.getValue().cleanup();
		}
	}).build(new CacheLoader<Key, Player<?, ?>>() {

		@Override
		public Player<?, ?> load(Key key) throws Exception {
			Player<?, ?> player = playerFactory.apply(key.profile);
			if(serverModels.containsKey(key)) {
				player.setModelDefinition(CompletableFuture.supplyAsync(() -> loadModel(serverModels.get(key), player), THREAD_POOL));
			} else {
				player.setModelDefinition(player.getTextures().load().thenCompose(v -> player.getTextures().getTexture(TextureType.SKIN)).thenApplyAsync(skin -> {
					if(skin != null && player.getModelDefinition() == null) {
						return loadModel(skin, player);
					} else {
						return null;
					}
				}, THREAD_POOL));
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

	public ModelDefinitionLoader(Function<GP, Player<?, ?>> playerFactory, Function<GP, UUID> getUUID, Function<GP, String> getName) {
		try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/free_space_template.png")) {
			this.template = Image.loadFrom(is);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load template", e);
		}
		this.playerFactory = playerFactory;
		this.getUUID = getUUID;
		this.getName = getName;
	}

	public Player<?, ?> loadPlayer(GP player) {
		try {
			return cache.get(new Key(player));
		} catch (ExecutionException | UncheckedExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ModelDefinition loadModel(String data, Player<?, ?> player) {
		return loadModel(Base64.getDecoder().decode(data), player);
	}

	public ModelDefinition loadModel(byte[] data, Player<?, ?> player) {
		try(ByteArrayInputStream in = new ByteArrayInputStream(data)) {
			return loadModel(in, player);
		} catch (Exception e) {
			return new ModelDefinition(e, player);
		}
	}

	public ModelDefinition loadModel(Image skin, Player<?, ?> player) {
		try(SkinDataInputStream in = new SkinDataInputStream(skin, template, player.getSkinType().getChannel())) {
			return loadModel(in, player);
		} catch (Exception e) {
			return new ModelDefinition(e, player);
		}
	}

	private ModelDefinition loadModel(InputStream in, Player<?, ?> player) {
		ModelDefinition def = new ModelDefinition(this, player);
		try {
			if(in.read() != HEADER)return null;
			ConfigKeys.ENABLE_MODEL_LOADING.checkFor(player, v -> v, BlockReason.CONFIG_DISABLED);
			if(SocialConfig.isBlocked(player.getUUID().toString()))throw new SafetyException(BlockReason.BLOCK_LIST);
			ChecksumInputStream cis = new ChecksumInputStream(in);
			IOHelper din = new IOHelper(cis);
			List<IModelPart> parts = new ArrayList<>();
			while(true) {
				IModelPart part = din.readObjectBlock(ModelPartType.VALUES, (t, d) -> t.getFactory().create(d, def));
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
			def.setParts(parts);
			def.validate();
			Log.debug(def);
		} catch (SafetyException e) {
			def.setModelBlocked(e);
		} catch (Throwable e) {
			def.setError(e);
		}
		return def;
	}

	public InputStream load(Link link, ResourceEncoding enc, ModelDefinition def) throws IOException {
		try {
			ResolvedLink rl = linkCache.get(link, () -> load0(link, enc, def));
			return rl.getData();
		} catch (ExecutionException e) {
			if(e.getCause() instanceof SafetyException)throw (SafetyException) e.getCause();
			throw new IOException(e);
		}
	}

	private ResolvedLink load0(Link link, ResourceEncoding enc, ModelDefinition def) throws SafetyException {
		try {
			ResourceLoader rl = LOADERS.get(link.loader);
			if(rl == null)throw new IOException("Couldn't find loader");
			return new ResolvedLink(rl.loadResource(link.path, enc, def));
		} catch (SafetyException e) {
			throw e;
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

	public List<Player<?, ?>> getPlayers() {
		return new ArrayList<>(cache.asMap().values());
	}

	private class Key {
		private UUID uuid;
		private GP profile;

		public Key(GP player) {
			this.profile = player;
			this.uuid = getUUID.apply(player);
		}

		public Key(UUID uuid) {
			this.uuid = uuid;
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

	public void settingsChanged(UUID uuid) {
		cache.invalidate(new Key(uuid));
	}

	public UUID getGP_UUID(GP gp) {
		return getUUID.apply(gp);
	}

	public String getGP_Name(GP gp) {
		return getName.apply(gp);
	}

	public CompletableFuture<Boolean> cloneModel(Player<?, ?> player, String name) {
		ModelDefinition d = player.getModelDefinition();
		if(d != null && d.cloneable != null) {
			String desc = d.cloneable.desc;
			Image icon = d.cloneable.icon;
			byte[] data = serverModels.get(new Key(player.getUUID()));
			if(data == null) {
				return player.getTextures().load().thenCompose(v -> player.getTextures().getTexture(TextureType.SKIN)).thenApplyAsync(skin -> {
					try(SkinDataInputStream in = new SkinDataInputStream(skin, template, player.getSkinType().getChannel())) {
						IOHelper ioh = new IOHelper();
						IOHelper.copy(in, ioh.getDout());
						if(ioh.read() != HEADER)return false;
						ioh.reset();
						storeModel(name, desc, icon, ioh.toBytes());
						return true;
					} catch (IOException e) {
						e.printStackTrace();
					}
					return false;
				}, THREAD_POOL);
			} else {
				try {
					storeModel(name, desc, icon, data);
					return CompletableFuture.completedFuture(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return CompletableFuture.completedFuture(false);
			}
		} else return CompletableFuture.completedFuture(false);
	}

	private void storeModel(String name, String desc, Image icon, byte[] data) throws IOException {
		File models = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
		models.mkdirs();
		File out = new File(models, name.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + ".cpmmodel");
		Random r = new Random();
		while(out.exists()) {
			out = new File(models, name.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + Integer.toHexString(r.nextInt()) + ".cpmmodel");
		}
		try (FileOutputStream fout = new FileOutputStream(out)){
			fout.write(ModelDefinitionLoader.HEADER);
			ChecksumOutputStream cos = new ChecksumOutputStream(fout);
			IOHelper h = new IOHelper(cos);
			h.writeUTF(name);
			h.writeUTF(desc != null ? desc : "");
			h.writeVarInt(data.length);
			h.write(data);
			h.writeVarInt(0);
			if(icon != null) {
				h.writeImage(icon);
			} else {
				h.writeVarInt(0);
			}
			cos.close();
		}
	}
}
