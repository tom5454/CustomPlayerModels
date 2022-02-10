package com.tom.cpm.shared.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.UpdaterRegistry;
import com.tom.cpl.gui.UpdaterRegistry.Updater;
import com.tom.cpl.gui.elements.MessagePopup;
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
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.editor.actions.Action;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimatedTex;
import com.tom.cpm.shared.editor.anim.AnimationEncodingData;
import com.tom.cpm.shared.editor.anim.AnimationType;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplayType;
import com.tom.cpm.shared.editor.gui.RenderUtil;
import com.tom.cpm.shared.editor.gui.ViewportPanel;
import com.tom.cpm.shared.editor.project.ProjectFile;
import com.tom.cpm.shared.editor.project.ProjectIO;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.editor.tree.ScalingElement;
import com.tom.cpm.shared.editor.tree.TexturesElement;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.TreeElement.ModelTree;
import com.tom.cpm.shared.editor.tree.TreeElement.VecType;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.StoreIDGen;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class Editor {
	public UpdaterRegistry updaterReg = new UpdaterRegistry();
	public Updater<Vec3f> setOffset = updaterReg.create();
	public Updater<Vec3f> setRot = updaterReg.create();
	public Updater<Vec3f> setPosition = updaterReg.create();
	public Updater<Vec3f> setSize = updaterReg.create();
	public Updater<Vec3f> setScale = updaterReg.create();
	public Updater<Float> setMCScale = updaterReg.create();
	public Updater<Boolean> setMirror = updaterReg.create();
	public Updater<String> updateName = updaterReg.create();
	public Updater<String> setModeBtn = updaterReg.create();
	public Updater<ModeDisplayType> setModePanel = updaterReg.create();
	public Updater<Vec3i> setTexturePanel = updaterReg.create();
	public Updater<Boolean> setVis = updaterReg.create();
	public Updater<Boolean> setDelEn = updaterReg.create();
	public Updater<Boolean> setAddEn = updaterReg.create();
	public Updater<String> setNameDisplay = updaterReg.create();
	public Updater<Void> updateGui = updaterReg.create();
	public Updater<String> setUndo = updaterReg.create();
	public Updater<String> setRedo = updaterReg.create();
	public Updater<Boolean> setGlow = updaterReg.create();
	public Updater<Integer> setPenColor = updaterReg.create();
	public Updater<Integer> setPartColor = updaterReg.create();
	public Updater<Boolean> setReColor = updaterReg.create();
	public Updater<Integer> setAnimFrame = updaterReg.create();
	public Updater<Vec3f> setAnimPos = updaterReg.create();
	public Updater<Vec3f> setAnimRot = updaterReg.create();
	public Updater<Boolean> setFrameAddEn = updaterReg.create();
	public Updater<Boolean> setAnimDelEn = updaterReg.create();
	public Updater<Boolean> setFrameDelEn = updaterReg.create();
	public Updater<Integer> setAnimDuration = updaterReg.create();
	public Updater<Integer> setAnimColor = updaterReg.create();
	public Updater<Boolean> setAnimShow = updaterReg.create();
	public Updater<Boolean> setAnimPlayEn = updaterReg.create();
	public Updater<EditorAnim> setSelAnim = updaterReg.create();
	public Updater<Boolean> setHiddenEffect = updaterReg.create();
	public Updater<Boolean> setSingleTex = updaterReg.create();
	public Updater<Boolean> setPerFaceUV = updaterReg.create();
	public Updater<Boolean> setExtrudeEffect = updaterReg.create();
	public Updater<Boolean> setSkinEdited = updaterReg.create();
	public Updater<String> setReload = updaterReg.create();
	public Updater<Boolean> setAnimPlay = updaterReg.create();
	public Updater<Integer> setAnimPriority = updaterReg.create();
	public Updater<Void> gestureFinished = updaterReg.create();
	public Updater<Boolean> displayViewport = updaterReg.create();
	public Updater<Float> setValue = updaterReg.create();
	public Updater<EditorTool> setTool = updaterReg.create();
	public Updater<Vec4f> setFaceUVs = updaterReg.create();
	public Updater<Rot> setFaceRot = updaterReg.create();
	public Updater<Boolean> setAutoUV = updaterReg.create();
	public Updater<Boolean> setEnAddAnimTex = updaterReg.create();
	public Updater<Pair<Integer, String>> setInfoMsg = updaterReg.create();
	public TreeHandler<TreeElement> treeHandler = new TreeHandler<>(new ModelTree(this));

	public Supplier<Vec2i> cursorPos;
	public int penColor = 0xffffff;
	public EditorTool drawMode = EditorTool.PEN;
	public int brushSize = 1;
	public int alphaValue = 255;
	public boolean drawAllUVs = false;
	public boolean onlyDrawOnSelected = true;
	public boolean playVanillaAnims = true;
	public boolean playAnimatedTex = true;
	public boolean drawParrots = false;
	public boolean displayChat = true;
	public EnumMap<ItemSlot, DisplayItem> handDisplay = new EnumMap<>(ItemSlot.class);
	public Set<PlayerModelLayer> modelDisplayLayers = new HashSet<>();
	public float animTestSlider;
	public Set<VanillaPose> testPoses = EnumSet.noneOf(VanillaPose.class);
	public ScalingElement scalingElem = new ScalingElement(this);
	public Direction perfaceFaceDir = Direction.UP;

	public ViewportCamera camera = new ViewportCamera();
	private Stack<Action> undoQueue = new Stack<>();
	private Stack<Action> redoQueue = new Stack<>();
	public boolean renderPaint;
	public boolean renderBase = true;
	public boolean applyAnim;
	public boolean playFullAnim;
	public boolean playerTpose;
	public boolean applyScaling;
	public boolean drawBoundingBox;
	public long playStartTime, gestureStartTime;
	public StoreIDGen storeIDgen;
	public AnimationEncodingData animEnc;

	public Frame frame;
	public TreeElement selectedElement;
	public List<ModelElement> elements = new ArrayList<>();
	public EditorAnim selectedAnim;
	public List<EditorAnim> animsToPlay = new ArrayList<>();
	public IPose poseToApply;
	public IPose renderedPose;
	public List<EditorAnim> animations = new ArrayList<>();
	public List<EditorTemplate> templates = new ArrayList<>();
	public TemplateSettings templateSettings;
	public TexturesElement texElem = new TexturesElement(this);
	public SkinType skinType;
	public boolean customSkinType;
	public ModelDescription description;
	public boolean hideHeadIfSkull, removeArmorOffset;
	public Image vanillaSkin;
	public boolean dirty, autoSaveDirty;
	public long lastEdit;
	public EditorDefinition definition;
	public Map<TextureSheetType, ETextures> textures = new HashMap<>();
	public File file;
	public ProjectFile project = new ProjectFile();
	public int exportSize;

	public Editor() {
		this.definition = new EditorDefinition(this);
		textures.put(TextureSheetType.SKIN, new ETextures(this, TextureSheetType.SKIN, stitcher -> templates.forEach(e -> e.stitch(stitcher))));
	}

	public void setGui(EditorGui gui) {
		this.frame = gui;
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

	public void switchMirror() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.MIRROR);
	}

	public void switchGlow() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.GLOW);
	}

	public void switchSingleTex() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.SINGLE_TEX);
	}

	public void switchPerfaceUV() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.PER_FACE_UV);
	}

	public void switchHide() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.HIDE);
	}

	public void switchExtrude() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.EXTRUDE);
	}

	public void setTexSize(int x, int y) {
		ETextures texs = getTextureProvider();
		if(texs != null) {
			EditorTexture tex = texs.provider;
			action("set", "action.cpm.texSize").
			updateValueOp(tex, tex.size.x, x, (a, b) -> a.size.x = b).
			updateValueOp(tex, tex.size.y, y, (a, b) -> a.size.y = b).
			onRun(texs::restitchTexture).
			execute();
		}
	}

	public void switchReColorEffect() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.RECOLOR);
	}

	public void drawPixel(int x, int y, boolean isSkin) {
		switch (drawMode) {
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
				if(box != null && onlyDrawOnSelected) {
					if(!box.isInBounds(x, y))return;
				}
				Image img = texs.getImage();
				if(x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight())return;
				int old = img.getRGB(x, y);
				if((old & 0xffffff) == penColor)return;
				if(!texs.isEdited())setSkinEdited.accept(true);
				Set<Vec2i> pixels = new HashSet<>();
				Stack<Vec2i> nextPixels = new Stack<>();
				nextPixels.add(new Vec2i(x, y));
				while(!nextPixels.empty()) {
					Vec2i p = nextPixels.pop();
					if(pixels.contains(p) || p.x < 0 || p.y < 0 || p.x >= img.getWidth() || p.y >= img.getHeight())continue;
					int color = img.getRGB(p.x, p.y);
					if(color == old) {
						if(box != null && onlyDrawOnSelected) {
							if(!box.isInBounds(p.x, p.y))continue;
						}
						pixels.add(p);
						nextPixels.add(new Vec2i(p.x - 1, p.y));
						nextPixels.add(new Vec2i(p.x + 1, p.y));
						nextPixels.add(new Vec2i(p.x, p.y - 1));
						nextPixels.add(new Vec2i(p.x, p.y + 1));
					}
				}
				int color = penColor | (alphaValue << 24);
				action("bucketFill").
				onRun(() -> pixels.forEach(p -> texs.setRGB(p.x, p.y, color))).
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
			if(box != null && onlyDrawOnSelected) {
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
	}

	public void markDirty0() {
		setNameDisplay.accept((file == null ? frame.getGui().i18nFormat("label.cpm.new_project") : file.getName()) + "*");
		dirty = true;
		if(!autoSaveDirty)lastEdit = System.currentTimeMillis();
		autoSaveDirty = true;
	}

	public void updateGui() {
		setOffset.accept(null);
		setRot.accept(null);
		setPosition.accept(null);
		setSize.accept(null);
		setScale.accept(null);
		setMCScale.accept(null);
		setMirror.accept(null);
		updateName.accept(null);
		setModeBtn.accept(null);
		setModePanel.accept(ModeDisplayType.NULL);
		setTexturePanel.accept(null);
		setVis.accept(null);
		setDelEn.accept(false);
		setAddEn.accept(false);
		setGlow.accept(null);
		setPartColor.accept(null);
		setReColor.accept(null);
		setAnimPos.accept(null);
		setAnimRot.accept(null);
		setAnimFrame.accept(null);
		setAnimDelEn.accept(false);
		setFrameAddEn.accept(false);
		setFrameDelEn.accept(false);
		setAnimDuration.accept(null);
		setAnimColor.accept(null);
		setAnimShow.accept(null);
		setAnimPlayEn.accept(false);
		setHiddenEffect.accept(null);
		setSingleTex.accept(null);
		setExtrudeEffect.accept(null);
		setPerFaceUV.accept(null);
		setVis.accept(false);
		setAnimPriority.accept(null);
		displayViewport.accept(true);
		setEnAddAnimTex.accept(false);
		applyScaling = false;

		if(templateSettings != null) {
			templateSettings.templateArgs.forEach(TemplateArgHandler::applyToModel);
		}
		templates.forEach(EditorTemplate::applyToModel);
		if(selectedElement != null) {
			selectedElement.updateGui();
		}
		setNameDisplay.accept((file == null ? frame.getGui().i18nFormat("label.cpm.new_project") : file.getName()) + (dirty ? "*" : ""));
		setUndo.accept(undoQueue.empty() ? null : undoQueue.peek().getName());
		setRedo.accept(redoQueue.empty() ? null : redoQueue.peek().getName());
		if(selectedAnim != null) {
			AnimFrame selFrm = selectedAnim.getSelectedFrame();
			if(selFrm != null) {
				ModelElement selectedElement = getSelectedElement();
				setAnimFrame.accept(selectedAnim.getFrames().indexOf(selFrm));
				if(selectedElement != null) {
					IElem dt = selFrm.getData(selectedElement);
					if(dt == null) {
						if(selectedAnim.add) {
							setAnimPos.accept(new Vec3f());
							setAnimRot.accept(new Vec3f());
						} else if(selectedElement.type == ElementType.ROOT_PART){
							PartValues val = ((VanillaModelPart)selectedElement.typeData).getDefaultSize(skinType);
							setAnimPos.accept(val.getPos());
							setAnimRot.accept(new Vec3f());
						} else {
							setAnimPos.accept(selectedElement.pos);
							setAnimRot.accept(selectedElement.rotation);
						}
						if(!selectedElement.texture || selectedElement.recolor) {
							setAnimColor.accept(selectedElement.rgb);
						}
						if(selectedElement.type != ElementType.ROOT_PART && selectedElement.itemRenderer == null)
							setAnimShow.accept(!selectedElement.hidden);
					} else {
						if(!selectedElement.texture || selectedElement.recolor) {
							Vec3f c = dt.getColor();
							setAnimColor.accept((((int) c.x) << 16) | (((int) c.y) << 8) | ((int) c.z));
						}
						setAnimPos.accept(dt.getPosition());
						setAnimRot.accept(dt.getRotation());
						if(selectedElement.type != ElementType.ROOT_PART && selectedElement.itemRenderer == null)
							setAnimShow.accept(dt.isVisible());
					}
				}
				setFrameDelEn.accept(true);
			}
			setFrameAddEn.accept(true);
			setAnimDelEn.accept(true);
			if(!(selectedAnim.pose instanceof VanillaPose && ((VanillaPose)selectedAnim.pose).hasStateGetter()))
				setAnimDuration.accept(selectedAnim.duration);
			setAnimPlayEn.accept(selectedAnim.getFrames().size() > 1);
			setAnimPriority.accept(selectedAnim.priority);
		}
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
		skinType = MinecraftClientAccess.get().getSkinType();
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
		storeIDgen = new StoreIDGen();
		Player<?, ?> profile = MinecraftClientAccess.get().getClientPlayer();
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
					restitchTextures();
				}
			});
		});
		for(PlayerModelParts type : PlayerModelParts.values()) {
			if(type != PlayerModelParts.CUSTOM_PART)
				elements.add(new ModelElement(this, ElementType.ROOT_PART, type, frame.getGui()));
		}
		restitchTextures();
	}

	public void preRender() {
		definition.itemTransforms.clear();
		elements.forEach(ModelElement::preRender);
		applyAnimations();
		if(playAnimatedTex)textures.values().forEach(ETextures::updateAnim);
	}

	public void applyAnimations() {
		if(this.applyAnim && this.selectedAnim != null) {
			if(this.playFullAnim) {
				long playTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
				long currentStep = (playTime - this.playStartTime);
				this.selectedAnim.applyPlay(currentStep);
				if(currentStep > this.selectedAnim.duration && !this.selectedAnim.loop && this.selectedAnim.pose == null){
					this.playFullAnim = false;
					setAnimPlay.accept(false);
				}
			} else {
				this.selectedAnim.apply();
			}
		} else if(this.applyAnim && !animsToPlay.isEmpty()) {
			animsToPlay.sort((a, b) -> Integer.compare(a.priority, b.priority));
			for (EditorAnim anim : animsToPlay) {
				long playTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
				long currentStep = (playTime - (anim.pose == null ? this.gestureStartTime : this.playStartTime));
				anim.applyPlay(currentStep);
				if(currentStep > anim.duration && !anim.loop && anim.pose == null){
					gestureFinished.accept(null);
				}
			}
			animsToPlay.clear();
		}
	}

	private void save0(File file) throws IOException {
		ProjectIO.saveProject(this, project);
		project.save(file);
	}

	public void save(File file) throws IOException {
		setInfoMsg.accept(Pair.of(200000, gui().i18nFormat("tooltip.cpm.saving", file.getName())));
		save0(file);
		this.file = file;
		dirty = false;
		autoSaveDirty = false;
		setInfoMsg.accept(Pair.of(2000, gui().i18nFormat("tooltip.cpm.saveSuccess", file.getName())));
		updateGui();
	}

	public void load(File file) throws IOException {
		loadDefaultPlayerModel();
		project.load(file);
		ProjectIO.loadProject(this, project);
		this.file = file;
		restitchTextures();
		updateGui();
	}

	public void reloadSkin() {
		ETextures tex = getTextureProvider();
		if(tex != null && tex.file != null) {
			try {
				Image img = Image.loadFrom(tex.file);
				if(img.getWidth() > 512 || img.getHeight() > 512)
					throw new IOException(frame.getGui().i18nFormat("label.cpm.tex_size_too_big", 512));
				tex.setImage(img);
				tex.setEdited(true);
				setSkinEdited.accept(true);
				tex.restitchTexture();
			} catch (IOException e) {
				Log.error("Failed to load image", e);
				frame.openPopup(new MessagePopup(frame, frame.getGui().i18nFormat("label.cpm.error"), frame.getGui().i18nFormat("error.cpm.img_load_failed", e.getLocalizedMessage())));
			}
		}
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
				frame.openPopup(new MessagePopup(frame, frame.getGui().i18nFormat("label.cpm.error"), frame.getGui().i18nFormat("error.cpm.img_save_failed", e.getLocalizedMessage())));
			}
		}
	}

	public ActionBuilder action(String name) {
		return new ActionBuilder(this, gui().i18nFormat("action.cpm." + name));
	}

	public ActionBuilder action(String name, String arg) {
		return new ActionBuilder(this, gui().i18nFormat("action.cpm." + name, gui().i18nFormat(arg)));
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
			if(v.x < 0 || v.x > 360 || v.y < 0 || v.y > 360 || v.z < 0 || v.z > 360) {
				while(v.x < 0)  v.x += 360;
				while(v.x > 360)v.x -= 360;
				while(v.y < 0)  v.y += 360;
				while(v.y > 360)v.y -= 360;
				while(v.z < 0)  v.z += 360;
				while(v.z > 360)v.z -= 360;
				setAnimRot.accept(v);
			}
			selectedAnim.setRotation(v);
		}
	}

	public void setAnimPos(Vec3f v) {
		if(selectedAnim != null) {
			selectedAnim.setPosition(v);
		}
	}

	public void addNewAnim(IPose pose, String displayName, boolean add, boolean loop, InterpolatorType it) {
		String fname = null;
		AnimationType type;
		if(pose instanceof VanillaPose) {
			fname = "v_" + ((VanillaPose)pose).name().toLowerCase() + "_" + displayName.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
			type = AnimationType.POSE;
		} else if(pose != null) {
			fname = "c_" + ((CustomPose) pose).getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
			type = AnimationType.POSE;
		} else {
			fname = "g_" + displayName.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
			type = AnimationType.GESTURE;
		}
		EditorAnim anim = new EditorAnim(this, fname, type, true);
		anim.pose = pose;
		anim.add = add;
		anim.loop = loop;
		anim.displayName = displayName;
		anim.intType = it;
		action("add", "action.cpm.anim").addToList(animations, anim).onUndo(() -> selectedAnim = null).execute();
		selectedAnim = anim;
		markDirty();
		updateGui();
	}

	public void editAnim(IPose pose, String displayName, boolean add, boolean loop, InterpolatorType it) {
		if(selectedAnim != null) {
			String fname = null;
			AnimationType type;
			if(pose instanceof VanillaPose) {
				fname = "v_" + ((VanillaPose)pose).name().toLowerCase() + "_" + displayName.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
				type = AnimationType.POSE;
			} else if(pose != null) {
				fname = "c_" + ((CustomPose) pose).getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
				type = AnimationType.POSE;
			} else {
				fname = "g_" + displayName.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
				type = AnimationType.GESTURE;
			}
			action("edit", "action.cpm.anim").
			updateValueOp(selectedAnim, selectedAnim.add, add, (a, b) -> a.add = b).
			updateValueOp(selectedAnim, selectedAnim.loop, loop, (a, b) -> a.loop = b).
			updateValueOp(selectedAnim, selectedAnim.displayName, displayName, (a, b) -> a.displayName = b).
			updateValueOp(selectedAnim, selectedAnim.pose, pose, (a, b) -> a.pose = b).
			updateValueOp(selectedAnim, selectedAnim.type, type, (a, b) -> a.type = b).
			updateValueOp(selectedAnim, selectedAnim.filename, fname, (a, b) -> a.filename = b).
			updateValueOp(selectedAnim, selectedAnim.intType, it, (a, b) -> a.intType = b).
			onAction(selectedAnim, EditorAnim::clearCache).
			execute();
			updateGui();
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
		if(selectedAnim != null) {
			selectedAnim.addFrame();
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
		updateValueOp(selectedAnim, selectedAnim.duration, value, 1, (int) Short.MAX_VALUE, (a, b) -> a.duration = b, setAnimDuration).
		execute();
	}

	public void setAnimPriority(int value) {
		if(selectedAnim == null)return;
		action("setAnim", "label.cpm.anim_priority").
		updateValueOp(selectedAnim, selectedAnim.priority, value, (int) Byte.MIN_VALUE, (int) Byte.MAX_VALUE, (a, b) -> a.priority = b, setAnimPriority).
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

	public void delSelectedAnimPartData() {
		if(selectedAnim != null) {
			selectedAnim.clearSelectedData();
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

	public UIColors colors() {
		return frame.getGui().getColors();
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

	public IGui gui() {
		return frame.getGui();
	}

	public ETextures getTextureProvider() {
		return selectedElement != null ? selectedElement.getTexture() : textures.get(TextureSheetType.SKIN);
	}

	public ModelElement getSelectedElement() {
		return selectedElement instanceof ModelElement ? (ModelElement) selectedElement : null;
	}

	public void free() {
		textures.values().forEach(ETextures::free);
		definition.cleanup();
	}

	public void restitchTextures() {
		textures.values().forEach(ETextures::restitchTexture);
		if(textures.get(TextureSheetType.SKIN).hasStitches() && hasVanillaParts()) {
			Generators.convertModel(this);
		}
	}

	public void tick() {
		if(autoSaveDirty && lastEdit + 5*60*1000 < System.currentTimeMillis()) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			File autosaves = new File(modelsDir, "autosaves");
			autosaves.mkdirs();
			File file = new File(autosaves, String.format("autosave-%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS-", System.currentTimeMillis()) + (this.file == null ? frame.getGui().i18nFormat("label.cpm.new_project") : this.file.getName()));
			Log.info("Editor autosave: " + file.getName());
			setInfoMsg.accept(Pair.of(5000, gui().i18nFormat("tooltip.cpm.autosaving", file.getName())));
			try {
				save0(file);
				setInfoMsg.accept(Pair.of(2000, gui().i18nFormat("tooltip.cpm.autoSaveSuccess", file.getName())));
			} catch (Exception e) {
				frame.getGui().onGuiException("Failed to autosave", e, false);
			}
			autoSaveDirty = false;
		}
	}

	public void addRoot(RootGroups group) {
		List<ModelElement> elems = new ArrayList<>();
		for (int i = 0; i < group.types.length; i++) {
			RootModelType type = group.types[i];
			if(elements.stream().noneMatch(e -> e.type == ElementType.ROOT_PART && e.typeData == type)) {
				ModelElement e = new ModelElement(this, ElementType.ROOT_PART, type, frame.getGui());
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
		if(drawBoundingBox) {
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
			if(tex != null && tex.isEditable()) {
				action("add", "action.cpm.animTex").addToList(tex.animatedTexs, new AnimatedTex(this, tex.getType())).execute();
				updateGui();
			}
		}
	}
}
