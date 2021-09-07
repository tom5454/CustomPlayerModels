package com.tom.cpm.shared.definition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.StringBuilderStream;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.IModelComponent;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.IResolvedModelPart;
import com.tom.cpm.shared.parts.ModelPartCloneable;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartPlayer;
import com.tom.cpm.shared.parts.ModelPartScale;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinLink;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.TextureStitcher;

public class ModelDefinition {
	private final ModelDefinitionLoader loader;
	private final Player<?, ?> playerObj;
	private List<IModelPart> parts;
	private List<IResolvedModelPart> resolved;
	private ModelPartPlayer player;
	protected List<RenderedCube> cubes;
	private Map<Integer, RenderedCube> cubeMap;
	private Map<TextureSheetType, TextureProvider> textures;
	private TextureProvider skinTexture;
	private Map<ItemSlot, MatrixStack.Entry> slotTransforms = new EnumMap<>(ItemSlot.class);
	protected Map<VanillaModelPart, PartRoot> rootRenderingCubes;
	private ModelLoadingState resolveState = ModelLoadingState.NEW;
	private AnimationRegistry animations = new AnimationRegistry();
	private ModelPartScale scale;
	private boolean stitchedTexture;
	public boolean hideHeadIfSkull = true, removeArmorOffset;
	public ModelPartCloneable cloneable;
	private Throwable error;

	public ModelDefinition(ModelDefinitionLoader loader, Player<?, ?> player) {
		this.loader = loader;
		this.playerObj = player;
	}

	public ModelDefinition(Throwable e, Player<?, ?> player) {
		this.loader = null;
		this.playerObj = player;
		setError(e);
	}

	public ModelDefinition setParts(List<IModelPart> parts) {
		this.parts = parts;
		return this;
	}

	protected ModelDefinition() {
		this.loader = null;
		resolveState = ModelLoadingState.LOADED;
		this.playerObj = null;
	}

	public void startResolve() {
		resolveState = ModelLoadingState.RESOLVING;
		ModelDefinitionLoader.THREAD_POOL.submit(() -> {
			try {
				resolveAll();
			} catch (SafetyException e) {
				setModelBlocked(e);
			} catch (Throwable e) {
				setError(e);
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
		textures = new HashMap<>();
		for (IResolvedModelPart parts : resolved) {
			TextureProvider img = parts.getSkin();
			if(img != null) {
				if(textures.containsKey(TextureSheetType.SKIN))throw new IOException("Multiple skin tags");
				else {
					textures.put(TextureSheetType.SKIN, img);
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
		ConfigKeys.MAX_CUBE_COUNT.checkFor(playerObj, cubes.size(), BlockReason.TOO_MANY_CUBES);
		TextureStitcher stitcher = new TextureStitcher(playerObj.isClientPlayer() ? 8192 : ConfigKeys.MAX_TEX_SHEET_SIZE.getValueFor(playerObj));
		if(textures.containsKey(TextureSheetType.SKIN)) {
			stitcher.setBase(textures.get(TextureSheetType.SKIN));
			skinTexture = textures.get(TextureSheetType.SKIN);
		} else {
			Image skin;
			try {
				skin = playerObj.getTextures().getTexture(TextureType.SKIN).get(5, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e1) {
				throw new IOException(e1);
			}
			if(skin == null)skin = playerObj.getSkinType().getSkinTexture();
			stitcher.setBase(skin);
			skinTexture = new TextureProvider(skin, new Vec2i(64, 64));
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
			stitcher.allocSingleColor(whiteBox, 0xffffffff, uv -> {
				coloredCubes.forEach(cube -> {
					cube.recolor = true;
					cube.getCube().texSize = 1;
					cube.getCube().u = uv.x;
					cube.getCube().v = uv.y;
				});
			});
		}
		resolved.forEach(r -> r.stitch(stitcher));
		TextureProvider tx = stitcher.finish();
		if(tx != null) {
			textures.put(TextureSheetType.SKIN, tx);
			skinTexture = tx;
		}
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
		resolveState = ModelLoadingState.LOADED;
		Log.debug(this);
	}

	protected void convertPart(RootModelElement p) {
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
		resolveState = ModelLoadingState.CLEANED_UP;
		if(loader == null)return;
		if(cubes != null)
			cubes.forEach(c -> {
				if(c.renderObject != null)c.renderObject.free();
				c.renderObject = null;
			});
		if(textures != null)
			textures.values().forEach(TextureProvider::free);
		cubes = null;
		textures = null;
	}

	public boolean doRender() {
		return loader != null && resolveState == ModelLoadingState.LOADED;
	}

	public ModelLoadingState getResolveState() {
		return resolveState;
	}

	public PartRoot getModelElementFor(VanillaModelPart part) {
		return rootRenderingCubes.get(part);
	}

	public boolean isEditor() {
		return false;
	}

	public AnimationRegistry getAnimations() {
		return animations;
	}

	public Player<?, ?> getPlayerObj() {
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
		case NEW:
		case RESOLVING:
			bb.append("\n\tParts:");
			for (IModelPart iModelPart : parts) {
				bb.append("\n\t\t");
				bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
			}
			break;

		case LOADED:
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

		case ERRORRED:
		case SAFETY_BLOCKED:
			bb.append("\n\t\t");
			error.printStackTrace(new StringBuilderStream(bb, "\n\t\t"));
			if(error instanceof SafetyException)bb.append(((SafetyException)error).getBlockReason());
			else bb.append("Unexpected error");
			break;

		default:
			break;
		}
		return bb.toString();
	}

	public RootModelElement addRoot(int baseID, VanillaModelPart type) {
		RootModelElement elem = new RootModelElement(type, this);
		elem.children = new ArrayList<>();
		rootRenderingCubes.computeIfAbsent(type, k -> new PartRoot(elem)).add(elem);
		cubes.add(elem);
		cubeMap.put(baseID, elem);
		if(type instanceof PlayerModelParts && stitchedTexture) {
			convertPart(elem);
		}
		return elem;
	}

	public TextureProvider getTexture(TextureSheetType key, boolean inGui) {
		if(key == TextureSheetType.SKIN && inGui)return skinTexture;
		return key.editable ? textures == null ? null : textures.get(key) : null;
	}

	public void setTexture(TextureSheetType key, TextureProvider value) {
		textures.put(key, value);
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

	public SkinType getSkinType() {
		return playerObj.getSkinType();
	}

	public void setScale(ModelPartScale scale) {
		this.scale = scale;
	}

	public ModelPartScale getScale() {
		return scale;
	}

	public static ModelDefinition createVanilla(Supplier<TextureProvider> texture, SkinType type) {
		return new VanillaDefinition(texture, type);
	}

	public boolean hasRoot(VanillaModelPart type) {
		return rootRenderingCubes.containsKey(type);
	}

	public ModelDefinitionLoader getLoader() {
		return loader;
	}

	public <V> void check(PlayerSpecificConfigKey<V> key, Predicate<V> check, BlockReason err) throws SafetyException {
		key.checkFor(playerObj, check, err);
	}

	public void setModelBlocked(SafetyException ex) {
		cleanup();
		resolveState = ModelLoadingState.SAFETY_BLOCKED;
		error = ex;
		clear();
	}

	public MatrixStack.Entry getTransform(ItemSlot slot) {
		return slotTransforms.get(slot);
	}

	public void storeTransform(ItemSlot slot, MatrixStack stack) {
		slotTransforms.put(slot, stack.storeLast());
	}

	private void clear() {
		parts = Collections.emptyList();
		resolved = null;
		player = null;
		cubes = null;
		cubeMap = null;
		textures = null;
		rootRenderingCubes = null;
		animations = null;
		scale = null;
		slotTransforms = null;
	}

	public void clearTransforms() {
		slotTransforms.clear();
	}

	public static enum ModelLoadingState {
		NEW,
		RESOLVING,
		LOADED,
		SAFETY_BLOCKED,
		ERRORRED,
		CLEANED_UP
	}

	public SafetyException getSafetyEx() {
		if(error instanceof SafetyException)
			return (SafetyException) error;
		return null;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable ex) {
		cleanup();
		resolveState = ModelLoadingState.ERRORRED;
		error = ex;
		Log.error("Failed to load model", ex);
		clear();
	}

	public boolean isHideHeadIfSkull() {
		return hideHeadIfSkull;
	}

	public boolean isRemoveArmorOffset() {
		return removeArmorOffset;
	}
}
