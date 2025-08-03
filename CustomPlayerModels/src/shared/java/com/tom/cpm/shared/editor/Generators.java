package com.tom.cpm.shared.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.ChooseElementPopup;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.Direction.Axis;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.actions.ImageAction;
import com.tom.cpm.shared.editor.anim.AnimFrame.FrameData;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.elements.RootGroups;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.popup.ExportUVMapPopup;
import com.tom.cpm.shared.editor.template.TemplateSettings;
import com.tom.cpm.shared.editor.util.QuickTask;
import com.tom.cpm.shared.editor.util.SafetyLevel;
import com.tom.cpm.shared.editor.util.SafetyLevel.SafetyReport;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.PlayerPartValues;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.model.render.PerFaceUV;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureType;

public class Generators {
	public static List<Generators> generators = new ArrayList<>();

	static {
		register("button.cpm.tools.convert_model_custom", "tooltip.cpm.tools.convert_model_custom", eg -> convertModel(eg.getEditor()));
		register("button.cpm.tools.add_skin_layer2", null, eg -> addSkinLayer(eg.getEditor()));
		register("button.cpm.tools.convert2template", null, Generators::convertTemplate);
		register("button.cpm.tools.convert2model", null, Generators::convertModel);
		register("button.cpm.tools.fillUV", null, Generators::fillUV);
		register("button.cpm.tools.safetyLevel", null, Generators::checkSafetyLevel);
		register("button.cpm.tools.mirror", null, Generators::mirrorElement);
		register("button.cpm.tools.exportUVMap", "tooltip.cpm.tools.exportUVMap", Generators::exportUVMap);
		register("button.cpm.tools.make2ndLayer", "tooltip.cpm.tools.make2ndLayer", Generators::make2ndLayer);
		register("button.cpm.tools.makeArmorLayer", "tooltip.cpm.tools.makeArmorLayer", Generators::makeArmorLayer);
	}

	public String name;
	public FormatText tooltip;
	public Consumer<EditorGui> func;

	public Generators(String name, FormatText tooltip, Consumer<EditorGui> func) {
		this.name = name;
		this.tooltip = tooltip;
		this.func = func;
	}

	private static void register(String name, String tooltip, Consumer<EditorGui> func) {
		generators.add(new Generators(name, tooltip == null ? null : new FormatText(tooltip), func));
	}

	private static void addSkinLayer(Editor e) {
		ActionBuilder ab = e.action("i", "button.cpm.tools.add_skin_layer2");
		ab.onUndo(() -> e.selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					ModelElement elem = new ModelElement(e);
					ab.addToList(el.children, elem);
					elem.parent = el;
					PlayerPartValues val = PlayerPartValues.getFor(p, e.skinType);
					elem.size = val.getSize();
					elem.offset = val.getOffset();
					elem.texture = true;
					elem.u = val.u2;
					elem.v = val.v2;
					elem.name = e.ui.i18nFormat("label.cpm.layer_" + val.layer.getLowerName());
					elem.mcScale = 0.25F;
					break;
				}
			}
		}
		ab.execute();
		e.updateGui();
	}

	public static void convertModel(Editor e) {
		ActionBuilder ab = e.action("i", "button.cpm.tools.convert_model_custom");
		ab.onUndo(() -> e.selectedElement = null);
		for (ModelElement el : e.elements) {
			if(el.type == ElementType.ROOT_PART) {
				if(!el.hidden) {
					ModelElement elem = new ModelElement(e);
					ab.addToList(el.children, elem);
					elem.parent = el;
					PartValues val = ((VanillaModelPart) el.typeData).getDefaultSize(e.skinType);
					elem.size = val.getSize();
					elem.offset = val.getOffset();
					elem.texture = true;
					elem.mcScale = val.getMCScale();
					elem.mirror = val.isMirror();
					Vec2i uv = val.getUV();
					elem.u = uv.x;
					elem.v = uv.y;
					elem.name = e.ui.i18nFormat("label.cpm.elem." + ((VanillaModelPart) el.typeData).getName());
					elem.generated = true;
					ab.updateValueOp(el, false, true, (a, b) -> a.hidden = b);
				}
			}
		}
		ab.execute();
		e.updateGui();
	}

	public static void setupTemplateModel(Editor e) {
		ActionBuilder ab = e.action("i", "button.cpm.tools.convert2template");
		ab.onUndo(() -> e.selectedElement = null);
		for(PlayerModelParts p : PlayerModelParts.VALUES) {
			for (ModelElement el : e.elements) {
				if(el.type == ElementType.ROOT_PART && el.typeData == p) {
					if(!el.hidden) {
						ModelElement elem = new ModelElement(e);
						ab.addToList(el.children, elem);
						elem.parent = el;
						PlayerPartValues val = PlayerPartValues.getFor(p, e.skinType);
						elem.size = val.getSize();
						elem.offset = val.getOffset();
						elem.texture = false;
						elem.rgb = 0xffffff;
						elem.name = el.name;
						elem.templateElement = true;
						ab.updateValueOp(el, false, true, (a, b) -> a.hidden = b);
					}
					break;
				}
			}
		}
		ab.execute();
	}

	private static void convertTemplate(EditorGui eg) {
		Editor editor = eg.getEditor();
		IGui gui = eg.getGui();
		if (editor.templateSettings == null) {
			if (editor.dirty) {
				eg.openPopup(new MessagePopup(eg, gui.i18nFormat("label.cpm.info"), gui.i18nFormat("label.cpm.must_save")));
			} else {
				if(editor.file == null)
					setupTemplate(editor);
				else
					eg.openPopup(new ConfirmPopup(eg, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.warnTemplate"),
							new ConfirmPopup(eg, gui.i18nFormat("label.cpm.warning"), gui.i18nFormat("label.cpm.warn_c2t"), () -> setupTemplate(editor), null),
							null));
			}
		}
	}

	private static void convertModel(EditorGui eg) {
		Editor editor = eg.getEditor();
		IGui gui = eg.getGui();
		if (editor.templateSettings != null) {
			if (editor.dirty) {
				eg.openPopup(new MessagePopup(eg, gui.i18nFormat("label.cpm.info"), gui.i18nFormat("label.cpm.must_save")));
			} else {
				editor.templateSettings = null;
				for (ModelElement el : editor.elements) {
					el.children.removeIf(e -> e.templateElement);
				}
				editor.markDirty();
				editor.updateGui();
			}
		}
	}

	private static void setupTemplate(Editor editor) {
		editor.templateSettings = new TemplateSettings(editor);
		setupTemplateModel(editor);
		editor.markDirty();
		editor.updateGui();
	}

	private static void fillUV(EditorGui eg) {
		Editor editor = eg.getEditor();
		ActionBuilder ab = editor.action("i", "button.cpm.tools.fillUV");
		Set<ETextures> texs = new HashSet<>();
		editor.forEachSeletectedElement(el -> {
			if(el instanceof ModelElement) {
				ModelElement me = (ModelElement) el;
				if(me.type == ElementType.NORMAL && me.texture) {
					if (me.faceUV != null) {
						Face f = me.faceUV.get(editor.perfaceFaceDir.get());
						if (f != null) {
							Box box = Box.fromArea(f.sx, f.sy, f.ex, f.ey);
							ab.action(new ImageAction(me.getTexture().getImage(), box, img -> {
								img.fill(box.x, box.y, box.w, box.h, 0xff888888);
							}));
							texs.add(me.getTexture());
						}
					} else {
						Box box = me.getTextureBox();
						int ts = Math.abs(me.texSize);
						int bx = me.u * ts;
						int by = me.v * ts;
						int dx = MathHelper.ceil(me.size.x * ts);
						int dy = MathHelper.ceil(me.size.y * ts);
						int dz = MathHelper.ceil(me.size.z * ts);
						ab.action(new ImageAction(me.getTexture().getImage(), box, img -> {
							img.fill(bx + dx + dz, by + dz, dz, dy, 0xffff0000);
							img.fill(bx, by + dz, dz, dy, 0xffdd0000);
							img.fill(bx + dz, by, dx, dz, 0xff00ff00);
							img.fill(bx + dz + dx, by, dx, dz, 0xff00dd00);
							img.fill(bx + dz, by + dz, dx, dy, 0xff0000ff);
							img.fill(bx + dz * 2 + dx, by + dz, dx, dy, 0xff0000dd);
						}));
						texs.add(me.getTexture());
					}
				}
			}
		});
		ab.onAction(() -> texs.forEach(ETextures::markDirty));
		ab.execute();
	}

	public static void addItemHoldPos(Editor editor) {
		addSlots(editor, ItemSlot.SLOTS);
	}

	public static void addParrots(Editor editor) {
		addSlots(editor, ItemSlot.PARROTS);
	}

	private static void addSlots(Editor editor, ItemSlot[] slots) {
		ActionBuilder ab = editor.action("add", "action.cpm.itemHold");
		Set<ItemSlot> added = new HashSet<>();
		Editor.walkElements(editor.elements, e -> {
			if(e.itemRenderer != null)added.add(e.itemRenderer.slot);
		});
		for (ItemSlot itemSlot : slots) {
			if(!added.contains(itemSlot)) {
				ModelElement elem = new ModelElement(editor);
				elem.itemRenderer = new ItemRenderer(itemSlot, 0);
				elem.name = editor.ui.i18nFormat("label.cpm.elem.item." + itemSlot.name().toLowerCase(Locale.ROOT));
				elem.size = new Vec3f(0, 0, 0);
				switch (itemSlot) {
				case HEAD:
					elem.parent = findPart(editor, PlayerModelParts.HEAD);
					break;
				case LEFT_HAND:
					elem.parent = findPart(editor, PlayerModelParts.LEFT_ARM);
					break;
				case RIGHT_HAND:
					elem.parent = findPart(editor, PlayerModelParts.RIGHT_ARM);
					break;
				case RIGHT_SHOULDER:
				case LEFT_SHOULDER:
					elem.parent = findPart(editor, PlayerModelParts.BODY);
					break;
				default:
					break;
				}
				if(elem.parent != null) {
					ab.addToList(elem.parent.children, elem);
				}
			}
		}
		ab.execute();
		editor.updateGui();
	}

	private static ModelElement findPart(Editor editor, VanillaModelPart part) {
		for(ModelElement e : editor.elements) {
			if(e.typeData == part) {
				return e;
			}
		}
		return null;
	}

	public static void loadTextures(Editor editor, RootGroups group, BiConsumer<TextureSheetType, ETextures> texs) {
		Arrays.stream(group.types).map(group::getTexSheet).distinct().forEach(tx -> {
			if(!editor.textures.containsKey(tx)) {
				ETextures tex = new ETextures(editor, tx);
				texs.accept(tx, tex);
				tex.provider.size = tx.getDefSize();
				Image def = new Image(tex.provider.size.x, tex.provider.size.y);
				try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/" + tx.name().toLowerCase(Locale.ROOT) + ".png")) {
					def = Image.loadFrom(is);
				} catch (IOException e) {
				}
				tex.setDefaultImg(def);
				tex.setImage(new Image(def));
				tex.markDirty();
				tex.setEdited(tx.texType == null && tx.editable);
				tex.setChangedLocally(false);
				if(tx.texType != null) {
					Player<?> profile = MinecraftClientAccess.get().getClientPlayer();
					profile.getTextures().load().thenRun(() -> loadTexture(tex, profile, tx.texType));
				}
			}
		});
	}

	private static void loadTexture(ETextures tex, Player<?> profile, TextureType type) {
		CompletableFuture<Image> img = profile.getTextures().getTexture(type);
		img.thenAccept(s -> {
			if(!tex.isEdited()) {
				if(s != null) {
					tex.setDefaultImg(s);
					tex.setImage(new Image(s));
					tex.setChangedLocally(false);
					tex.restitchTexture();
				} else if(type == TextureType.ELYTRA) {
					loadTexture(tex, profile, TextureType.CAPE);
				} else if(type == TextureType.CAPE) {
					Image.download("http://s.optifine.net/capes/" + profile.getName() + ".png").thenAccept(i -> {
						if(!tex.isEdited()) {
							if(i != null) {
								tex.setDefaultImg(i);
								tex.setImage(new Image(i));
								tex.setChangedLocally(false);
								tex.restitchTexture();
							}
						}
					});
				}
			}
		});
	}

	private static void checkSafetyLevel(EditorGui eg) {
		IGui gui = eg.getGui();
		SafetyReport report = SafetyLevel.getLevel(eg.getEditor());
		List<String> msg = new ArrayList<>();
		String lvl = gui.i18nFormat("label.cpm.safetyLevel", gui.i18nFormat("label.cpm.safetyProfile." + report.getLvl()));
		msg.add(lvl);
		report.details.stream().map(t -> t.toString(gui)).forEach(msg::add);
		collectAnimationDetails(eg, msg);
		eg.openPopup(new MessagePopup(eg, gui.i18nFormat("label.cpm.info"), String.join("\\", msg)));
	}

	private static void collectAnimationDetails(EditorGui eg, List<String> msgs) {
		Editor e = eg.getEditor();
		long layerCnt = e.animations.stream().
				filter(a -> a.type.isLayer()).map(a -> a.getId()).distinct().count();
		long poseCnt = e.animations.stream().
				filter(a -> a.type == AnimationType.CUSTOM_POSE).map(a -> a.getId()).distinct().count();
		long gestureCnt = e.animations.stream().
				filter(a -> a.type == AnimationType.GESTURE).map(a -> a.getId()).distinct().count();
		long layerCtrlCnt = e.animations.stream().
				filter(a -> (a.type == AnimationType.GESTURE || a.type == AnimationType.CUSTOM_POSE) && a.layerControlled).
				map(a -> a.getId()).distinct().count();
		msgs.add(eg.getGui().i18nFormat("label.cpm.safety.animCnt.pose", poseCnt, 253));
		msgs.add(eg.getGui().i18nFormat("label.cpm.safety.animCnt.gesture", gestureCnt, 253));
		msgs.add(eg.getGui().i18nFormat("label.cpm.safety.animCnt.layer", layerCnt, 253));
		if (e.animEnc != null) {
			int limit = (1 << e.animEnc.freeLayers.size()) - 2;
			msgs.add(eg.getGui().i18nFormat("label.cpm.safety.animCnt.layerCtrl", layerCtrlCnt, limit));
		} else  {
			msgs.add(eg.getGui().i18nFormat("label.cpm.safety.animCnt.layerCtrl", layerCtrlCnt, "?"));
		}
	}

	private static void mirrorElement(EditorGui eg) {
		Editor editor = eg.getEditor();
		ActionBuilder b = eg.getEditor().action("i", "button.cpm.tools.mirror");
		Set<ModelElement> mirrored = new HashSet<>();
		editor.forEachSeletectedElement(el -> {
			if(el instanceof ModelElement) {
				ModelElement me = (ModelElement) el;
				if(me.type == ElementType.NORMAL) {
					mirrorZ(me, b, mirrored);
				}
			}
		});
		b.execute();
		editor.setQuickAction.accept(new QuickTask(editor.ui.i18nFormat("button.cpm.mirrorAnimations"), editor.ui.i18nFormat("tooltip.cpm.mirrorAnimations"), () -> {
			ActionBuilder ab = eg.getEditor().action("i", "button.cpm.tools.mirror");
			editor.animations.forEach(a -> a.getFrames().forEach(f -> mirrored.forEach(me -> {
				FrameData dt = f.getComponents().get(me);
				if(dt != null)dt.mirror(ab);
			})));
			ab.onAction(() -> editor.animations.forEach(EditorAnim::clearCache));
			ab.execute();
		}));
	}

	private static void mirrorZ(ModelElement e, ActionBuilder b, Set<ModelElement> mirrored) {
		if(mirrored.contains(e))return;
		mirrored.add(e);
		if(e.faceUV != null) {
			e.faceUV.mirror(b, Axis.Z);
		} else
			b.updateValueOp(e, e.mirror, !e.mirror, (a, c) -> a.mirror = c);
		Vec3f s = e.size;
		Vec3f v = new Vec3f(e.pos);
		v.x = -v.x;
		b.updateValueOp(e, e.pos, v, -FormatLimits.getVectorLimit(), FormatLimits.getVectorLimit(), false, (a, c) -> a.pos = c, vec -> {});
		v = new Vec3f(e.offset);
		v.x = -v.x - s.x;
		b.updateValueOp(e, e.offset, v, -FormatLimits.getVectorLimit(), FormatLimits.getVectorLimit(), false, (a, c) -> a.offset = c, vec -> {});
		v = new Vec3f(e.rotation);
		v.y = 360 - v.y;
		v.z = 360 - v.z;
		b.updateValueOp(e, e.rotation, v, 0, 360, true, (a, c) -> a.rotation = c, vec -> {});
		e.children.forEach(p -> mirrorZ(p, b, mirrored));
		b.onAction(() -> e.markDirty());
	}

	public static void fixAdditive(Editor editor) {
		if(editor.selectedAnim != null) {
			boolean add = editor.selectedAnim.add;
			ActionBuilder ab = editor.action("i", "button.cpm.fixAdditiveToggle");
			editor.selectedAnim.getFrames().forEach(frm -> frm.getComponents().forEach((p, f) -> {
				Vec3f pos;
				if (p.type == ElementType.ROOT_PART) {
					VanillaModelPart part = (VanillaModelPart) p.typeData;
					PartValues pv = part.getDefaultSize(editor.skinType);
					pos = pv.getPos().add(p.pos);
				} else {
					pos = p.pos;
				}
				pos = add ? f.getPosition().sub(pos) : f.getPosition().add(pos);
				Vec3f rot = add ? f.getRotation().sub(p.rotation) : f.getRotation().add(p.rotation);
				ab.updateValueOp(f, f.getPosition(), pos, -FormatLimits.getVectorLimit(), FormatLimits.getVectorLimit(), false, FrameData::setPos, v -> {});
				ab.updateValueOp(f, f.getRotation(), rot, 0, 360, true, FrameData::setRot, v -> {});
			}));
			ab.onAction(editor.selectedAnim, EditorAnim::clearCache);
			ab.onRun(editor::updateGui);
			ab.execute();
		}
	}

	private static void exportUVMap(EditorGui eg) {
		eg.openPopup(new ExportUVMapPopup(eg));
	}

	private static void make2ndLayer(EditorGui eg) {
		Editor editor = eg.getEditor();
		ActionBuilder b = eg.getEditor().action("i", "button.cpm.tools.make2ndLayer");
		editor.forEachSeletectedElement(el -> {
			if(el instanceof ModelElement) {
				ModelElement me = (ModelElement) el;
				if(me.type == ElementType.NORMAL) {
					ModelElement elem = new ModelElement(editor);
					b.addToList(me.children, elem);
					elem.parent = me;
					elem.size = new Vec3f(me.size);
					elem.offset = new Vec3f(me.offset);
					elem.texture = me.texture;
					if (me.texture) {
						if (me.faceUV != null) {
							elem.faceUV = new PerFaceUV(me.faceUV);
							elem.faceUV.faces.values().forEach(f -> {
								int d = Math.abs(f.sx - f.ex);
								f.sx += d;
								f.ex += d;
							});
						} else {
							elem.u = me.u + me.getTextureBox().w;
							elem.v = me.v;
							elem.texSize = me.texSize;
							elem.singleTex = me.singleTex;
						}
					}
					elem.name = editor.ui.i18nFormat("label.cpm.generated2ndLayer", me.name);
					elem.mcScale = 0.25F;
				}
			}
		});
		b.onRun(editor::updateGui);
		b.execute();
	}

	private static void makeArmorLayer(EditorGui eg) {
		Editor editor = eg.getEditor();
		NameMapper<RootModelType> map = new NameMapper<>(RootGroups.ARMOR.types, n -> editor.ui.i18nFormat("label.cpm.elem." + n.getName()));
		eg.openPopup(new ChooseElementPopup<>(eg, editor.ui.i18nFormat("label.cpm.model.selectArmorRootToMake"), map.asList(), root -> {
			RootModelType rmt = root.getElem();
			ModelElement addTo = findPart(editor, rmt);

			if (addTo == null) {
				editor.ui.displayMessagePopup(editor.ui.i18nFormat("label.cpm.error"), editor.ui.i18nFormat("label.cpm.model.rootNotFound"));
				return;
			}

			ActionBuilder b = eg.getEditor().action("i", "button.cpm.tools.makeArmorLayer");
			Map<ModelElement, ModelElement> ctLookup = new HashMap<>();
			editor.forEachSeletectedElement(el -> {
				if(el instanceof ModelElement) {
					ModelElement me = (ModelElement) el;
					if (me.type == ElementType.NORMAL) {
						makeArmorLayer(editor, b, me, addTo, ctLookup, rmt);
					}
				}
			});
			b.onRun(editor::updateGui);
			b.execute();
		}, null));
	}

	private static void makeArmorLayer(Editor editor, ActionBuilder ab, ModelElement me, ModelElement addTo, Map<ModelElement, ModelElement> ctLookup, RootModelType rmt) {
		ModelElement i = me;
		List<ModelElement> parts = new ArrayList<>();
		while (i.type == ElementType.NORMAL) {
			parts.add(i);
			i = i.parent;
		}
		i = addTo;
		for (int j = parts.size() - 1; j >= 0; j--) {
			ModelElement el = parts.get(j);

			final ModelElement fi = i;
			i = ctLookup.computeIfAbsent(el, __ -> {
				for (ModelElement c : fi.children) {
					if (c.copyTransform != null && c.copyTransform.from == el) {
						return c;
					}
				}

				ModelElement elem = new ModelElement(editor);
				ab.addToList(fi.children, elem);
				elem.size = new Vec3f(0);
				elem.name = editor.ui.i18nFormat("label.cpm.generatedArmorLayer.ct", el.name);
				elem.parent = fi;
				elem.copyTransform = new CopyTransformEffect(elem);
				elem.copyTransform.from = el;
				elem.copyTransform.setAll(true);
				return elem;
			});
		}

		if (i.size.epsilon(0.1f)) {
			ab.updateValueOp(i, i.size, new Vec3f(me.size), (a, b) -> a.size = b);
			ab.updateValueOp(i, i.offset, new Vec3f(me.offset), (a, b) -> a.offset = b);
			ab.updateValueOp(i, i.texture, true, (a, b) -> a.texture = b);
			ab.updateValueOp(i, i.name, editor.ui.i18nFormat("label.cpm.generatedArmorLayer", me.name), (a, b) -> a.name = b);
			ab.updateValueOp(i, i.mcScale, rmt.getDefaultSize(editor.skinType).getMCScale(), (a, b) -> a.mcScale = b);
		} else {
			ModelElement elem = new ModelElement(editor);
			ab.addToList(i.children, elem);
			elem.parent = i;
			elem.size = new Vec3f(me.size);
			elem.offset = new Vec3f(me.offset);
			elem.texture = true;
			elem.name = editor.ui.i18nFormat("label.cpm.generatedArmorLayer", me.name);
			elem.mcScale = rmt.getDefaultSize(editor.skinType).getMCScale();
		}
	}

	public static void afterDuplicate(ModelElement from, ModelElement to) {
		Map<ModelElement, ModelElement> dup = new HashMap<>();
		List<CopyTransformEffect> cts = new ArrayList<>();
		walk(from, to, dup, cts);
		for (CopyTransformEffect e : cts) {
			e.from = dup.getOrDefault(e.from, e.from);
		}
	}

	private static void walk(ModelElement from, ModelElement to, Map<ModelElement, ModelElement> dup, List<CopyTransformEffect> cts) {
		for (int i = 0; i < from.children.size(); i++) {
			ModelElement f = from.children.get(i);
			ModelElement t = to.children.get(i);
			dup.put(f, t);
			if (t.copyTransform != null)cts.add(t.copyTransform);
			walk(f, t, dup, cts);
		}
	}
}
