package com.tom.cpm.shared.definition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.util.concurrent.UncheckedExecutionException;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.tag.AllTagManagers;
import com.tom.cpl.tag.IAllTags;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.LocalizedException;
import com.tom.cpl.util.StringBuilderStream;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.IModelComponent;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.PartPosition;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.BoxRender;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.model.render.ItemTransform;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.parts.IModelPart;
import com.tom.cpm.shared.parts.IResolvedModelPart;
import com.tom.cpm.shared.parts.ModelPartCloneable;
import com.tom.cpm.shared.parts.ModelPartCollection;
import com.tom.cpm.shared.parts.ModelPartDefinition;
import com.tom.cpm.shared.parts.ModelPartDefinitionLink;
import com.tom.cpm.shared.parts.ModelPartLink;
import com.tom.cpm.shared.parts.ModelPartSkin;
import com.tom.cpm.shared.parts.ModelPartSkinLink;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.TextureStitcher;

public class ModelDefinition {
	private final ModelDefinitionLoader<?> loader;
	private final Player<?> playerObj;
	private List<IModelPart> parts;
	private List<IResolvedModelPart> resolved;
	protected List<RenderedCube> cubes;
	private Map<Integer, RenderedCube> cubeMap;
	private Map<TextureSheetType, TextureProvider> textures;
	private TextureProvider skinTexture;
	public Map<ItemRenderer, ItemTransform> itemTransforms = new HashMap<>();
	protected Map<VanillaModelPart, PartRoot> rootRenderingCubes;
	protected ModelLoadingState resolveState = ModelLoadingState.NEW;
	private AnimationRegistry animations = new AnimationRegistry();
	private ScaleData scale;
	public PartPosition fpLeftHand, fpRightHand;
	private boolean stitchedTexture;
	public boolean hideHeadIfSkull = true, removeArmorOffset, removeBedOffset, enableInvisGlow;
	public ModelPartCloneable cloneable;
	private Throwable error;
	public IAllTags modelTagManager;

	public ModelDefinition(ModelDefinitionLoader<?> loader, Player<?> player) {
		this.loader = loader;
		this.playerObj = player;
		this.modelTagManager = new AllTagManagers(MinecraftClientAccess.get().getBuiltinTags());
	}

	public ModelDefinition(Throwable e, Player<?> player) {
		this.loader = null;
		this.playerObj = player;
		this.modelTagManager = new AllTagManagers(MinecraftClientAccess.get().getBuiltinTags());
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
		ModelDefinitionLoader.THREAD_POOL.execute(() -> {
			try {
				resolveAll();
			} catch (Throwable e) {
				setError(e);
			}
		});
	}

	public void validate() {
		if(loader == null)return;
		boolean hasSkin = false;
		boolean hasDef = false;
		for (IModelPart iModelPart : parts) {
			if(iModelPart instanceof ModelPartSkin || iModelPart instanceof ModelPartSkinLink) {
				if(hasSkin)throw new IllegalStateException("Multiple skin tags");
				hasSkin = true;
			}
			if(iModelPart instanceof ModelPartDefinition || iModelPart instanceof ModelPartDefinitionLink) {
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
		cubes = new ArrayList<>();
		rootRenderingCubes = new HashMap<>();
		Map<Integer, RootModelElement> playerModelParts = new HashMap<>();
		for(int i = 0;i<PlayerModelParts.VALUES.length;i++) {
			RootModelElement elem = new RootModelElement(PlayerModelParts.VALUES[i], this);
			rootRenderingCubes.put(PlayerModelParts.VALUES[i], new PartRoot(elem));
			playerModelParts.put(i, elem);
		}
		resolved.forEach(r -> r.preApply(this));
		for (RenderedCube rc : cubes) {
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
		int cc = cubes.size();
		for (RenderedCube rc : cubes) {
			if (rc.extrude) {
				cc += BoxRender.getExtrudeSize(rc.getCube().size, rc.getCube().texSize);
			}
		}
		ConfigKeys.MAX_CUBE_COUNT.checkFor(playerObj, cc, BlockReason.TOO_MANY_CUBES);
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
		if(!p.isHidden()) {
			p.setHidden(true);
			Cube cube = new Cube();
			PlayerPartValues val = PlayerPartValues.getFor(part, playerObj.getSkinType());
			cube.offset = val.getOffset();
			cube.rotation = new Vec3f(0, 0, 0);
			cube.pos = new Vec3f(0, 0, 0);
			cube.size = val.getSize();
			cube.scale = new Vec3f(1, 1, 1);
			cube.meshScale = new Vec3f(1, 1, 1);
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

	public Player<?> getPlayerObj() {
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
			bb.append("\n\tOther:");
			for (IResolvedModelPart iModelPart : resolved) {
				bb.append("\n\t\t");
				bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
			}
			break;

		case ERRORRED:
		case SAFETY_BLOCKED:
			bb.append("\n\t\t");
			StringBuilderStream.stacktraceToString(error, bb, "\n\t\t");
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

	public ModelPartLink findDefLink() {
		for (IModelPart iModelPart : parts) {
			if(iModelPart instanceof ModelPartDefinitionLink || iModelPart instanceof ModelPartCollection.PackageLink) {
				return (ModelPartLink) iModelPart;
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

	public void setScale(ScaleData scale) {
		this.scale = scale;
	}

	public ScaleData getScale() {
		return scale;
	}

	public static ModelDefinition createVanilla(Supplier<TextureProvider> texture, SkinType type) {
		return new VanillaDefinition(texture, type);
	}

	public boolean hasRoot(VanillaModelPart type) {
		return rootRenderingCubes.containsKey(type);
	}

	public ModelDefinitionLoader<?> getLoader() {
		return loader;
	}

	public <V> void check(PlayerSpecificConfigKey<V> key, Predicate<V> check, BlockReason err) throws SafetyException {
		key.checkFor(playerObj, check, err);
	}

	public ItemTransform getTransform(ItemSlot slot) {
		return itemTransforms.keySet().stream().filter(s -> s.slot == slot).findFirst().map(this::getTransform).orElse(null);
	}

	public ItemTransform getTransform(ItemRenderer slot) {
		return itemTransforms.get(slot);
	}

	public void storeTransform(ItemRenderer render, MatrixStack stack, boolean doRender) {
		itemTransforms.computeIfAbsent(render, k -> new ItemTransform()).set(stack, doRender);
	}

	private void clear() {
		parts = Collections.emptyList();
		resolved = null;
		cubes = null;
		cubeMap = null;
		textures = null;
		rootRenderingCubes = null;
		animations = null;
		scale = null;
		itemTransforms = null;
	}

	public static enum ModelLoadingState {
		NEW,
		RESOLVING,
		LOADED,
		SAFETY_BLOCKED,
		ERRORRED,
		CLEANED_UP
	}

	public BlockReason getBlockReason() {
		if(error instanceof SafetyException)
			return ((SafetyException) error).getBlockReason();
		return null;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable ex) {
		cleanup();
		if(ex instanceof ExecutionException || ex instanceof UncheckedExecutionException)
			ex = ex.getCause();
		if(ex instanceof SafetyException)
			resolveState = ModelLoadingState.SAFETY_BLOCKED;
		else
			resolveState = ModelLoadingState.ERRORRED;
		error = ex;
		if(!(ex instanceof IOException || ex instanceof SafetyException))
			Log.error("Failed to load model", ex);
		clear();
	}

	public boolean isHideHeadIfSkull() {
		return hideHeadIfSkull;
	}

	public boolean isRemoveArmorOffset() {
		return removeArmorOffset;
	}

	public FormatText getStatus() {
		switch (getResolveState()) {
		case ERRORRED:
			if(getError() instanceof LocalizedException)
				return new FormatText("label.cpm.errorLoadingModel", ((LocalizedException)getError()).getLocalizedText());
			else
				return new FormatText("label.cpm.errorLoadingModel", getError().toString());
		case NEW:
		case RESOLVING:
			return new FormatText("label.cpm.loading");
		case SAFETY_BLOCKED:
			if(getBlockReason() == BlockReason.BLOCK_LIST)return null;
			return new FormatText("label.cpm.safetyBlocked");
		case LOADED:
		case CLEANED_UP:
		default:
			return null;
		}
	}

	public void addCubes(Collection<RenderedCube> cubes) {
		this.cubes.addAll(cubes);
	}
}
