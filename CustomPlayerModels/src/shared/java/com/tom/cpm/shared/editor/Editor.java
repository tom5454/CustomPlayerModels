package com.tom.cpm.shared.editor;

import static com.tom.cpm.shared.MinecraftObjectHolder.gson;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
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
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.UpdaterRegistry;
import com.tom.cpl.gui.UpdaterRegistry.Updater;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.actions.Action;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimationEncodingData;
import com.tom.cpm.shared.editor.anim.AnimationType;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplType;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler;
import com.tom.cpm.shared.editor.template.TemplateArgType;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.editor.tree.ScalingElement;
import com.tom.cpm.shared.editor.tree.TexturesElement;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.tree.TreeElement.VecType;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.editor.util.ModelDescription.CopyProtection;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.editor.util.StoreIDGen;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;
import com.tom.cpm.shared.io.ProjectFile;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.PerFaceUV;
import com.tom.cpm.shared.model.render.PerFaceUV.Dir;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureType;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class Editor {
	public static final int projectFileVersion = 1;
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
	public Updater<ModeDisplType> setModePanel = updaterReg.create();
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
	public Updater<Boolean> setSkinEdited = updaterReg.create();
	public Updater<String> setReload = updaterReg.create();
	public Updater<Boolean> setAnimPlay = updaterReg.create();
	public Updater<Integer> setAnimPriority = updaterReg.create();
	public Updater<Void> gestureFinished = updaterReg.create();
	public Updater<Boolean> displayViewport = updaterReg.create();
	public Updater<Boolean> heldRenderEnable = updaterReg.create();
	public Updater<Float> setValue = updaterReg.create();
	public Updater<EditorTool> setTool = updaterReg.create();
	public Updater<Vec4f> setFaceUVs = updaterReg.create();
	public Updater<Rot> setFaceRot = updaterReg.create();

	public Supplier<Vec2i> cursorPos;
	public int penColor = 0xffffff;
	public EditorTool drawMode = EditorTool.PEN;
	public int brushSize = 1;
	public boolean drawAllUVs = false;
	public boolean onlyDrawOnSelected = true;
	public EnumMap<Hand, HeldItem> handDisplay = new EnumMap<>(Hand.class);
	public Set<PlayerModelLayer> modelDisplayLayers = new HashSet<>();
	public ScalingElement scalingElem = new ScalingElement(this);
	public Dir perfaceFaceDir = Dir.UP;

	public ViewportCamera camera = new ViewportCamera();
	private Stack<Action> undoQueue = new Stack<>();
	private Stack<Action> redoQueue = new Stack<>();
	public boolean renderPaint;
	public boolean renderBase = true;
	public boolean applyAnim;
	public boolean playFullAnim;
	public boolean playerTpose;
	public boolean applyScaling;
	public long playStartTime, gestureStartTime;
	private StoreIDGen storeIDgen;
	public AnimationEncodingData animEnc;

	public EditorGui frame;
	public TreeElement selectedElement;
	public List<ModelElement> elements = new ArrayList<>();
	public EditorAnim selectedAnim;
	public List<EditorAnim> animsToPlay = new ArrayList<>();
	public IPose poseToApply;
	public List<EditorAnim> animations = new ArrayList<>();
	public List<EditorTemplate> templates = new ArrayList<>();
	public TemplateSettings templateSettings;
	public TexturesElement texElem = new TexturesElement(this);
	public SkinType skinType;
	public boolean customSkinType;
	public ModelDescription description;
	public float scaling;
	public Image vanillaSkin;
	public boolean dirty, autoSaveDirty;
	public long lastEdit;
	public ModelDefinition definition;
	public Map<TextureSheetType, ETextures> textures = new HashMap<>();
	public File file;
	public ProjectFile project = new ProjectFile();

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

	public void setTexSize(int x, int y) {
		ETextures texs = getTextureProvider();
		if(texs != null) {
			EditorTexture tex = texs.provider;
			action("set", "action.cpm.texSize").
			updateValueOp(tex, tex.size.x, x, (a, b) -> a.size.x = b).
			updateValueOp(tex, tex.size.y, y, (a, b) -> a.size.y = b).
			execute();
		}
	}

	public void switchReColorEffect() {
		if(selectedElement != null)selectedElement.switchEffect(Effect.RECOLOR);
	}

	public void drawPixel(int x, int y, boolean isSkin) {
		switch (drawMode) {
		case PEN:
			setPixel(x, y, penColor | 0xff000000);
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
						pixels.add(p);
						nextPixels.add(new Vec2i(p.x - 1, p.y));
						nextPixels.add(new Vec2i(p.x + 1, p.y));
						nextPixels.add(new Vec2i(p.x, p.y - 1));
						nextPixels.add(new Vec2i(p.x, p.y + 1));
					}
				}
				int color = penColor | 0xff000000;
				action("action.cpm.bucketFill").
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
		setNameDisplay.accept((file == null ? frame.getGui().i18nFormat("label.cpm.new_project") : file.getName()) + "*");
		dirty = true;
		if(!autoSaveDirty)lastEdit = System.currentTimeMillis();
		autoSaveDirty = true;
		redoQueue.clear();
		setUndo.accept(undoQueue.empty() ? null : undoQueue.peek().getName());
		setRedo.accept(null);
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
		setModePanel.accept(ModeDisplType.NULL);
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
		setPerFaceUV.accept(null);
		setVis.accept(false);
		setAnimPriority.accept(null);
		displayViewport.accept(true);
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
							PlayerPartValues val = PlayerPartValues.getFor((PlayerModelParts) selectedElement.typeData, skinType);
							setAnimPos.accept(val.getPos());
							setAnimRot.accept(new Vec3f());
						} else {
							setAnimPos.accept(selectedElement.pos);
							setAnimRot.accept(selectedElement.rotation);
						}
						if(!selectedElement.texture || selectedElement.recolor) {
							setAnimColor.accept(selectedElement.rgb);
						}
						setAnimShow.accept(selectedElement.show);
					} else {
						if(!selectedElement.texture || selectedElement.recolor) {
							Vec3f c = dt.getColor();
							setAnimColor.accept((((int) c.x) << 16) | (((int) c.y) << 8) | ((int) c.z));
						}
						setAnimPos.accept(dt.getPosition());
						setAnimRot.accept(dt.getRotation());
						setAnimShow.accept(dt.isVisible());
					}
				}
				setFrameDelEn.accept(true);
			}
			setFrameAddEn.accept(true);
			setAnimDelEn.accept(true);
			setAnimDuration.accept(selectedAnim.duration);
			setAnimPlayEn.accept(selectedAnim.getFrames().size() > 1);
			setAnimPriority.accept(selectedAnim.priority);
		}
		setSelAnim.accept(selectedAnim);
		ETextures tex = getTextureProvider();
		setSkinEdited.accept(tex != null ? tex.isEdited() : false);
		setReload.accept(tex != null && tex.file != null ? tex.file.getName() : null);
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
		Image skin = MinecraftClientAccess.get().getVanillaSkin(skinType);
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
		scaling = 0;
		storeIDgen = new StoreIDGen();
		Player<?, ?> profile = MinecraftClientAccess.get().getClientPlayer();
		profile.getTextures().load().thenRun(() -> {
			if(!customSkinType)skinType = profile.getSkinType();
			CompletableFuture<Image> img = profile.getTextures().getTexture(TextureType.SKIN);
			this.vanillaSkin = MinecraftClientAccess.get().getVanillaSkin(skinType);
			skinTex.setDefaultImg(vanillaSkin);
			img.thenAccept(s -> {
				if(!skinTex.isEdited()) {
					if(s != null) {
						this.vanillaSkin = s;
						skinTex.setDefaultImg(vanillaSkin);
						skinTex.setImage(new Image(this.vanillaSkin));
					} else {
						skinTex.setImage(new Image(this.vanillaSkin));
					}
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
		elements.forEach(ModelElement::preRender);
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
		Map<String, Object> data = new HashMap<>();
		List<Map<String, Object>> lst = new ArrayList<>();
		data.put("elements", lst);
		for (ModelElement elem : elements) {
			Map<String, Object> map = new HashMap<>();
			lst.add(map);
			map.put("id", ((VanillaModelPart) elem.typeData).getName());
			if(elem.typeData instanceof RootModelType)map.put("customPart", true);
			map.put("show", elem.show);
			if(!elem.children.isEmpty()) {
				List<Map<String, Object>> list = new ArrayList<>();
				map.put("children", list);
				saveChildren(elem, list);
			}
			map.put("pos", elem.pos.toMap());
			map.put("rotation", elem.rotation.toMap());
			map.put("dup", elem.duplicated);
			if(elem.duplicated) {
				storeIDgen.setID(elem);
				map.put("storeID", elem.storeID);
			}
		}
		data.put("version", projectFileVersion);
		for(TextureSheetType tex : TextureSheetType.VALUES) {
			String name = tex.name().toLowerCase();
			ETextures eTex = textures.get(tex);
			if(eTex != null) {
				Map<String, Object> size = new HashMap<>();
				data.put(name + "Size", size);
				size.put("x", eTex.provider.size.x);
				size.put("y", eTex.provider.size.y);
				if(eTex.provider.texture != null && eTex.isEdited()) {
					try(OutputStream os = project.setAsStream(name + ".png")) {
						eTex.getImage().storeTo(os);
					}
				}
			} else {
				project.delete(name + ".png");
			}
		}

		data.put("skinType", skinType.getName());
		data.put("scaling", scaling);
		try(OutputStreamWriter os = new OutputStreamWriter(project.setAsStream("config.json"))) {
			gson.toJson(data, os);
		}
		project.clearFolder("animations");
		for (EditorAnim e : animations) {
			data = new HashMap<>();
			data.put("additive", e.add);
			data.put("name", e.displayName);
			if(e.pose instanceof CustomPose)data.put("name", ((CustomPose)e.pose).getName());
			data.put("duration", e.duration);
			data.put("priority", e.priority);
			data.put("loop", e.loop);
			data.put("frames", e.writeFrames());
			try(OutputStreamWriter os = new OutputStreamWriter(project.setAsStream("animations/" + e.filename))) {
				gson.toJson(data, os);
			}
		}
		if(animEnc != null) {
			data = new HashMap<>();
			data.put("freeLayers", animEnc.freeLayers.stream().map(l -> l.getLowerName()).collect(Collectors.toList()));
			data.put("defaultValues", animEnc.defaultLayerValue.entrySet().stream().map(e -> Pair.of(e.getKey().getLowerName(), e.getValue())).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
			try(OutputStreamWriter os = new OutputStreamWriter(project.setAsStream("anim_enc.json"))) {
				gson.toJson(data, os);
			}
		}
		if(!templates.isEmpty()) {
			data = new HashMap<>();
			lst = new ArrayList<>();
			data.put("templates", lst);
			for (EditorTemplate templ : templates) {
				Map<String, Object> t = new HashMap<>();
				lst.add(t);
				templ.store(t);
			}
			try(OutputStreamWriter os = new OutputStreamWriter(project.setAsStream("templates.json"))) {
				gson.toJson(data, os);
			}
		}
		if(templateSettings != null) {
			data = new HashMap<>();
			lst = new ArrayList<>();
			data.put("args", lst);
			data.put("texture", templateSettings.hasTex);
			for(TemplateArgHandler arg : templateSettings.templateArgs) {
				Map<String, Object> map = new HashMap<>();
				lst.add(map);
				map.put("name", arg.name);
				map.put("desc", arg.desc);
				map.put("type", arg.type.name().toLowerCase());
				if(arg.handler.requiresParts() && arg.effectedElems != null) {
					List<Number> partList = new ArrayList<>();
					arg.effectedElems.forEach(e -> partList.add(e.storeID));
					map.put("parts", partList);
				}
				Map<String, Object> m = new HashMap<>();
				map.put("data", m);
				arg.handler.saveProject(m);
			}
			List<Object> dispIds = new ArrayList<>();
			data.put("displayElems", dispIds);
			walkElements(elements, e -> {
				if(e.templateElement) {
					dispIds.add(e.storeID);
				}
			});
			try(OutputStreamWriter os = new OutputStreamWriter(project.setAsStream("template_settings.json"))) {
				gson.toJson(data, os);
			}
		}
		if(description != null) {
			data = new HashMap<>();
			data.put("name", description.name);
			data.put("desc", description.desc);
			Map<String, Object> map = new HashMap<>();
			data.put("cam", map);
			map.put("zoom", description.camera.camDist);
			map.put("look", description.camera.look.toMap());
			map.put("pos", description.camera.position.toMap());
			map.put("copyProt", description.copyProtection.name().toLowerCase());
			try(OutputStreamWriter os = new OutputStreamWriter(project.setAsStream("description.json"))) {
				gson.toJson(data, os);
			}
			if(description.icon != null) {
				try(OutputStream os = project.setAsStream("desc_icon.png")) {
					description.icon.storeTo(os);
				}
			}
		}
		project.save(file);
	}

	public void save(File file) throws IOException {
		save0(file);
		this.file = file;
		dirty = false;
		autoSaveDirty = false;
		updateGui();
	}

	private void saveChildren(ModelElement modelElement, List<Map<String, Object>> lst) {
		for (ModelElement elem : modelElement.children) {
			Map<String, Object> map = new HashMap<>();
			lst.add(map);
			map.put("name", elem.name);
			map.put("show", elem.show);
			map.put("texture", elem.texture);
			map.put("textureSize", elem.textureSize);
			map.put("offset", elem.offset.toMap());
			map.put("pos", elem.pos.toMap());
			map.put("rotation", elem.rotation.toMap());
			map.put("size", elem.size.toMap());
			map.put("scale", elem.scale.toMap());
			map.put("u", elem.u);
			map.put("v", elem.v);
			map.put("color", Integer.toHexString(elem.rgb));
			map.put("mirror", elem.mirror);
			map.put("mcScale", elem.mcScale);
			map.put("glow", elem.glow);
			map.put("recolor", elem.recolor);
			map.put("hidden", elem.hidden);
			map.put("singleTex", elem.singleTex);
			if(elem.faceUV != null)map.put("faceUV", elem.faceUV.toMap());
			storeIDgen.setID(elem);
			map.put("storeID", elem.storeID);

			if(!elem.children.isEmpty()) {
				List<Map<String, Object>> list = new ArrayList<>();
				map.put("children", list);
				saveChildren(elem, list);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void load(File file) throws IOException {
		loadDefaultPlayerModel();
		project.load(file);
		Map<String, Object> data;
		try(InputStreamReader rd = new InputStreamReader(project.getAsStream("config.json"))) {
			data = (Map<String, Object>) gson.fromJson(rd, Object.class);
		}
		byte[] ze;
		int fileVersion = ((Number) data.getOrDefault("version", 0)).intValue();
		if(fileVersion != projectFileVersion)throw new IOException("Unsupported file version try a newer version of the mod");
		List<Map<String, Object>> lst = (List<Map<String, Object>>) data.get("elements");
		this.file = file;
		for (Map<String, Object> map : lst) {
			String key = (String) map.get("id");
			ModelElement elem = null;
			if ((boolean) map.getOrDefault("customPart", false)) {
				for (RootModelType rmt : RootModelType.VALUES) {
					if(rmt.getName().equalsIgnoreCase(key)) {
						elem = new ModelElement(this, ElementType.ROOT_PART, rmt, frame.getGui());
						break;
					}
				}
				elements.add(elem);
				elem.storeID = ((Number) map.getOrDefault("storeID", 0)).longValue();
			} else if ((boolean) map.getOrDefault("dup", false)) {
				for (ModelElement e : elements) {
					if(((VanillaModelPart) e.typeData).getName().equalsIgnoreCase(key)) {
						elem = new ModelElement(this, ElementType.ROOT_PART, e.typeData, frame.getGui());
						elem.duplicated = true;
						elements.add(elem);
						break;
					}
				}
				elem.storeID = ((Number) map.getOrDefault("storeID", 0)).longValue();
			} else {
				for (ModelElement e : elements) {
					if(((VanillaModelPart) e.typeData).getName().equalsIgnoreCase(key)) {
						elem = e;
						break;
					}
				}
			}
			if(elem != null) {
				elem.show = (boolean) map.get("show");
				if(map.containsKey("children")) {
					loadChildren((List<Map<String, Object>>) map.get("children"), elem, fileVersion);
				}
				elem.pos = new Vec3f((Map<String, Object>) map.get("pos"), new Vec3f(0, 0, 0));
				elem.rotation = new Vec3f((Map<String, Object>) map.get("rotation"), new Vec3f(0, 0, 0));
			}
		}
		if(data.containsKey("skinType")) {
			customSkinType = true;
			skinType = SkinType.get((String) data.get("skinType"));
		}
		List<String> anims = project.listEntires("animations");
		scaling = ((Number)data.getOrDefault("scaling", 0)).floatValue();
		if(anims != null) {
			for (String anim : anims) {
				try(InputStreamReader rd = new InputStreamReader(project.getAsStream("animations/" + anim))) {
					data = (Map<String, Object>) gson.fromJson(rd, Object.class);
				}
				IPose pose = null;
				String displayName = (String) data.getOrDefault("name", "Unnamed");
				AnimationType type = null;
				String[] sp = anim.split("_", 2);
				if(sp[0].equals("v")) {
					String poseName = sp[1].endsWith(".json") ? sp[1].substring(0, sp[1].length() - 5) : sp[1];
					for(VanillaPose p : VanillaPose.VALUES) {
						if(poseName.startsWith(p.name().toLowerCase())) {
							pose = p;
							type = AnimationType.POSE;
							break;
						}
					}
				} else if(sp[0].equals("c")) {
					pose = new CustomPose(displayName);
					type = AnimationType.POSE;
				} else if(sp[0].equals("g")) {
					type = AnimationType.GESTURE;
				}
				if(type == null)continue;
				EditorAnim e = new EditorAnim(this, anim, type, false);
				e.displayName = displayName;
				e.pose = pose;
				e.add = (boolean) data.get("additive");
				animations.add(e);
				e.duration = ((Number)data.get("duration")).intValue();
				e.priority = ((Number)data.getOrDefault("priority", 0)).intValue();
				e.loop = ((boolean)data.getOrDefault("loop", false));
				List<Map<String, Object>> frames = (List<Map<String, Object>>) data.get("frames");
				for (Map<String,Object> map : frames) {
					e.loadFrame(map);
				}
			}
		}
		for(TextureSheetType tex : TextureSheetType.VALUES) {
			String name = tex.name().toLowerCase();
			ze = project.getEntry(name + ".png");
			if(ze != null) {
				Image img = Image.loadFrom(new ByteArrayInputStream(ze));
				if(img.getWidth() > tex.texLimit || img.getHeight() > tex.texLimit) {
					Log.error("Illegal image size for texture: " + name);
					continue;
				}
				ETextures eTex = textures.get(tex);
				if(eTex == null)eTex = new ETextures(this, tex);
				textures.put(tex, eTex);
				eTex.setImage(img);
				eTex.markDirty();
				Map<String, Object> skinTexSize = (Map<String, Object>) data.get(name + "Size");
				eTex.provider.size = new Vec2i(((Number)skinTexSize.get("x")).intValue(), ((Number)skinTexSize.get("y")).intValue());
			}
		}
		ze = project.getEntry("anim_enc.json");
		if(ze != null) {
			try(InputStreamReader rd = new InputStreamReader(new ByteArrayInputStream(ze))) {
				data = (Map<String, Object>) gson.fromJson(rd, Object.class);
			}
			animEnc = new AnimationEncodingData();
			((List<String>) data.get("freeLayers")).forEach(v -> animEnc.freeLayers.add(PlayerSkinLayer.getLayer(v)));
			((Map<String, Boolean>) data.get("defaultValues")).forEach((k, v) -> animEnc.defaultLayerValue.put(PlayerSkinLayer.getLayer(k), v));
		}
		ze = project.getEntry("templates.json");
		if(ze != null) {
			try(InputStreamReader rd = new InputStreamReader(new ByteArrayInputStream(ze))) {
				data = (Map<String, Object>) gson.fromJson(rd, Object.class);
			}
			lst = (List<Map<String, Object>>) data.get("templates");
			for (Map<String,Object> map : lst) {
				EditorTemplate templ = EditorTemplate.load(this, map);
				templates.add(templ);
			}
		}
		ze = project.getEntry("template_settings.json");
		if(ze != null) {
			templateSettings = new TemplateSettings(this);
			try(InputStreamReader rd = new InputStreamReader(new ByteArrayInputStream(ze))) {
				data = (Map<String, Object>) gson.fromJson(rd, Object.class);
			}
			templateSettings.hasTex = (boolean) data.get("texture");
			lst = (List<Map<String, Object>>) data.get("args");
			for (Map<String, Object> map : lst) {
				TemplateArgType type = TemplateArgType.lookup((String) map.get("type"));
				TemplateArgHandler arg = new TemplateArgHandler(this, (String) map.get("name"), (String) map.get("desc"), type);
				templateSettings.templateArgs.add(arg);
				arg.handler.loadProject((Map<String, Object>) map.get("data"));
				if(arg.handler.requiresParts() && arg.effectedElems != null) {
					List<Number> partList = (List<Number>) map.get("parts");
					partList.forEach(e -> walkElements(elements, elem -> {
						if(elem.storeID == e.longValue()) {
							arg.effectedElems.add(elem);
						}
					}));
				}
			}
			List<Long> templElems = ((List<Number>) data.get("displayElems")).stream().map(Number::longValue).collect(Collectors.toList());
			walkElements(elements, e -> {
				if(templElems.contains(e.storeID)) {
					e.templateElement = true;
				}
			});
		}
		ze = project.getEntry("description.json");
		if(ze != null) {
			try(InputStreamReader rd = new InputStreamReader(new ByteArrayInputStream(ze))) {
				data = (Map<String, Object>) gson.fromJson(rd, Object.class);
			}
			description = new ModelDescription();
			description.name = (String) data.get("name");
			description.desc = (String) data.get("desc");
			Map<String, Object> map = (Map<String, Object>) data.get("cam");
			description.camera.camDist = ((Number)map.get("zoom")).floatValue();
			description.camera.look = new Vec3f((Map<String, Object>) map.get("look"), description.camera.look);
			description.camera.position = new Vec3f((Map<String, Object>) map.get("pos"), description.camera.position);
			description.copyProtection = CopyProtection.lookup((String) map.getOrDefault("copyProt", "normal"));
		}
		ze = project.getEntry("desc_icon.png");
		if(ze != null) {
			if(description == null)description = new ModelDescription();
			description.icon = Image.loadFrom(new ByteArrayInputStream(ze));
			if(description.icon.getWidth() != 256 || description.icon.getHeight() != 256) {
				description.icon = null;
				Log.error("Illegal image size for model/template icon must be 256x256");
			}
		}
		ze = project.getEntry("list_icon.png");
		if(ze != null) {
			ETextures tex = null;
			if(textures.containsKey(TextureSheetType.LIST_ICON))tex = textures.get(TextureSheetType.LIST_ICON);
			if(tex == null)tex = new ETextures(this, TextureSheetType.LIST_ICON);
			tex.provider.size = new Vec2i(32, 32);
			tex.setImage(Image.loadFrom(new ByteArrayInputStream(ze)));
			if(tex.getImage().getWidth() > 32 || tex.getImage().getHeight() > 32 ||
					tex.getImage().getWidth() != tex.getImage().getHeight()) {
				tex.free();
				tex = null;
				Log.error("Illegal image size for list icon must be 32x32 or less and square");
			}
			if(tex == null)textures.remove(TextureSheetType.LIST_ICON);
			else textures.put(TextureSheetType.LIST_ICON, tex);
		}
		restitchTextures();
		updateGui();
	}

	@SuppressWarnings("unchecked")
	private void loadChildren(List<Map<String, Object>> list, ModelElement parent, int fileVer) {
		for (Map<String, Object> map : list) {
			ModelElement elem = new ModelElement(this);
			elem.parent = parent;
			parent.children.add(elem);
			elem.name = (String) map.get("name");
			elem.show = (boolean) map.get("show");
			elem.texture = (boolean) map.get("texture");
			elem.textureSize = ((Number)map.get("textureSize")).intValue();
			elem.offset = new Vec3f((Map<String, Object>) map.get("offset"), new Vec3f());
			elem.pos = new Vec3f((Map<String, Object>) map.get("pos"), new Vec3f());
			elem.rotation = new Vec3f((Map<String, Object>) map.get("rotation"), new Vec3f());
			elem.size = new Vec3f((Map<String, Object>) map.get("size"), new Vec3f(1, 1, 1));
			elem.scale = new Vec3f((Map<String, Object>) map.get("scale"), new Vec3f(1, 1, 1));
			elem.u = ((Number)map.get("u")).intValue();
			elem.v = ((Number)map.get("v")).intValue();
			elem.rgb = Integer.parseUnsignedInt((String) map.get("color"), 16);
			elem.mirror = (boolean) map.get("mirror");
			elem.mcScale = ((Number) map.get("mcScale")).floatValue();
			elem.glow = (boolean) map.getOrDefault("glow", false);
			elem.recolor = (boolean) map.getOrDefault("recolor", false);
			elem.hidden = (boolean) map.getOrDefault("hidden", false);
			elem.singleTex = (boolean) map.getOrDefault("singleTex", false);
			if(map.containsKey("faceUV"))elem.faceUV = new PerFaceUV((Map<String, Map<String, Object>>) map.get("faceUV"));
			elem.storeID = ((Number) map.getOrDefault("storeID", 0)).longValue();

			if(map.containsKey("children")) {
				loadChildren((List<Map<String, Object>>) map.get("children"), elem, fileVer);
			}
		}
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
		updateGui();
	}

	public void redo() {
		if(redoQueue.empty())return;
		Action r = redoQueue.pop();
		if(r != null) {
			undoQueue.add(r);
			r.run();
		}
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

	public void addNewAnim(IPose pose, String displayName, boolean add, boolean loop) {
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
		action("add", "action.cpm.anim").addToList(animations, anim).onUndo(() -> selectedAnim = null).execute();
		selectedAnim = anim;
		markDirty();
		updateGui();
	}

	public void editAnim(IPose pose, String displayName, boolean add, boolean loop) {
		if(selectedAnim != null) {
			String[] sp = selectedAnim.filename.split("_", 2);
			String newFname = (pose instanceof VanillaPose ? "v" : (pose != null ? "c" : "g")) + "_" + sp[1];
			action("edit", "action.cpm.anim").
			updateValueOp(selectedAnim, selectedAnim.add, add, (a, b) -> a.add = b).
			updateValueOp(selectedAnim, selectedAnim.loop, loop, (a, b) -> a.loop = b).
			updateValueOp(selectedAnim, selectedAnim.displayName, displayName, (a, b) -> a.displayName = b).
			updateValueOp(selectedAnim, selectedAnim.pose, pose, (a, b) -> a.pose = b).
			updateValueOp(selectedAnim, selectedAnim.type, pose != null ? AnimationType.POSE : AnimationType.GESTURE, (a, b) -> a.type = b).
			updateValueOp(selectedAnim, selectedAnim.filename, newFname, (a, b) -> a.filename = b).
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
		updateValueOp(selectedAnim, selectedAnim.duration, value, (a, b) -> a.duration = b).
		execute();
	}

	public void setAnimPriority(int value) {
		if(selectedAnim == null)return;
		action("setAnim", "label.cpm.anim_priority").
		updateValueOp(selectedAnim, selectedAnim.priority, value, (a, b) -> a.priority = b).
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
		poseToApply = null;
	}

	public UIColors colors() {
		return frame.getGui().getColors();
	}

	public boolean hasVanillaParts() {
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					if(el.show)return true;
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
			try {
				save0(file);
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
		Arrays.stream(group.types).map(group::getTexSheet).distinct().forEach(tx -> {
			if(tx.editable && !textures.containsKey(tx)) {
				ETextures tex = new ETextures(this, tx);
				ab.addToMap(textures, tx, tex);
				tex.provider.size = tx.getDefSize();
				Image def = new Image(tex.provider.size.x, tex.provider.size.y);
				try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/" + tx.name().toLowerCase() + ".png")) {
					def = Image.loadFrom(is);
				} catch (IOException e) {
				}
				tex.setDefaultImg(def);
				tex.setImage(new Image(def));
				tex.markDirty();
				if(tx.texType != null) {
					Player<?, ?> profile = MinecraftClientAccess.get().getClientPlayer();
					profile.getTextures().load().thenRun(() -> {
						CompletableFuture<Image> img = profile.getTextures().getTexture(tx.texType);
						img.thenAccept(s -> {
							if(!tex.isEdited()) {
								if(s != null) {
									tex.setDefaultImg(s);
									tex.setImage(new Image(s));
									tex.restitchTexture();
								}
							}
						});
					});
				}
			}
		});
		ab.execute();
		updateGui();
	}
}
