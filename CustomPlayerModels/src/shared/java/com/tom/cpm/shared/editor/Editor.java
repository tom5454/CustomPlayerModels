package com.tom.cpm.shared.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.anim.IElem;
import com.tom.cpm.shared.editor.util.PlayerPartValues;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.editor.util.StoreIDGen;
import com.tom.cpm.shared.editor.util.ValueOp;
import com.tom.cpm.shared.gui.Frame;
import com.tom.cpm.shared.gui.UIColors;
import com.tom.cpm.shared.gui.UpdaterRegistry;
import com.tom.cpm.shared.gui.UpdaterRegistry.Updater;
import com.tom.cpm.shared.gui.elements.MessagePopup;
import com.tom.cpm.shared.io.ProjectFile;
import com.tom.cpm.shared.math.Vec2i;
import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.math.Vec3i;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.skin.SkinProvider;
import com.tom.cpm.shared.util.Image;
import com.tom.cpm.shared.util.Pair;

public class Editor {
	public static final int projectFileVersion = 1;
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
	public UpdaterRegistry updaterReg = new UpdaterRegistry();
	public Updater<Vec3f> setOffset = updaterReg.create();
	public Updater<Vec3f> setRot = updaterReg.create();
	public Updater<Vec3f> setPosition = updaterReg.create();
	public Updater<Vec3f> setSize = updaterReg.create();
	public Updater<Vec3f> setScale = updaterReg.create();
	public Updater<Float> setMCScale = updaterReg.create();
	public Updater<Boolean> setMirror = updaterReg.create();
	public Updater<String> updateName = updaterReg.create();
	public Updater<Boolean> setModeBtn = updaterReg.create();
	public Updater<Vec3i> modeUpdate = updaterReg.create();
	public Updater<Boolean> setVis = updaterReg.create();
	public Updater<Boolean> setDelEn = updaterReg.create();
	public Updater<Boolean> setAddEn = updaterReg.create();
	public Updater<String> setNameDisplay = updaterReg.create();
	public Updater<Void> updateTree = updaterReg.create();
	public Updater<Boolean> setUndoEn = updaterReg.create();
	public Updater<Boolean> setRedoEn = updaterReg.create();
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
	public Updater<Boolean> setSkinEdited = updaterReg.create();

	public Supplier<Vec2i> cursorPos;
	public int penColor = 0xffffff;
	public int drawMode = 0;

	public Vec3f position = new Vec3f(0.5f, 1, 0.5f);
	public Vec3f look = new Vec3f(0.25f, 0.5f, 0.25f);
	public float camDist = 64;
	public Stack<Runnable> undoQueue = new Stack<>();
	public Stack<Runnable> redoQueue = new Stack<>();
	public Runnable currentOp;
	public boolean renderPaint;
	public boolean renderBase = true;
	public boolean applyAnim;
	public boolean playFullAnim;
	public boolean playerTpose;
	public long playStartTime;
	private StoreIDGen storeIDgen;
	public AnimationEncodingData animEnc;

	public Frame gui;
	public ModelElement selectedElement;
	public List<ModelElement> elements = new ArrayList<>();
	public EditorAnim selectedAnim;
	public List<EditorAnim> animations = new ArrayList<>();
	public int skinType;
	public Image vanillaSkin;
	public boolean dirty;
	public ModelDefinition definition;
	public SkinProvider skinProvider = new SkinProvider();
	public SkinProvider listIconProvider;
	public File file, skinFile;
	public ProjectFile project = new ProjectFile();

	public Editor(Frame gui) {
		this.definition = new ModelDefinition(this);
		this.gui = gui;
	}

	public void setVec(Vec3f v, int object) {
		if(selectedElement != null) {
			switch (object) {
			case 0:
			{
				addUndo(new ValueOp<>(selectedElement, selectedElement.size, (a, b) -> a.size = b));
				selectedElement.size = v;
				boolean changed = false;
				if(selectedElement.size.x < 0) {
					selectedElement.size.x = 0;
					changed = true;
				}
				if(selectedElement.size.y < 0) {
					selectedElement.size.y = 0;
					changed = true;
				}
				if(selectedElement.size.z < 0) {
					selectedElement.size.z = 0;
					changed = true;
				}
				if(selectedElement.size.x > 25) {
					selectedElement.size.x = 25;
					changed = true;
				}
				if(selectedElement.size.y > 25) {
					selectedElement.size.y = 25;
					changed = true;
				}
				if(selectedElement.size.z > 25) {
					selectedElement.size.z = 25;
					changed = true;
				}
				currentOp = new ValueOp<>(selectedElement, selectedElement.size, (a, b) -> a.size = b);
				if(changed)setSize.accept(selectedElement.size);
			}
			break;
			case 1:
				addUndo(new ValueOp<>(selectedElement, selectedElement.offset, (a, b) -> a.offset = b));
				selectedElement.offset = v;
				posLimit(selectedElement.offset, setOffset);
				currentOp = new ValueOp<>(selectedElement, selectedElement.offset, (a, b) -> a.offset = b);
				break;
			case 2:
				addUndo(new ValueOp<>(selectedElement, selectedElement.rotation, (a, b) -> a.rotation = b));
				selectedElement.rotation = v;
				if(v.x < 0 || v.x > 360 || v.y < 0 || v.y > 360 || v.z < 0 || v.z > 360) {
					while(selectedElement.rotation.x < 0)selectedElement.rotation.x += 360;
					while(selectedElement.rotation.x > 360)selectedElement.rotation.x -= 360;
					while(selectedElement.rotation.y < 0)selectedElement.rotation.y += 360;
					while(selectedElement.rotation.y > 360)selectedElement.rotation.y -= 360;
					while(selectedElement.rotation.z < 0)selectedElement.rotation.z += 360;
					while(selectedElement.rotation.z > 360)selectedElement.rotation.z -= 360;
					setRot.accept(selectedElement.rotation);
				}
				currentOp = new ValueOp<>(selectedElement, selectedElement.rotation, (a, b) -> a.rotation = b);
				break;
			case 3:
				addUndo(new ValueOp<>(selectedElement, selectedElement.pos, (a, b) -> a.pos = b));
				selectedElement.pos = v;
				posLimit(selectedElement.pos, setPosition);
				currentOp = new ValueOp<>(selectedElement, selectedElement.pos, (a, b) -> a.pos = b);
				break;
			case 4:
				addUndo(new ValueOp<>(selectedElement, selectedElement.scale, (a, b) -> a.scale = b));
				selectedElement.scale = v;
				{
					boolean changed = false;
					if(selectedElement.scale.x < 0) {
						selectedElement.scale.x = 0;
						changed = true;
					}
					if(selectedElement.scale.y < 0) {
						selectedElement.scale.y = 0;
						changed = true;
					}
					if(selectedElement.scale.z < 0) {
						selectedElement.scale.z = 0;
						changed = true;
					}
					if(selectedElement.scale.x > 25) {
						selectedElement.scale.x = 25;
						changed = true;
					}
					if(selectedElement.scale.y > 25) {
						selectedElement.scale.y = 25;
						changed = true;
					}
					if(selectedElement.scale.z > 25) {
						selectedElement.scale.z = 25;
						changed = true;
					}
					if(changed)setScale.accept(selectedElement.scale);
				}
				currentOp = new ValueOp<>(selectedElement, selectedElement.scale, (a, b) -> a.scale = b);
				break;
			default:
				break;
			}
			markDirty();
		}
	}

	private void posLimit(Vec3f pos, Consumer<Vec3f> setter) {
		boolean changed = false;
		if(Math.abs(pos.x) > Vec3f.MAX_POS) {
			if(pos.x < 0)pos.x = -Vec3f.MAX_POS;
			else pos.x = Vec3f.MAX_POS;
			changed = true;
		}
		if(Math.abs(pos.y) > Vec3f.MAX_POS) {
			if(pos.y < 0)pos.y = -Vec3f.MAX_POS;
			else pos.y = Vec3f.MAX_POS;
			changed = true;
		}
		if(Math.abs(pos.z) > Vec3f.MAX_POS) {
			if(pos.z < 0)pos.z = -Vec3f.MAX_POS;
			else pos.z = Vec3f.MAX_POS;
			changed = true;
		}
		if(changed)setter.accept(pos);
	}

	public void setName(String name) {
		if(selectedElement != null) {
			addUndo(new ValueOp<>(selectedElement, selectedElement.name, (a, b) -> a.name = b));
			selectedElement.name = name;
			currentOp = new ValueOp<>(selectedElement, selectedElement.name, (a, b) -> a.name = b);
			updateTree.accept(null);
			markDirty();
		}
	}

	public void switchMode() {
		if(selectedElement != null) {
			addUndo(new ValueOp<>(selectedElement, selectedElement.texture, (a, b) -> a.texture = b));
			selectedElement.texture = !selectedElement.texture;
			currentOp = new ValueOp<>(selectedElement, selectedElement.texture, (a, b) -> a.texture = b);
			setModeBtn.accept(selectedElement.texture);
			modeUpdate.accept(selectedElement.texture ? new Vec3i(selectedElement.u, selectedElement.v, selectedElement.textureSize) : new Vec3i(selectedElement.rgb, 0, 0));
			if(!selectedElement.texture || selectedElement.recolor)
				setPartColor.accept(selectedElement.rgb);
			else
				setPartColor.accept(null);
			markDirty();
		}
	}

	public void setColor(int color) {
		updateValueOp(selectedElement, selectedElement.rgb, color, (a, b) -> a.rgb = b, setPartColor);
	}

	public void setTex(float u, float v, float t) {
		if(selectedElement != null) {
			addUndo(
					new ValueOp<>(selectedElement, selectedElement.u, (a, b) -> a.u = b),
					new ValueOp<>(selectedElement, selectedElement.v, (a, b) -> a.v = b),
					new ValueOp<>(selectedElement, selectedElement.textureSize, (a, b) -> a.textureSize = b)
					);
			selectedElement.u = (int) u;
			selectedElement.v = (int) v;
			selectedElement.textureSize = (int) t;
			boolean refreshGui = false;
			if(selectedElement.u < 0) {
				selectedElement.u = 0;
				refreshGui = true;
			}
			if(selectedElement.v < 0) {
				selectedElement.v = 0;
				refreshGui = true;
			}
			if(selectedElement.textureSize < 1) {
				selectedElement.textureSize = 1;
				refreshGui = true;
			}
			currentOp = new OpList(
					new ValueOp<>(selectedElement, selectedElement.u, (a, b) -> a.u = b),
					new ValueOp<>(selectedElement, selectedElement.v, (a, b) -> a.v = b),
					new ValueOp<>(selectedElement, selectedElement.textureSize, (a, b) -> a.textureSize = b)
					);
			if(selectedElement.texture && refreshGui)
				modeUpdate.accept(new Vec3i(selectedElement.u, selectedElement.v, selectedElement.textureSize));
			markDirty();
		}
	}

	public void setMcScale(float value) {
		if(selectedElement != null) {
			addUndo(new ValueOp<>(selectedElement, selectedElement.mcScale, (a, b) -> a.mcScale = b));
			selectedElement.mcScale = value;
			if(selectedElement.mcScale > 7) {
				selectedElement.mcScale = 7;
				setMCScale.accept(selectedElement.mcScale);
			}
			if(selectedElement.mcScale < -7) {
				selectedElement.mcScale = -7;
				setMCScale.accept(selectedElement.mcScale);
			}
			currentOp = new ValueOp<>(selectedElement, selectedElement.mcScale, (a, b) -> a.mcScale = b);
			markDirty();
		}
	}

	public void addNew() {
		if(selectedElement != null) {
			ModelElement elem = new ModelElement(this);
			ModelElement sel = selectedElement;
			addUndo(() -> {
				sel.children.remove(elem);
				selectedElement = null;
			});
			runOp(() -> sel.children.add(elem));
			elem.parent = selectedElement;
			selectedElement = elem;
			markDirty();
			updateGui();
		}
	}

	public void deleteSel() {
		if(selectedElement != null && selectedElement.type == ElementType.NORMAL) {
			ModelElement elem = selectedElement;
			addUndo(() -> {
				if(elem.parent != null) {
					elem.parent.children.add(elem);
				}
			});
			runOp(() -> {
				if(selectedElement.parent != null) {
					selectedElement.parent.children.remove(selectedElement);
				}
				selectedElement = null;
			});
			markDirty();
			updateGui();
		}
	}

	public void switchVis() {
		updateValueOp(selectedElement, selectedElement.show, !selectedElement.show, (a, b) -> a.show = b, setVis);
	}

	public void switchMirror() {
		updateValueOp(selectedElement, selectedElement.mirror, !selectedElement.mirror, (a, b) -> a.mirror = b, setMirror);
	}

	public void switchGlow() {
		updateValueOp(selectedElement, selectedElement.glow, !selectedElement.glow, (a, b) -> a.glow = b, setGlow);
	}

	public void switchHide() {
		if(selectedElement != null && selectedElement.type == ElementType.ROOT_PART) {
			switchVis();
			setHiddenEffect.accept(!selectedElement.show);
		} else
			updateValueOp(selectedElement, selectedElement.hidden, !selectedElement.hidden, (a, b) -> a.hidden = b, setHiddenEffect);
	}

	public void setTexSize(int x, int y) {
		markDirty();
		int sx = skinProvider.size.x;
		int sy = skinProvider.size.y;
		addUndo(() -> {
			skinProvider.size.x = sx;
			skinProvider.size.y = sy;
		});
		runOp(() -> {
			skinProvider.size.x = x;
			skinProvider.size.y = y;
		});
	}

	public void switchReColorEffect() {
		if(selectedElement != null) {
			addUndo(new ValueOp<>(selectedElement, selectedElement.recolor, (a, b) -> a.recolor = b));
			selectedElement.recolor = !selectedElement.recolor;
			setReColor.accept(selectedElement.recolor);
			if(!selectedElement.texture || selectedElement.recolor)
				setPartColor.accept(selectedElement.rgb);
			else
				setPartColor.accept(null);
			currentOp = new ValueOp<>(selectedElement, selectedElement.recolor, (a, b) -> a.recolor = b);
			markDirty();
		}
	}

	public void drawPixel(int x, int y) {
		switch (drawMode) {
		case 0:
			setPixel(x, y, penColor | 0xff000000);
			break;

		case 1:
			setPixel(x, y, 0);
			break;

		default:
			break;
		}
	}

	public void setPixel(int x, int y, int color) {
		int old = skinProvider.getImage().getRGB(x, y);
		if(old == color)return;
		if(!skinProvider.isEdited())setSkinEdited.accept(true);
		addUndo(() -> skinProvider.setRGB(x, y, old));
		runOp(() -> skinProvider.setRGB(x, y, color));
		markDirty();
	}

	public void markDirty() {
		setNameDisplay.accept((file == null ? gui.getGui().i18nFormat("label.cpm.new_project") : file.getName()) + "*");
		dirty = true;
		redoQueue.clear();
		setUndoEn.accept(true);
		setRedoEn.accept(false);
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
		modeUpdate.accept(null);
		setVis.accept(null);
		setDelEn.accept(false);
		setAddEn.accept(selectedElement != null);
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
		if(selectedElement != null) {
			setVis.accept(selectedElement.show);
			switch(selectedElement.type) {
			case NORMAL:
				setOffset.accept(selectedElement.offset);
				setRot.accept(selectedElement.rotation);
				setPosition.accept(selectedElement.pos);
				setSize.accept(selectedElement.size);
				setScale.accept(selectedElement.scale);
				setMCScale.accept(selectedElement.mcScale);
				setMirror.accept(selectedElement.mirror);
				updateName.accept(selectedElement.name);
				setModeBtn.accept(selectedElement.texture);
				modeUpdate.accept(new Vec3i(selectedElement.u, selectedElement.v, selectedElement.textureSize));
				if(!selectedElement.texture || selectedElement.recolor)
					setPartColor.accept(selectedElement.rgb);
				setDelEn.accept(true);
				setGlow.accept(selectedElement.glow);
				setReColor.accept(selectedElement.recolor);
				setHiddenEffect.accept(selectedElement.hidden);
				break;

			case ROOT_PART:
				setPosition.accept(selectedElement.pos);
				setRot.accept(selectedElement.rotation);
				setHiddenEffect.accept(!selectedElement.show);
				break;

			default:
				break;
			}
		}
		setNameDisplay.accept((file == null ? gui.getGui().i18nFormat("label.cpm.new_project") : file.getName()) + (dirty ? "*" : ""));
		setUndoEn.accept(!undoQueue.empty());
		setRedoEn.accept(!redoQueue.empty());
		if(selectedAnim != null) {
			AnimFrame selFrm = selectedAnim.getSelectedFrame();
			if(selFrm != null) {
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
		}
		setSelAnim.accept(selectedAnim);
		setSkinEdited.accept(skinProvider.isEdited());
		updateTree.accept(null);
	}

	public void loadDefaultPlayerModel() {
		project = new ProjectFile();
		elements.clear();
		animations.clear();
		skinProvider.free();
		skinProvider.texture = null;
		skinProvider.setEdited(false);
		if(listIconProvider != null)listIconProvider.free();
		listIconProvider = null;
		undoQueue.clear();
		redoQueue.clear();
		skinType = MinecraftClientAccess.get().getSkinType();
		Image skin = MinecraftClientAccess.get().getVanillaSkin(skinType);
		this.vanillaSkin = skin;
		skinProvider.setImage(new Image(skin));
		skinProvider.size = new Vec2i(skin.getWidth(), skin.getHeight());
		dirty = false;
		file = null;
		skinFile = null;
		selectedElement = null;
		selectedAnim = null;
		currentOp = null;
		animEnc = null;
		storeIDgen = new StoreIDGen();
		Player profile = MinecraftClientAccess.get().getClientPlayer();
		profile.loadSkin(() -> {
			skinType = profile.getSkinType();
			CompletableFuture<Image> img = profile.getSkin();
			this.vanillaSkin = MinecraftClientAccess.get().getVanillaSkin(skinType);
			img.thenAccept(s -> {
				if(!skinProvider.isEdited()) {
					if(s != null) {
						this.vanillaSkin = s;
						skinProvider.setImage(new Image(this.vanillaSkin));
					} else {
						skinProvider.setImage(new Image(this.vanillaSkin));
					}
				}
			});
		});
		for(PlayerModelParts type : PlayerModelParts.values()) {
			if(type != PlayerModelParts.CUSTOM_PART)
				elements.add(new ModelElement(this, ElementType.ROOT_PART, type, gui.getGui()));
		}
	}

	public void preRender() {
		elements.forEach(ModelElement::preRender);
		if(this.applyAnim && this.selectedAnim != null) {
			if(this.playFullAnim) {
				long playTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
				long currentStep = (playTime - this.playStartTime);
				this.selectedAnim.applyPlay(currentStep);
				if(currentStep > this.selectedAnim.duration && !this.selectedAnim.loop && this.selectedAnim.pose == null){
					this.playFullAnim = false;
				}
			} else {
				this.selectedAnim.apply();
			}
		}
	}

	public void save(File file) throws IOException {
		Map<String, Object> data = new HashMap<>();
		List<Map<String, Object>> lst = new ArrayList<>();
		data.put("elements", lst);
		for (ModelElement elem : elements) {
			Map<String, Object> map = new HashMap<>();
			lst.add(map);
			map.put("id", ((PlayerModelParts) elem.typeData).name().toLowerCase());
			map.put("show", elem.show);
			if(!elem.children.isEmpty()) {
				List<Map<String, Object>> list = new ArrayList<>();
				map.put("children", list);
				saveChildren(elem, list);
			}
			map.put("pos", elem.pos.toMap());
			map.put("rotation", elem.rotation.toMap());
		}
		data.put("version", projectFileVersion);
		Map<String, Object> skinSize = new HashMap<>();
		data.put("skinSize", skinSize);
		skinSize.put("x", skinProvider.size.x);
		skinSize.put("y", skinProvider.size.y);
		try(OutputStreamWriter os = new OutputStreamWriter(project.setAsStream("config.json"))) {
			gson.toJson(data, os);
		}
		if(skinProvider.texture != null && skinProvider.isEdited()) {
			try(OutputStream os = project.setAsStream("skin.png")) {
				skinProvider.texture.getImage().storeTo(os);
			}
		}
		project.clearFolder("animations");
		for (EditorAnim e : animations) {
			data = new HashMap<>();
			data.put("additive", e.add);
			if(e.gestureName != null)data.put("name", e.gestureName);
			if(e.pose instanceof CustomPose)data.put("name", ((CustomPose)e.pose).getName());
			data.put("duration", e.duration);
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
		project.save(file);
		this.file = file;
		dirty = false;
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
		byte[] ze = project.getEntry("skin.png");
		if(ze != null) {
			skinProvider.setImage(Image.loadFrom(new ByteArrayInputStream(ze)));
			skinProvider.markDirty();
		}
		int fileVersion = ((Number) data.getOrDefault("version", 0)).intValue();
		if(fileVersion != projectFileVersion)throw new IOException("Unsupported file version try a newer version of the mod");
		List<Map<String, Object>> lst = (List<Map<String, Object>>) data.get("elements");
		this.file = file;
		for (Map<String, Object> map : lst) {
			String key = (String) map.get("id");
			for (ModelElement elem : elements) {
				if(((PlayerModelParts) elem.typeData).name().equalsIgnoreCase(key)) {
					elem.show = (boolean) map.get("show");
					if(map.containsKey("children")) {
						loadChildren((List<Map<String, Object>>) map.get("children"), elem, fileVersion);
					}
					elem.pos = new Vec3f((Map<String, Object>) map.get("pos"), new Vec3f(0, 0, 0));
					elem.rotation = new Vec3f((Map<String, Object>) map.get("rotation"), new Vec3f(0, 0, 0));
				}
			}
		}
		List<String> anims = project.listEntires("animations");
		Map<String, Object> skinTexSize = (Map<String, Object>) data.get("skinSize");
		skinProvider.size = new Vec2i(((Number)skinTexSize.get("x")).intValue(), ((Number)skinTexSize.get("y")).intValue());
		if(anims != null) {
			for (String anim : anims) {
				try(InputStreamReader rd = new InputStreamReader(project.getAsStream("animations/" + anim))) {
					data = (Map<String, Object>) gson.fromJson(rd, Object.class);
				}
				IPose pose = null;
				String gesture = null;
				if(anim.startsWith("v_")) {
					String poseName = anim.substring(2, anim.length() - 5);
					for(VanillaPose p : VanillaPose.VALUES) {
						if(p.name().equalsIgnoreCase(poseName)) {
							pose = p;
							break;
						}
					}
				} else if(anim.startsWith("c_")) {
					pose = new CustomPose((String) data.get("name"));
				} else if(anim.startsWith("g_")) {
					gesture = (String) data.get("name");
				}
				if(pose == null && gesture == null)continue;
				EditorAnim e = new EditorAnim(this, anim, false);
				e.gestureName = gesture;
				e.pose = pose;
				e.add = (boolean) data.get("additive");
				animations.add(e);
				e.duration = ((Number)data.get("duration")).intValue();
				List<Map<String, Object>> frames = (List<Map<String, Object>>) data.get("frames");
				for (Map<String,Object> map : frames) {
					e.loadFrame(map);
				}
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
			elem.storeID = ((Number) map.getOrDefault("storeID", 0)).longValue();

			if(map.containsKey("children")) {
				loadChildren((List<Map<String, Object>>) map.get("children"), elem, fileVer);
			}
		}
	}

	public void reloadSkin() {
		if(skinFile != null) {
			try {
				Image img = Image.loadFrom(skinFile);
				if(img.getWidth() > 512 || img.getHeight() > 512)
					throw new IOException(gui.getGui().i18nFormat("label.cpm.tex_size_too_big", 512));
				skinProvider.setImage(img);
				skinProvider.setEdited(true);
				setSkinEdited.accept(true);
			} catch (IOException e) {
				e.printStackTrace();
				gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("error.cpm.img_load_failed", e.getLocalizedMessage())));
			}
		}
	}

	public void saveSkin(File f) {
		try {
			skinProvider.getImage().storeTo(f);
			skinFile = f;
		} catch (IOException e) {
			gui.openPopup(new MessagePopup(gui.getGui(), gui.getGui().i18nFormat("label.cpm.error"), gui.getGui().i18nFormat("error.cpm.img_save_failed", e.getLocalizedMessage())));
		}

	}

	public void addUndo(Runnable... r) {
		if(r.length == 1)undoQueue.push(r[0]);
		else undoQueue.push(new OpList(r));
	}

	public void appendCurrentOp(Runnable r) {
		if(currentOp == null)currentOp = r;
		else if(currentOp instanceof OpList) {
			((OpList)currentOp).rs.add(r);
		} else {
			currentOp = new OpList(currentOp, r);
		}
	}

	public void runOp(Runnable r) {
		currentOp = r;
		r.run();
	}

	private static class OpList implements Runnable {
		private List<Runnable> rs;

		public OpList(Runnable... r) {
			rs = new ArrayList<>(Arrays.asList(r));
		}

		@Override
		public void run() {
			rs.forEach(Runnable::run);
		}
	}

	public <E, T> void updateValueOp(E elem, T currVal, T newVal, BiConsumer<E, T> setter, Updater<T> updater) {
		if(elem == null)return;
		addUndo(new ValueOp<>(elem, currVal, setter));
		updater.accept(newVal);
		runOp(new ValueOp<>(elem, newVal, setter));
		markDirty();
	}

	public void undo() {
		if(undoQueue.empty())return;
		Runnable r = undoQueue.pop();
		if(r != null) {
			if(currentOp != null) {
				redoQueue.add(currentOp);
				currentOp = r;
				r.run();
			}
		}
		updateGui();
	}

	public void redo() {
		if(redoQueue.empty())return;
		Runnable r = redoQueue.pop();
		if(r != null) {
			if(currentOp != null) {
				undoQueue.add(currentOp);
				currentOp = r;
				r.run();
			}
		}
		updateGui();
	}

	public void moveElement(ModelElement element, ModelElement to) {
		if(checkChild(element.children, to))return;
		ModelElement me = element.parent;
		addUndo(() -> {
			to.children.remove(element);
			element.parent = me;
			me.children.add(element);
		});
		runOp(() -> {
			element.parent.children.remove(element);
			element.parent = to;
			to.children.add(element);
		});
		selectedElement = null;
		markDirty();
		updateGui();
	}

	private boolean checkChild(List<ModelElement> elem, ModelElement to) {
		for (ModelElement modelElement : elem) {
			if(modelElement == to)return true;
			if(checkChild(elem, to))return true;
		}
		return false;
	}

	public void walkElements(List<ModelElement> elem, Consumer<ModelElement> c) {
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

	public void addNewAnim(IPose pose, String gesture, boolean add, boolean loop) {
		String fname = null;
		if(pose instanceof VanillaPose) {
			fname = "v_" + ((VanillaPose)pose).name().toLowerCase() + ".json";
		} else if(pose != null) {
			fname = "c_" + ((CustomPose) pose).getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
		} else {
			fname = "g_" + gesture.replaceAll("[^a-zA-Z0-9\\.\\-]", "") + "_" + (storeIDgen.newId() % 10000) + ".json";
		}
		EditorAnim anim = new EditorAnim(this, fname, true);
		anim.pose = pose;
		anim.add = add;
		anim.loop = loop;
		anim.gestureName = gesture;
		addUndo(() -> {
			animations.remove(anim);
			selectedAnim = null;
		});
		runOp(() -> animations.add(anim));
		selectedAnim = anim;
		markDirty();
		updateGui();
	}

	public void delSelectedAnim() {
		if(selectedAnim != null) {
			EditorAnim anim = selectedAnim;
			addUndo(() -> animations.add(anim));
			runOp(() -> animations.remove(anim));
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
		updateValueOp(selectedAnim, selectedAnim.duration, value, (a, b) -> a.duration = b, setAnimDuration);
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

	public void switchAnimShow() {
		if(selectedAnim != null) {
			selectedAnim.switchVisible();
			updateGui();
		}
	}

	public void applyRenderPoseForAnim(Consumer<VanillaPose> func) {
		if(applyAnim && selectedAnim != null && selectedAnim.pose instanceof VanillaPose) {
			func.accept((VanillaPose) selectedAnim.pose);
		}
	}

	public static class AnimationEncodingData {
		public Set<PlayerSkinLayer> freeLayers;
		public EnumMap<PlayerSkinLayer, Boolean> defaultLayerValue;

		public AnimationEncodingData() {
			freeLayers = new HashSet<>();
			defaultLayerValue = new EnumMap<>(PlayerSkinLayer.class);
		}

		public AnimationEncodingData(AnimationEncodingData cpy) {
			freeLayers = new HashSet<>(cpy.freeLayers);
			defaultLayerValue = new EnumMap<>(cpy.defaultLayerValue);
		}
	}

	public UIColors colors() {
		return gui.getGui().getColors();
	}

	public void addSkinLayer() {
		OpList l = new OpList();
		OpList u = new OpList(() -> selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					ModelElement elem = new ModelElement(this);
					l.rs.add(() -> el.children.add(elem));
					u.rs.add(() -> el.children.remove(elem));
					elem.parent = el;
					PlayerPartValues val = PlayerPartValues.getFor(p, skinType);
					elem.size = val.getSize();
					elem.offset = val.getOffset();
					elem.texture = true;
					elem.u = val.u2;
					elem.v = val.v2;
					elem.name = gui.getGui().i18nFormat("label.cpm.layer_" + val.layer.getLowerName());
					elem.mcScale = 0.25F;
					break;
				}
			}
		}
		runOp(l);
		addUndo(u);
		markDirty();
		updateGui();
	}

	public void convertModel() {
		OpList l = new OpList();
		OpList u = new OpList(() -> selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					if(el.show) {
						ModelElement elem = new ModelElement(this);
						l.rs.add(() -> el.children.add(elem));
						u.rs.add(() -> el.children.remove(elem));
						elem.parent = el;
						PlayerPartValues val = PlayerPartValues.getFor(p, skinType);
						elem.size = val.getSize();
						elem.offset = val.getOffset();
						elem.texture = true;
						elem.u = val.u;
						elem.v = val.v;
						elem.name = el.name;
						l.rs.add(() -> el.show = false);
						u.rs.add(() -> el.show = true);
					}
					break;
				}
			}
		}
		runOp(l);
		addUndo(u);
		markDirty();
		updateGui();
	}
}
