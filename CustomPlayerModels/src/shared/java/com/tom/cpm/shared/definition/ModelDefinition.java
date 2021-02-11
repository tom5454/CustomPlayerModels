package com.tom.cpm.shared.definition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.IModelComponent;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.IResolvedModelPart;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinLink;
import com.tom.cpm.shared.skin.SkinProvider;
import com.tom.cpm.shared.util.ListView;
import com.tom.cpm.shared.util.MapViewOfList;

public class ModelDefinition {
	public static final ModelDefinition NULL_DEF = new ModelDefinition(null, Collections.emptyList(), null);
	private final ModelDefinitionLoader loader;
	private final Player playerObj;
	private List<IModelPart> parts;
	private List<IResolvedModelPart> resolved;
	private ModelPartPlayer player;
	private SkinProvider skinOverride;
	private List<RenderedCube> cubes;
	private Map<Integer, RenderedCube> cubeMap;
	private Map<ModelPart, RootModelElement> rootRenderingCubes;
	private int resolveState;
	private boolean editor;
	private AnimationRegistry animations = new AnimationRegistry();

	public ModelDefinition(ModelDefinitionLoader loader, List<IModelPart> parts, Player player) {
		this.loader = loader;
		this.parts = parts;
		this.editor = false;
		this.playerObj = player;
	}

	public ModelDefinition(Editor editor) {
		this.loader = null;
		this.editor = true;
		resolveState = 2;
		rootRenderingCubes = new MapViewOfList<>(
				new ListView<>(editor.elements, e -> (RootModelElement) e.rc),
				RootModelElement::getPart,
				Function.identity()
				);
		skinOverride = editor.skinProvider;
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
			SkinProvider img = parts.getSkin();
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
			RootModelElement elem = new RootModelElement(PlayerModelParts.VALUES[i], this);
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
		rootRenderingCubes = new HashMap<>();
		playerModelParts.forEach((i, e) -> rootRenderingCubes.put(PlayerModelParts.VALUES[i], e));
		cubeMap = new HashMap<>();
		cubes.addAll(playerModelParts.values());
		cubes.forEach(c -> cubeMap.put(c.getId(), c));
		resolved.forEach(r -> r.apply(this));
		resolveState = 2;
		if(MinecraftObjectHolder.DEBUGGING)
			System.out.println(this);
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

	public RootModelElement getModelElementFor(ModelPart part) {
		return rootRenderingCubes.get(part);
	}

	public boolean isEditor() {
		return editor;
	}

	public SkinProvider getSkinOverride() {
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

	public boolean doRenderPart(ModelPart part) {
		if(part instanceof PlayerModelParts)return player.doRenderPart((PlayerModelParts) part);
		return true;
	}

	public RootModelElement addRoot(int id, RootModelType type) {
		RootModelElement elem = new RootModelElement(type, this);
		rootRenderingCubes.put(type, elem);
		return elem;
	}
}
