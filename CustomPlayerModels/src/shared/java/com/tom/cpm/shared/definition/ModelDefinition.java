package com.tom.cpm.shared.definition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.IModelComponent;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.IResolvedModelPart;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinLink;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.TextureStitcher;

public class ModelDefinition {
	public static final ModelDefinition NULL_DEF = new ModelDefinition(null, Collections.emptyList(), null);
	private final ModelDefinitionLoader loader;
	private final Player playerObj;
	private List<IModelPart> parts;
	private List<IResolvedModelPart> resolved;
	private ModelPartPlayer player;
	private TextureProvider skinOverride;
	private TextureProvider listIconOverride;
	private List<RenderedCube> cubes;
	private Map<Integer, RenderedCube> cubeMap;
	private Map<ModelPart, PartRoot> rootRenderingCubes;
	private int resolveState;
	private AnimationRegistry animations = new AnimationRegistry();
	private boolean stitchedTexture;

	public ModelDefinition(ModelDefinitionLoader loader, List<IModelPart> parts, Player player) {
		this.loader = loader;
		this.parts = parts;
		this.playerObj = player;
	}

	protected ModelDefinition() {
		this.loader = null;
		resolveState = 2;
		this.playerObj = null;
	}

	public void startResolve() {
		resolveState = 1;
		ModelDefinitionLoader.THREAD_POOL.submit(() -> {
			try {
				resolveAll();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	public void validate() {
		if(loader == null)return;
		boolean hasSkin = false;
		boolean hasPlayer = false;
		boolean hasDef = false;
		for (IModelPart iModelPart : parts) {
			if(iModelPart instanceof ModelPartSkin || iModelPart instanceof ModelPartSkinLink) {
				if(hasSkin)throw new IllegalStateException("Multiple skin tags");
				hasSkin = true;
			}
			if(iModelPart instanceof ModelPartPlayer) {
				if(hasPlayer)throw new IllegalStateException("Multiple player tags");
				hasPlayer = true;
				player = (ModelPartPlayer) iModelPart;
			}
			if(iModelPart instanceof ModelPartDefinition) {
				if(hasDef)throw new IllegalStateException("Multiple definition tags");
				ModelPartDefinition def = (ModelPartDefinition) iModelPart;
				if(def.getPlayer() != null) {
					if(hasPlayer)throw new IllegalStateException("Multiple player tags");
					hasPlayer = true;
					player = def.getPlayer();
				}
				hasDef = true;
			}
			if(iModelPart instanceof ModelPartDefinitionLink) {
				if(hasDef)throw new IllegalStateException("Multiple definition tags");
				hasDef = true;
			}
		}
	}

	public void resolveAll() throws IOException {
		if(loader == null)return;
		resolved = new ArrayList<>();
		for (IModelPart part : parts) {
			resolved.add(part.resolve());
		}
		for (IResolvedModelPart parts : resolved) {
			TextureProvider img = parts.getSkin();
			if(img != null) {
				if(skinOverride != null)throw new IOException("Multiple skin tags");
				else {
					skinOverride = img;
				}
			}
			if(parts instanceof ModelPartDefinition) {
				ModelPartDefinition def = (ModelPartDefinition) parts;
				if(def.getPlayer() != null) {
					if(player != null && player != def.getPlayer())throw new IllegalStateException("Multiple player tags");
					player = def.getPlayer();
				}
			}
		}
		if(player == null)player = new ModelPartPlayer();
		cubes = new ArrayList<>();
		Map<Integer, RootModelElement> playerModelParts = new HashMap<>();
		for(int i = 0;i<PlayerModelParts.VALUES.length;i++) {
			RootModelElement elem = new RootModelElement(PlayerModelParts.VALUES[i]);
			elem.hidden = !player.doRenderPart(PlayerModelParts.VALUES[i]);
			playerModelParts.put(i, elem);
		}
		for (IResolvedModelPart part : resolved) {
			List<RenderedCube> cs = part.getModel();
			for (RenderedCube rc : cs) {
				int id = rc.getCube().parentId;
				RenderedCube p = playerModelParts.get(id);
				if(p != null) {
					p.addChild(rc);
					rc.setParent(p);
				}
				if(rc.getParent() == null) {
					throw new IOException("Cube without parent");
				}
			}
			cubes.addAll(cs);
		}
		TextureStitcher stitcher = new TextureStitcher();
		if(skinOverride != null) {
			stitcher.setBase(skinOverride);
		} else {
			Image skin;
			try {
				skin = playerObj.getSkin().get(5, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e1) {
				throw new IOException(e1);
			}
			stitcher.setBase(skin);
		}
		Vec2i whiteBox = new Vec2i(0, 0);
		List<RenderedCube> coloredCubes = new ArrayList<>();
		cubes.forEach(t -> {
			if(t.getCube().texSize == 0) {
				coloredCubes.add(t);
				int dx = MathHelper.ceil(t.getCube().size.x);
				int dy = MathHelper.ceil(t.getCube().size.y);
				int dz = MathHelper.ceil(t.getCube().size.z);
				whiteBox.x = Math.max(whiteBox.x, 2 * (dx + dz));
				whiteBox.y = Math.max(whiteBox.y, dy + dz);
			}
		});
		if(whiteBox.x > 0 && whiteBox.y > 0) {
			stitcher.allocSingleColor(whiteBox, 0xffffffff, uv -> coloredCubes.forEach(cube -> {
				cube.recolor = true;
				cube.getCube().texSize = 1;
				cube.getCube().u = uv.x;
				cube.getCube().v = uv.y;
			}));
		}
		resolved.forEach(r -> r.stitch(stitcher));
		skinOverride = stitcher.finish();
		stitchedTexture = stitcher.hasStitches();
		if(stitchedTexture) {
			for (PlayerModelParts part : PlayerModelParts.VALUES) {
				if(part == PlayerModelParts.CUSTOM_PART)continue;
				convertPart(playerModelParts.get(part.ordinal()));
			}
		}
		rootRenderingCubes = new HashMap<>();
		playerModelParts.forEach((i, e) -> rootRenderingCubes.put(PlayerModelParts.VALUES[i], new PartRoot(e)));
		cubeMap = new HashMap<>();
		cubes.addAll(playerModelParts.values());
		cubes.forEach(c -> cubeMap.put(c.getId(), c));
		resolved.forEach(r -> r.apply(this));
		resetAnimationPos();
		resolveState = 2;
		if(MinecraftObjectHolder.DEBUGGING)
			System.out.println(this);
	}

	private void convertPart(RootModelElement p) {
		PlayerModelParts part = (PlayerModelParts) p.getPart();
		if(!p.hidden) {
			p.hidden = true;
			Cube cube = new Cube();
			PlayerPartValues val = PlayerPartValues.getFor(part, playerObj.getSkinType());
			cube.offset = val.getOffset();
			cube.rotation = new Vec3f(0, 0, 0);
			cube.pos = new Vec3f(0, 0, 0);
			cube.size = val.getSize();
			cube.scale = new Vec3f(1, 1, 1);
			cube.u = val.u;
			cube.v = val.v;
			cube.texSize = 1;
			cube.id = 0xfff0 + part.ordinal();
			RenderedCube rc = new RenderedCube(cube);
			rc.setParent(p);
			p.addChild(rc);
		}
	}

	public void cleanup() {
		if(loader == null)return;
		cubes.forEach(MinecraftClientAccess.get().getPlayerRenderManager()::cleanupRenderedCube);
		if(skinOverride != null)skinOverride.free();
	}

	public boolean doRender() {
		return loader != null;
	}

	public int getResolveState() {
		return resolveState;
	}

	public PartRoot getModelElementFor(ModelPart part) {
		return rootRenderingCubes.get(part);
	}

	public boolean isEditor() {
		return false;
	}

	public TextureProvider getSkinOverride() {
		return skinOverride;
	}

	public AnimationRegistry getAnimations() {
		return animations;
	}

	public Player getPlayerObj() {
		return playerObj;
	}

	public RenderedCube getElementById(int id) {
		return cubeMap.get(id);
	}

	public void resetAnimationPos() {
		cubes.forEach(IModelComponent::reset);
	}

	@Override
	public String toString() {
		StringBuilder bb = new StringBuilder("ModelDefinition\n\tResolved: ");
		bb.append(resolveState);
		switch (resolveState) {
		case 0:
		case 1:
			bb.append("\n\tParts:");
			for (IModelPart iModelPart : parts) {
				bb.append("\n\t\t");
				bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
			}
			break;

		case 2:
			bb.append("\n\tCubes: ");
			bb.append(cubes.size());
			bb.append("\n\tPlayer:\n\t\t");
			bb.append(player.toString().replace("\n", "\n\t\t"));
			bb.append("\n\tOther:");
			for (IResolvedModelPart iModelPart : resolved) {
				bb.append("\n\t\t");
				bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
			}
			break;
		default:
			break;
		}
		return bb.toString();
	}

	public RootModelElement addRoot(int baseID, ModelPart type) {
		RootModelElement elem = new RootModelElement(type);
		elem.children = new ArrayList<>();
		rootRenderingCubes.computeIfAbsent(type, k -> new PartRoot(elem)).add(elem);
		cubes.add(elem);
		cubeMap.put(baseID, elem);
		if(type instanceof PlayerModelParts && stitchedTexture) {
			convertPart(elem);
		}
		return elem;
	}

	public void setListIconOverride(TextureProvider listIconOverride) {
		this.listIconOverride = listIconOverride;
	}

	public TextureProvider getListIconOverride() {
		return listIconOverride;
	}

	public Link findDefLink() {
		for (IModelPart iModelPart : parts) {
			if(iModelPart instanceof ModelPartDefinitionLink) {
				return ((ModelPartDefinitionLink)iModelPart).getLink();
			}
		}
		return null;
	}

	public boolean isStitchedTexture() {
		return stitchedTexture;
	}
}
