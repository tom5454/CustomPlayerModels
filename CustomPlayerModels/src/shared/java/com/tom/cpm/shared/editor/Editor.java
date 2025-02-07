package com.tom.cpm.shared.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.gui.UI;
import com.tom.cpl.gui.UpdaterRegistry;
import com.tom.cpl.gui.UpdaterRegistry.BooleanUpdater;
import com.tom.cpl.gui.UpdaterRegistry.Updater;
import com.tom.cpl.gui.UpdaterRegistry.UpdaterWithValue;
import com.tom.cpl.gui.elements.Tree.TreeHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.actions.Action;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.AnimationEncodingData;
import com.tom.cpm.shared.editor.anim.AnimationProperties;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.elements.MultiSelector;
import com.tom.cpm.shared.editor.elements.RootGroups;
import com.tom.cpm.shared.editor.gui.ModeDisplayType;
import com.tom.cpm.shared.editor.gui.RenderUtil;
import com.tom.cpm.shared.editor.gui.ViewportPanel;
import com.tom.cpm.shared.editor.project.ProjectFile;
import com.tom.cpm.shared.editor.project.ProjectIO;
import com.tom.cpm.shared.editor.project.loaders.AnimationsLoaderV1;
import com.tom.cpm.shared.editor.tags.EditorTags;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.editor.tree.ScalingElement;
import com.tom.cpm.shared.editor.tree.TexturesElement;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.TreeElement.ModelTree;
import com.tom.cpm.shared.editor.tree.TreeElement.TreeSettingElement;
import com.tom.cpm.shared.editor.tree.VecType;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.QuickTask;
import com.tom.cpm.shared.editor.util.StoreIDGen;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.model.PartPosition;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class Editor {
	public UpdaterRegistry updaterReg = new UpdaterRegistry();
	public Updater<Vec3f> setOffset = updaterReg.create(null);
	public Updater<Vec3f> setRot = updaterReg.create(null);
	public Updater<Vec3f> setPosition = updaterReg.create(null);
	public Updater<Vec3f> setSize = updaterReg.create(null);
	public Updater<Vec3f> setMeshScale = updaterReg.create(null);
	public Updater<Float> setMCScale = updaterReg.create(null);
	public Updater<Boolean> setMirror = updaterReg.create(null);
	public Updater<String> updateName = updaterReg.create(null);
	public Updater<String> setModeBtn = updaterReg.create(null);
	public Updater<ModeDisplayType> setModePanel = updaterReg.create(ModeDisplayType.NULL);
	public Updater<Vec3i> setTexturePanel = updaterReg.create(null);
	public Updater<Boolean> setVis = updaterReg.create(null);
	public Updater<Boolean> setDelEn = updaterReg.create(false);
	public Updater<Boolean> setAddEn = updaterReg.create(false);
	public Updater<Boolean> setGlow = updaterReg.create(null);
	public Updater<Integer> setPartColor = updaterReg.create(null);
	public Updater<Boolean> setReColor = updaterReg.create(null);
	public Updater<Integer> setAnimFrame = updaterReg.create(null);
	public Updater<Vec3f> setAnimPos = updaterReg.create(null);
	public Updater<Vec3f> setAnimRot = updaterReg.create(null);
	public Updater<Vec3f> setAnimScale = updaterReg.create(null);
	public Updater<Boolean> setFrameAddEn = updaterReg.create(false);
	public Updater<Boolean> setAnimDelEn = updaterReg.create(false);
	public Updater<Boolean> setFrameDelEn = updaterReg.create(false);
	public Updater<Integer> setAnimDuration = updaterReg.create(null);
	public Updater<Integer> setAnimColor = updaterReg.create(null);
	public Updater<Boolean> setAnimShow = updaterReg.create(null);
	public Updater<Boolean> setAnimPlayEn = updaterReg.create(false);
	public Updater<Boolean> setHiddenEffect = updaterReg.create(null);
	public Updater<Boolean> setSingleTex = updaterReg.create(null);
	public Updater<Boolean> setPerFaceUV = updaterReg.create(null);
	public Updater<Boolean> setExtrudeEffect = updaterReg.create(null);
	public Updater<Integer> setAnimPriority = updaterReg.create(null);
	public Updater<Integer> setAnimOrder = updaterReg.create(null);
	public Updater<Boolean> displayViewport = updaterReg.create(true);
	public Updater<Boolean> setEnAddAnimTex = updaterReg.create(false);
	public Updater<Boolean> setCopyTransformEffect = updaterReg.create(null);
	public Updater<Boolean> setDisableVanillaEffect = updaterReg.create(null);

	public Updater<Integer> setPenColor = updaterReg.create();
	public Updater<String> setNameDisplay = updaterReg.create();
	public Updater<Void> updateGui = updaterReg.create();
	public Updater<String> setUndo = updaterReg.create();
	public Updater<String> setRedo = updaterReg.create();
	public Updater<Boolean> setSkinEdited = updaterReg.create();
	public Updater<String> setReload = updaterReg.create();
	public Updater<Boolean> setAnimPlay = updaterReg.create();
	public Updater<EditorAnim> setSelAnim = updaterReg.create();
	public Updater<Float> setValue = updaterReg.create();
	public Updater<Vec4f> setFaceUVs = updaterReg.create();
	public Updater<Rot> setFaceRot = updaterReg.create();
	public Updater<Boolean> setAutoUV = updaterReg.create();
	public Updater<Pair<Integer, String>> setInfoMsg = updaterReg.create();
	public TreeHandler<TreeElement> treeHandler = new TreeHandler<>(new ModelTree(this));
	public UpdaterWithValue<QuickTask> setQuickAction = updaterReg.createValue(null);

	public Supplier<Vec2i> cursorPos;
	public int penColor = 0xffffff;
	public UpdaterWithValue<EditorTool> drawMode = updaterReg.createValue(EditorTool.PEN);
	public int brushSize = 1;
	public int alphaValue = 255;
	public BooleanUpdater drawAllUVs = updaterReg.createBool(false);
	public BooleanUpdater onlyDrawOnSelected = updaterReg.createBool(true);
	public BooleanUpdater playVanillaAnims = updaterReg.createBool(true);
	public BooleanUpdater playAnimatedTex = updaterReg.createBool(true);
	public BooleanUpdater drawParrots = updaterReg.createBool(false);
	public boolean displayChat = true;
	public boolean displayAdvScaling = ModConfig.getCommonConfig().getBoolean(ConfigKeys.ADV_SCALING_SETTINGS, false);
	public BooleanUpdater forceHeldItemInAnim = updaterReg.createBool(false);
	public BooleanUpdater displayGizmo = updaterReg.createBool(true);
	public BooleanUpdater showOutlines = updaterReg.createBool(true);
	public EnumMap<ItemSlot, DisplayItem> handDisplay = new EnumMap<>(ItemSlot.class);
	public Set<PlayerModelLayer> modelDisplayLayers = new HashSet<>();
	public Map<String, Float> animTestSliders = new HashMap<>();
	public Set<VanillaPose> testPoses = EnumSet.noneOf(VanillaPose.class);
	public ScalingElement scalingElem = new ScalingElement(this);
	public UpdaterWithValue<Direction> perfaceFaceDir = updaterReg.createValue(Direction.UP);

	public ViewportCamera camera = new ViewportCamera();
	private Stack<Action> undoQueue = new Stack<>();
	private Stack<Action> redoQueue = new Stack<>();
	public BooleanUpdater renderBase = updaterReg.createBool(true);
	public boolean applyAnim;
	public boolean playFullAnim;
	public BooleanUpdater playerTpose = updaterReg.createBool(false);
	public boolean applyScaling;
	public BooleanUpdater drawBoundingBox = updaterReg.createBool(false);
	public long playStartTime;
	public AnimationEncodingData animEnc;
	public BooleanUpdater showPreviousFrame = updaterReg.createBool(true);

	public UI ui;
	private boolean initialized;
	public TreeElement selectedElement;
	public List<ModelElement> elements = new ArrayList<>();
	public EditorAnim selectedAnim;
	public List<Runnable> animsToPlay = new ArrayList<>();
	public IPose poseToApply;
	public IPose renderedPose;
	public List<EditorAnim> animations = new ArrayList<>();
	public List<EditorTemplate> templates = new ArrayList<>();
	public PartPosition leftHandPos = new PartPosition(), rightHandPos = new PartPosition();
	public TemplateSettings templateSettings;
	public TexturesElement texElem = new TexturesElement(this);
	public SkinType skinType;
	public boolean customSkinType;
	public ModelDescription description;
	public String modelId;
	public boolean hideHeadIfSkull, removeArmorOffset, removeBedOffset, enableInvisGlow;
	public Image vanillaSkin;
	public boolean dirty, autoSaveDirty;
	public long lastEdit;
	public EditorDefinition definition;
	public Map<TextureSheetType, ETextures> textures = new HashMap<>();
	public TextureProvider textureEditorBg = new TextureProvider(new Image(2, 2), new Vec2i(2, 2));
	public EditorTags tags = new EditorTags(this);
	public File file;
	public ProjectFile project = new ProjectFile();
	public int exportSize;

	public Editor() {
		this.definition = new EditorDefinition(this);
		textures.put(TextureSheetType.SKIN, new ETextures(this, TextureSheetType.SKIN, stitcher -> templates.forEach(e -> e.stitch(stitcher))));
	}

	public void setUI(UI ui) {
		this.ui = ui;
	}

	public void setVec(Vec3f v, VecType object) {
		if(selectedElement != null) {
			selectedElement.setVec(v, object);
		}
	}

	public void setValue(float value) {
		if(selectedElement != null) {
			action("set", "action.cpm.value").
			updateValueOp(selectedElement, selectedElement.getValue(), value, TreeElement::setValue).
			execute();
		}
	}

	public void setName(String name) {
		if(selectedElement != null) {
			action("set", "label.cpm.name").
			updateValueOp(selectedElement, selectedElement.getElemName(), name, TreeElement::setElemName).
			execute();
			treeHandler.update();
			updateGui.accept(null);
		}
	}

	public void switchMode() {
		if(selectedElement != null) {
			selectedElement.modeSwitch();
		}
	}

	public void setColor(int color) {
		updateValue(color, TreeElement::setElemColor);
	}

	public void setMcScale(float value) {
		updateValue(value, TreeElement::setMCScale);
	}

	public void addNew() {
		if(selectedElement != null) {
			selectedElement.addNew();
		}
	}

	public void deleteSel() {
		if(selectedElement != null) {
			selectedElement.delete();
		}
	}

	public void switchVis() {
		if(selectedElement != null)selectedElement.switchVis();
	}

	public void switchEffect(Effect e) {
		if(selectedElement != null)selectedElement.switchEffect(e);
	}

	public void switchLock() {
		ModelElement me = getSelectedElement();
		if (me != null) {
			me.locked = !me.locked;
			updateGui();
		}
	}

	public void setTexSize(int x, int y) {
		ETextures texs = getTextureProvider();
		if(texs != null) {
			EditorTexture tex = texs.provider;
			action("set", "action.cpm.texSize").
			updateValueOp(tex, tex.size.x, x, (a, b) -> a.size.x = b).
			updateValueOp(tex, tex.size.y, y, (a, b) -> a.size.y = b).
			onAction(texs::restitchTexture).
			onAction(this::markElementsDirty).
			execute();
		}
	}

	public void drawPixel(int x, int y, boolean isSkin) {
		switch (drawMode.get()) {
		case PEN:
			setPixel(x, y, penColor | (alphaValue << 24));
			break;

		case RUBBER:
			setPixel(x, y, 0);
			break;

		case FILL:
		{
			ETextures texs = getTextureProvider();
			if(texs != null && texs.isEditable()) {
				Box box = selectedElement != null ? selectedElement.getTextureBox() : null;
				if(box != null && onlyDrawOnSelected.get()) {
					if(!box.isInBounds(x, y))return;
				}
				Image img = texs.getImage();
				if(x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight())return;
				int old = img.getRGB(x, y);
				int fillColor = penColor | (alphaValue << 24);
				if(old == fillColor)return;
				if(!texs.isEdited())setSkinEdited.accept(true);
				Set<Vec2i> pixels = new HashSet<>();
				Stack<Vec2i> nextPixels = new Stack<>();
				nextPixels.add(new Vec2i(x, y));
				while(!nextPixels.empty()) {
					Vec2i p = nextPixels.pop();
					if(pixels.contains(p) || p.x < 0 || p.y < 0 || p.x >= img.getWidth() || p.y >= img.getHeight())continue;
					int color = img.getRGB(p.x, p.y);
					if(color == old || ((old & 0xff000000) == 0) && ((color & 0xff000000) == 0)) {
						if(box != null && onlyDrawOnSelected.get()) {
							if(!box.isInBounds(p.x, p.y))continue;
						}
						pixels.add(p);
						nextPixels.add(new Vec2i(p.x - 1, p.y));
						nextPixels.add(new Vec2i(p.x + 1, p.y));
						nextPixels.add(new Vec2i(p.x, p.y - 1));
						nextPixels.add(new Vec2i(p.x, p.y + 1));
					}
				}
				action("bucketFill").
				onRun(() -> pixels.forEach(p -> texs.setRGB(p.x, p.y, fillColor))).
				onUndo(() -> pixels.forEach(p -> texs.setRGB(p.x, p.y, old))).
				onAction(texs::refreshTexture).
				execute();
			}
		}
		break;

		default:
			break;
		}
	}

	private void setPixel(int x, int y, int color) {
		ETextures texs = getTextureProvider();
		if(texs != null && texs.isEditable()) {
			Box box = selectedElement != null ? selectedElement.getTextureBox() : null;
			if(box != null && onlyDrawOnSelected.get()) {
				if(!box.isInBounds(x, y))return;
			}
			Image img = texs.getImage();
			if(x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight())return;
			int old = img.getRGB(x, y);
			if(old == color)return;
			if(!texs.isEdited())setSkinEdited.accept(true);
			action("draw").
			onUndo(() -> texs.setRGB(x, y, old)).
			onRun(() -> texs.setRGB(x, y, color)).
			onAction(texs::refreshTexture).
			execute();
		}
	}

	public void markDirty() {
		markDirty0();
		redoQueue.clear();
		setUndo.accept(undoQueue.empty() ? null : undoQueue.peek().getName());
		setRedo.accept(null);
		setQuickAction.accept(null);
	}

	private void markDirty0() {
		setNameDisplay.accept((file == null ? ui.i18nFormat("label.cpm.new_project") : file.getName()) + "*");
		dirty = true;
		if(!autoSaveDirty)lastEdit = System.currentTimeMillis();
		autoSaveDirty = true;
	}

	public void updateGui() {
		updaterReg.setDefault();
		applyScaling = false;

		if(templateSettings != null) {
			templateSettings.templateArgs.forEach(TemplateArgHandler::applyToModel);
		}
		templates.forEach(EditorTemplate::applyToModel);
		if(selectedElement != null)selectedElement.updateGui();
		setNameDisplay.accept((file == null ? ui.i18nFormat("label.cpm.new_project") : file.getName()) + (dirty ? "*" : ""));
		setUndo.accept(undoQueue.empty() ? null : undoQueue.peek().getName());
		setRedo.accept(redoQueue.empty() ? null : redoQueue.peek().getName());
		if(selectedAnim != null)selectedAnim.updateGui();
		setSelAnim.accept(selectedAnim);
		ETextures tex = getTextureProvider();
		setSkinEdited.accept(tex != null ? tex.isEdited() : false);
		setReload.accept(tex != null && tex.file != null ? tex.file.getName() : null);
		treeHandler.update();
		updateGui.accept(null);
	}

	public void loadDefaultPlayerModel() {
		project = new ProjectFile();
		elements.clear();
		animations.clear();
		templates.clear();
		templateSettings = null;
		textures.values().forEach(ETextures::free);
		ETextures skinTex = textures.get(TextureSheetType.SKIN);
		textures.clear();
		skinTex.clean();
		textures.put(TextureSheetType.SKIN, skinTex);
		undoQueue.clear();
		redoQueue.clear();
		leftHandPos = new PartPosition();
		rightHandPos = new PartPosition();
		try {
			skinType = MinecraftClientAccess.get().getSkinType();
		} catch (NullPointerException e) {
			ui.onGuiException(ui.i18nFormat("error.cpm.corruptedInstall"), e, true);
			return;
		}
		Image skin = skinType.getSkinTexture();
		this.vanillaSkin = skin;
		skinTex.setDefaultImg(vanillaSkin);
		customSkinType = false;
		skinTex.setImage(new Image(skin));
		skinTex.provider.size = new Vec2i(skin.getWidth(), skin.getHeight());
		dirty = false;
		autoSaveDirty = false;
		file = null;
		selectedElement = null;
		selectedAnim = null;
		animEnc = null;
		description = null;
		scalingElem.reset();
		removeArmorOffset = true;
		hideHeadIfSkull = true;
		removeBedOffset = false;
		enableInvisGlow = false;
		modelId = null;
		tags.clear();
		Player<?> profile = MinecraftClientAccess.get().getClientPlayer();
		profile.getTextures().load().thenRun(() -> {
			if(!customSkinType)skinType = profile.getSkinType();
			CompletableFuture<Image> img = profile.getTextures().getTexture(TextureType.SKIN);
			this.vanillaSkin = skinType.getSkinTexture();
			skinTex.setDefaultImg(vanillaSkin);
			img.thenAccept(s -> {
				if(s != null) {
					this.vanillaSkin = s;
				}
				if(!skinTex.isEdited()) {
					skinTex.provider.size = new Vec2i(vanillaSkin.getWidth(), vanillaSkin.getHeight());
					skinTex.setDefaultImg(vanillaSkin);
					skinTex.setImage(new Image(this.vanillaSkin));
					skinTex.setChangedLocally(false);
					restitchTextures();
				}
			});
		});
		for(PlayerModelParts type : PlayerModelParts.values()) {
			if(type != PlayerModelParts.CUSTOM_PART)
				elements.add(new ModelElement(this, ElementType.ROOT_PART, type));
		}
		restitchTextures();
	}

	public void preRender() {
		definition.preRender();
		elements.forEach(ModelElement::preRender);
		applyAnimations();
		elements.forEach(ModelElement::postRender);
		if(playAnimatedTex.get())textures.values().forEach(ETextures::updateAnim);
	}

	public void applyAnimations() {
		if(this.applyAnim && this.selectedAnim != null) {
			if(this.playFullAnim) {
				long playTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
				long currentStep = (playTime - this.playStartTime);
				this.selectedAnim.animate(currentStep, definition, AnimationMode.PLAYER);
				if(currentStep > this.selectedAnim.duration && !this.selectedAnim.loop && this.selectedAnim.pose == null){
					this.playFullAnim = false;
					setAnimPlay.accept(false);
				}
			} else {
				this.selectedAnim.apply();
			}
		} else if(this.applyAnim && !animsToPlay.isEmpty()) {
			for (Runnable anim : animsToPlay) {
				anim.run();
			}
			animsToPlay.clear();
		}
	}

	private CompletableFuture<Void> save0(File file) {
		try {
			StoreIDGen storeIDgen = new StoreIDGen();
			walkElements(elements, storeIDgen::setID);
			ProjectIO.saveProject(this, project);
		} catch (IOException e) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(e);
			return f;
		}
		return project.save(file);
	}

	public CompletableFuture<Void> save(File file) {
		setInfoMsg.accept(Pair.of(200000, ui.i18nFormat("tooltip.cpm.saving", file.getName())));
		return save0(file).thenRunAsync(() -> {
			this.file = file;
			dirty = false;
			autoSaveDirty = false;
			setInfoMsg.accept(Pair.of(2000, ui.i18nFormat("tooltip.cpm.saveSuccess", file.getName())));
			updateGui();
		}, ui::executeLater);
	}

	public CompletableFuture<Void> load(File file) {
		setInfoMsg.accept(Pair.of(200000, ui.i18nFormat("tooltip.cpm.loading", file.getName())));
		loadDefaultPlayerModel();
		return project.load(file).thenComposeAsync(v -> {
			try {
				ProjectIO.loadProject(this, project);
			} catch (Exception e) {
				System.out.println("Err:" + e);
				CompletableFuture<Void> f = new CompletableFuture<>();
				f.completeExceptionally(e);
				return f;
			}
			this.file = file;
			restitchTextures();
			updateGui();
			setInfoMsg.accept(Pair.of(2000, ui.i18nFormat("tooltip.cpm.loadSuccess", file.getName())));
			return CompletableFuture.completedFuture(null);
		}, ui::executeLater);
	}

	public void reloadSkin() {
		ETextures tex = getTextureProvider();
		if(tex != null && tex.file != null) {
			if (tex.isChangedLocally()) {
				ui.displayConfirm(ui.i18nFormat("label.cpm.reloadQuestion"), () -> {
					ActionBuilder ab = action("loadTexture");
					reloadSkin(ab, tex, tex.file);
				}, null);
			} else {
				ActionBuilder ab = action("loadTexture");
				reloadSkin(ab, tex, tex.file);
			}
		}
		markDirty();
	}

	public void reloadSkin(ActionBuilder ab, ETextures tex, File file) {
		Image.loadFrom(file).thenAcceptAsync(img -> {
			if(img.getWidth() > 8192 || img.getHeight() > 8192) {
				ui.displayMessagePopup(ui.i18nFormat("label.cpm.error"), ui.i18nFormat("error.cpm.img_load_failed", ui.i18nFormat("label.cpm.tex_size_too_big", 8192)));
				return;
			}
			ab.updateValueOp(tex, tex.getImage(), img, ETextures::setImage).
			updateValueOp(tex, tex.isEdited(), true, ETextures::setEdited).
			updateValueOp(tex, tex.isChangedLocally(), false, ETextures::setChangedLocally);
			if(!tex.customGridSize) {
				ab.updateValueOp(tex, tex.provider.size.x, img.getWidth(), (a, b) -> a.provider.size.x = b).
				updateValueOp(tex, tex.provider.size.y, img.getHeight(), (a, b) -> a.provider.size.y = b).
				onAction(this::markElementsDirty);
			}
			ab.onAction(tex::restitchTexture);
			ab.execute();
			setSkinEdited.accept(true);
		}, ui::executeLater).exceptionally(e -> {
			Log.error("Failed to load image", e);
			ui.displayMessagePopup(ui.i18nFormat("label.cpm.error"), ui.i18nFormat("error.cpm.img_load_failed", e.getLocalizedMessage()));
			return null;
		});
	}

	public void saveSkin(File f) {
		ETextures tex = getTextureProvider();
		if(tex != null) {
			try {
				tex.getImage().storeTo(f);
				tex.file = f;
				updateGui();
			} catch (IOException e) {
				Log.error("Failed to save image", e);
				ui.displayMessagePopup(ui.i18nFormat("label.cpm.error"), ui.i18nFormat("error.cpm.img_save_failed", e.getLocalizedMessage()));
			}
		}
	}

	public ActionBuilder action(String name) {
		return new ActionBuilder(this, ui.i18nFormat("action.cpm." + name));
	}

	public ActionBuilder action(String name, String arg) {
		return new ActionBuilder(this, ui.i18nFormat("action.cpm." + name, ui.i18nFormat(arg)));
	}

	public void executeAction(Action a) {
		undoQueue.add(a);
		a.run();
	}

	public void undo() {
		if(undoQueue.empty())return;
		Action r = undoQueue.pop();
		if(r != null) {
			redoQueue.add(r);
			r.undo();
		}
		markDirty0();
		updateGui();
	}

	public void redo() {
		if(redoQueue.empty())return;
		Action r = redoQueue.pop();
		if(r != null) {
			undoQueue.add(r);
			r.run();
		}
		markDirty0();
		updateGui();
	}

	public <T> void updateValue(T value, BiConsumer<TreeElement, T> func) {
		if(selectedElement != null)func.accept(selectedElement, value);
	}

	public void moveElement(ModelElement element, ModelElement to) {
		if(checkChild(element.children, to))return;
		action("move", "action.cpm.cube").
		addToList(to.children, element).
		removeFromList(element.parent.children, element).
		updateValueOp(element, element.parent, to, (a, b) -> a.parent = b).
		execute();
		selectedElement = null;
		updateGui();
	}

	private boolean checkChild(List<ModelElement> elem, ModelElement to) {
		for (ModelElement modelElement : elem) {
			if(modelElement == to)return true;
			if(checkChild(modelElement.children, to))return true;
		}
		return false;
	}

	public static void walkElements(List<ModelElement> elem, Consumer<ModelElement> c) {
		for (ModelElement modelElement : elem) {
			c.accept(modelElement);
			walkElements(modelElement.children, c);
		}
	}

	public void setAnimRot(Vec3f v) {
		if(selectedAnim != null) {
			selectedAnim.setRotation(v);
		}
	}

	public void setAnimPos(Vec3f v) {
		if(selectedAnim != null) {
			selectedAnim.setPosition(v);
		}
	}

	public void setAnimScale(Vec3f v) {
		if(selectedAnim != null) {
			selectedAnim.setScale(v);
		}
	}

	public void addNewAnim(AnimationProperties prop) {
		String fn = AnimationsLoaderV1.getFileName(prop.pose, prop.displayName);
		EditorAnim anim = new EditorAnim(this, fn, prop.type, true);
		ActionBuilder ab = action("add", "action.cpm.anim");
		anim.setProperties(prop, ab);
		ab.addToList(animations, anim).onUndo(() -> selectedAnim = null).execute();
		selectedAnim = anim;
		updateGui();
	}

	public void editAnim(AnimationProperties prop) {
		if(selectedAnim != null) {
			boolean add = selectedAnim.add;
			ActionBuilder ab = action("edit", "action.cpm.anim");
			selectedAnim.setProperties(prop, ab);
			ab.onAction(selectedAnim, EditorAnim::clearCache).
			execute();
			updateGui();
			if(add != prop.add) {
				setQuickAction.accept(new QuickTask(ui.i18nFormat("button.cpm.fixAdditiveToggle"), ui.i18nFormat("tooltip.cpm.fixAdditiveToggle"), () -> Generators.fixAdditive(this)));
			}
		}
	}

	public void delSelectedAnim() {
		if(selectedAnim != null) {
			EditorAnim anim = selectedAnim;
			action("remove", "action.cpm.anim").removeFromList(animations, anim).onRun(() -> selectedAnim = null).execute();
			selectedAnim = null;
			updateGui();
		}
	}

	public void addNewAnimFrame() {
		addNewAnimFrame(true);
	}

	public void addNewAnimFrame(boolean copyCurrent) {
		if(selectedAnim != null) {
			selectedAnim.addFrame(copyCurrent);
			updateGui();
		}
	}

	public void delSelectedAnimFrame() {
		if(selectedAnim != null) {
			selectedAnim.deleteFrame();
			updateGui();
		}
	}

	public void setAnimDuration(int value) {
		if(selectedAnim == null)return;
		action("setAnim", "label.cpm.duration").
		updateValueOp(selectedAnim, selectedAnim.duration, value, 1, FormatLimits.getAnimLenLimit(), (a, b) -> a.duration = b, setAnimDuration).
		execute();
	}

	public void setAnimPriority(int value) {
		if(selectedAnim == null)return;
		action("setAnim", "label.cpm.anim_priority").
		updateValueOp(selectedAnim, selectedAnim.priority, value, FormatLimits.getAnimSortLimitsMin(), FormatLimits.getAnimSortLimitsMax(), (a, b) -> a.priority = b, setAnimPriority).
		execute();
	}

	public void setAnimOrder(int value) {
		if(selectedAnim == null)return;
		action("setAnim", "label.cpm.anim_order").
		updateValueOp(selectedAnim, selectedAnim.order, value, FormatLimits.getAnimSortLimitsMin(), FormatLimits.getAnimSortLimitsMax(), (a, b) -> a.order = b, setAnimOrder).
		execute();
	}

	public void animPrevFrm() {
		if(selectedAnim != null) {
			selectedAnim.prevFrame();
			updateGui();
		}
	}

	public void animNextFrm() {
		if(selectedAnim != null) {
			selectedAnim.nextFrame();
			updateGui();
		}
	}

	public void setAnimColor(int rgb) {
		if(selectedAnim != null) {
			selectedAnim.setColor(rgb);
			updateGui();
		}
	}

	public void delSelectedAnimPartData(boolean all) {
		if(selectedAnim != null) {
			selectedAnim.clearSelectedData(all);
			updateGui();
		}
	}

	public void switchAnimShow() {
		if(selectedAnim != null) {
			selectedAnim.switchVisible();
			updateGui();
		}
	}

	public void applyRenderPoseForAnim(Consumer<VanillaPose> func) {
		if(applyAnim && selectedAnim != null && selectedAnim.pose instanceof VanillaPose) {
			func.accept((VanillaPose) selectedAnim.pose);
		} else if(applyAnim && poseToApply != null && poseToApply instanceof VanillaPose) {
			func.accept((VanillaPose) poseToApply);
		}
		renderedPose = poseToApply;
		poseToApply = null;
	}

	public boolean hasVanillaParts() {
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					if(!el.hidden)return true;
				}
			}
		}
		return false;
	}

	public ETextures getTextureProvider() {
		return selectedElement != null ? selectedElement.getTexture() : textures.get(TextureSheetType.SKIN);
	}

	public ModelElement getSelectedElement() {
		if(selectedElement instanceof TreeSettingElement) {
			TreeSettingElement e = (TreeSettingElement) selectedElement;
			if(e.getParent() instanceof ModelElement)return (ModelElement) e.getParent();
			else return null;
		}
		return selectedElement instanceof ModelElement ? (ModelElement) selectedElement : null;
	}

	public void forEachSeletectedElement(Consumer<TreeElement> el) {
		TreeElement elem = selectedElement;
		if(selectedElement instanceof TreeSettingElement) {
			elem = ((TreeSettingElement) selectedElement).getParent();
		}
		if(elem instanceof MultiSelector) {
			((MultiSelector)elem).forEachSelected(el);
		} else {
			el.accept(elem);
		}
	}

	public void free() {
		textures.values().forEach(ETextures::free);
		definition.cleanup();
		textureEditorBg.free();
	}

	public void restitchTextures() {
		textures.values().forEach(ETextures::restitchTexture);
		if(textures.get(TextureSheetType.SKIN).hasStitches() && hasVanillaParts()) {
			Generators.convertModel(this);
		}
	}

	public void tick() {
		int autosave = ModConfig.getCommonConfig().getInt(ConfigKeys.EDITOR_AUTOSAVE_TIME, 5 * 60);
		if(autoSaveDirty && autosave > 0 && lastEdit + autosave * 1000 < System.currentTimeMillis()) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			File autosaves = new File(modelsDir, "autosaves");
			autosaves.mkdirs();
			File file = new File(autosaves, String.format("autosave-%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS-", System.currentTimeMillis()) + (this.file == null ? ui.i18nFormat("label.cpm.new_project") : this.file.getName()));
			Log.info("Editor autosave: " + file.getName());
			setInfoMsg.accept(Pair.of(5000, ui.i18nFormat("tooltip.cpm.autosaving", file.getName())));
			autoSaveDirty = false;
			save0(file).handleAsync((v, e) -> {
				if(e != null) {
					ui.onGuiException("Failed to autosave", e, false);
				} else {
					setInfoMsg.accept(Pair.of(2000, ui.i18nFormat("tooltip.cpm.autoSaveSuccess", file.getName())));
				}
				return null;
			}, ui::executeLater);
		}
	}

	public void addRoot(RootGroups group) {
		List<ModelElement> elems = new ArrayList<>();
		for (int i = 0; i < group.types.length; i++) {
			RootModelType type = group.types[i];
			if(elements.stream().noneMatch(e -> e.type == ElementType.ROOT_PART && e.typeData == type)) {
				ModelElement e = new ModelElement(this, ElementType.ROOT_PART, type);
				elems.add(e);
			}
		}
		ActionBuilder ab = action("add", "action.cpm.root");
		elems.forEach(e -> ab.addToList(elements, e));
		ab.onUndo(() -> selectedElement = null);
		Generators.loadTextures(this, group, (a, b) -> ab.addToMap(textures, a, b));
		ab.execute();
		updateGui();
	}

	public void render(MatrixStack stack, VBuffers buf, ViewportPanel panel) {
		if(drawBoundingBox.get()) {
			RenderUtil.renderBounds(stack, buf.getBuffer(panel.getRenderTypes(), RenderMode.OUTLINE), getRenderedPose(), applyScaling, scalingElem);
		}
	}

	public VanillaPose getRenderedPose() {
		VanillaPose pose = null;
		if(applyAnim && selectedAnim != null && selectedAnim.pose instanceof VanillaPose) {
			pose = (VanillaPose) selectedAnim.pose;
		} else if(applyAnim && renderedPose != null && renderedPose instanceof VanillaPose) {
			pose = (VanillaPose) renderedPose;
		}
		return pose;
	}

	public void animMoveFrame(int i) {
		if(selectedAnim != null) {
			selectedAnim.moveFrame(i);
			updateGui();
		}
	}

	public void addAnimTex() {
		if(selectedElement != null) {
			ETextures tex = selectedElement.getTexture();
			if(tex != null)tex.addAnimTex();
		}
	}

	public void refreshCaches() {
		restitchTextures();
		animations.forEach(EditorAnim::clearCache);
		markElementsDirty();
	}

	public void markElementsDirty() {
		walkElements(elements, ModelElement::markDirty);
	}

	public void saveRecovered() throws Exception {
		File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
		File autosaves = new File(modelsDir, "autosaves");
		autosaves.mkdirs();
		File file = new File(autosaves, String.format("recovered-%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS-", System.currentTimeMillis()) + (this.file == null ? ui.i18nFormat("label.cpm.new_project") : this.file.getName()));
		save0(file).thenRun(() -> {
			ModConfig.getCommonConfig().setString(ConfigKeys.REOPEN_PROJECT, file.getAbsolutePath());
			ModConfig.getCommonConfig().save();
			Log.info("Saved recovery project: " + file.getName());
		});
	}

	public static void walkFromRoot(ModelElement from, Consumer<ModelElement> c) {
		Deque<ModelElement> list = new LinkedList<>();
		while(from != null) {
			list.addFirst(from);
			from = from.parent;
		}
		list.forEach(c);
	}

	public void reinit() {
		updaterReg.reset();
		if (!initialized) {
			initialized = true;
			loadDefaultPlayerModel();
		}
	}
}
