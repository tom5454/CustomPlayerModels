package com.tom.cpm.shared.definition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.ResourceLoader;
import com.tom.cpm.shared.config.ResourceLoader.ResourceEncoding;
import com.tom.cpm.shared.io.ChecksumInputStream;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.SkinDataInputStream;
import com.tom.cpm.shared.loaders.GistResourceLoader;
import com.tom.cpm.shared.loaders.GithubRepoResourceLoader;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.ModelPartEnd;
import com.tom.cpm.shared.parts.ModelPartSkinType;
import com.tom.cpm.shared.parts.ModelPartType;
import com.tom.cpm.shared.util.Image;

public class ModelDefinitionLoader {
	public static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
	private Function<Object, Player> playerFactory;
	private final LoadingCache<Object, Player> cache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<Object, Player>() {

		@Override
		public Player load(Object key) throws Exception {
			Player player = playerFactory.apply(key);
			player.loadSkin(() -> {
				CompletableFuture<Image> skinF = player.getSkin();
				skinF.thenAccept(skin -> {
					if(skin != null && player.getModelDefinition() == null)
						player.setModelDefinition(loadModel(skin, player));
				});
			});
			return player;
		}
	});
	private static final Map<String, ResourceLoader> LOADERS = new HashMap<>();
	static {
		LOADERS.put("git", new GistResourceLoader());
		LOADERS.put("gh", new GithubRepoResourceLoader());
	}
	private Image template;
	public static final int HEADER = 0x53;

	public ModelDefinitionLoader(Image template, Function<Object, Player> playerFactory) {
		this.template = template;
		this.playerFactory = playerFactory;
	}

	public Player loadPlayer(Object player) {
		try {
			return cache.get(player);
		} catch (ExecutionException | UncheckedExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ModelDefinition loadModel(String data, Player player) {
		try(ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(data))) {
			return loadModel(in, player);
		} catch (Exception e) {
			return null;
		}
	}

	public ModelDefinition loadModel(Image skin, Player player) {
		try(SkinDataInputStream in = new SkinDataInputStream(skin, template, player.getSkinType())) {
			return loadModel(in, player);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private ModelDefinition loadModel(InputStream in, Player player) throws IOException {
		if(in.read() != HEADER)return null;
		ChecksumInputStream cis = new ChecksumInputStream(in);
		IOHelper din = new IOHelper(cis);
		List<IModelPart> parts = new ArrayList<>();
		while(true) {
			IModelPart part = din.readObjectBlock(ModelPartType.VALUES, (t, d) -> t.getFactory().create(d, this));
			if(part == null)continue;
			if(part instanceof ModelPartSkinType && in instanceof SkinDataInputStream) {
				SkinDataInputStream sin = (SkinDataInputStream) in;
				int type = ((ModelPartSkinType)part).getSkinType();
				if(type != sin.getChannel()) {
					sin.setChannel(type);
					System.out.println("[WARN]: Mismatching skin type");
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
		if(MinecraftObjectHolder.DEBUGGING)System.out.println(def);
		return def;
	}

	public InputStream load(Link link, ResourceEncoding enc) throws IOException {
		return LOADERS.get(link.loader).loadResource(link.path, enc);
	}

	public Image getTemplate() {
		return template;
	}

	public void clearCache() {
		cache.invalidateAll();
	}
}
