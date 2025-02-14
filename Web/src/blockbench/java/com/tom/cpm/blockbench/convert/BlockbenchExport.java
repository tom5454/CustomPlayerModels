package com.tom.cpm.blockbench.convert;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.Pair;
import com.tom.cpm.blockbench.convert.WarnEntry.MultiWarnEntry;
import com.tom.cpm.blockbench.format.AnimationWizard;
import com.tom.cpm.blockbench.format.CubeData;
import com.tom.cpm.blockbench.format.GroupData;
import com.tom.cpm.blockbench.format.ProjectData;
import com.tom.cpm.blockbench.format.ProjectGenerator;
import com.tom.cpm.blockbench.proxy.Animation;
import com.tom.cpm.blockbench.proxy.Animation.GeneralAnimator;
import com.tom.cpm.blockbench.proxy.BoneAnimator;
import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.proxy.Cube.CubeFace;
import com.tom.cpm.blockbench.proxy.Dialog;
import com.tom.cpm.blockbench.proxy.Dialog.DialogProperties;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.blockbench.proxy.Group;
import com.tom.cpm.blockbench.proxy.Group.GroupProperties;
import com.tom.cpm.blockbench.proxy.Project;
import com.tom.cpm.blockbench.proxy.Texture;
import com.tom.cpm.blockbench.proxy.Undo;
import com.tom.cpm.blockbench.proxy.Undo.UndoData;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.cpm.blockbench.util.JsonUtil;
import com.tom.cpm.blockbench.util.PopupDialogs;
import com.tom.cpm.blockbench.util.PopupDialogs.UserException;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.CopyTransformEffect;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimFrame.FrameData;
import com.tom.cpm.shared.editor.anim.AnimatedTex;
import com.tom.cpm.shared.editor.anim.AnimationEncodingData;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.elements.RootGroups;
import com.tom.cpm.shared.editor.project.JsonList;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.project.JsonMapImpl;
import com.tom.cpm.shared.editor.project.loaders.AnimationsLoaderV1;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.model.PartPosition;
import com.tom.cpm.shared.model.PartValues;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.DirectPartValues;
import com.tom.cpm.shared.model.render.DirectParts;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.model.render.PerFaceUV;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.web.client.java.Java;
import com.tom.cpm.web.client.java.JsBuilder;
import com.tom.cpm.web.client.util.I18n;
import com.tom.cpm.web.client.util.ImageIO;
import com.tom.ugwt.client.JsArrayE;

import elemental2.dom.DomGlobal;
import elemental2.dom.FileReader;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class BlockbenchExport {
	private final Editor editor;
	private JsArrayE<Group> all;
	private List<WarnEntry> warnings = new ArrayList<>();
	private Map<String, CPMElem> modelTree = new HashMap<>();
	private Map<TextureSheetType, UVMul> uvMul = new HashMap<>();
	private List<Runnable> postConvert = new ArrayList<>();
	private Map<String, ModelElement> uuidLookup = new HashMap<>();
	private Function<List<WarnEntry>, Promise<Void>> openWarn;

	public BlockbenchExport(Editor editor, Function<List<WarnEntry>, Promise<Void>> openWarn) {
		this.editor = editor;
		this.openWarn = openWarn;
	}

	private static void log(String msg) {
		DomGlobal.console.log(msg);
	}

	@SuppressWarnings("unchecked")
	public Promise<Void> doExport() {
		log("Exporting");
		all = Global.getAllGroups();

		List<Promise<Void>> loadedTextures = new ArrayList<>();
		UVMul skinUV = new UVMul();
		uvMul.put(TextureSheetType.SKIN, skinUV);
		if(Texture.all.length == 1) {
			Texture skin = Texture.all.getAt(0);
			loadedTextures.add(loadTexture(skin, TextureSheetType.SKIN));
			skinUV.texId = skin.uuid;
		} else {
			for (int i = 0;i<Texture.all.length;i++) {
				Texture tex = Texture.all.getAt(i);
				TextureSheetType tst = TextureSheetType.SKIN;
				boolean found = false;
				for(TextureSheetType s : TextureSheetType.VALUES) {
					if(tex.name.equalsIgnoreCase(s.name()) || tex.name.equalsIgnoreCase(s.name() + ".png")) {
						tst = s;
						found = true;
						break;
					}
				}
				if(!found) {
					warnings.add(new WarnEntry(I18n.format("bb-label.warn.unknownTexture", tex.name), () -> fixTexture(tex)));
					continue;
				}
				loadedTextures.add(loadTexture(tex, tst));
				if(tst != TextureSheetType.SKIN) {
					UVMul m = new UVMul();
					if(!tst.editable) {
						m.x = tst.getDefSize().x / (float) Project.texture_width;
						m.y = tst.getDefSize().y / (float) Project.texture_height;
					}
					uvMul.put(tst, m);
				}
				uvMul.get(tst).texId = tex.uuid;
			}
		}
		log("Exported textures");

		//Throw all loose cubes into the body
		ModelElement body = getPart(PlayerModelParts.BODY);
		Cube.all.forEach(cube -> {
			if(cube.parent == Group.ROOT) {
				Vec3f pos = cube.origin.toVecF();
				pos.x *= 1;
				pos.y *= 1;
				pos.y += 24;
				ModelElement me = new ModelElement(editor);
				me.pos = pos;
				me.texture = true;
				add(body, me);
				me.showInEditor = cube.visibility;
				me.mirror = cube.mirror_uv;
				me.rotation = cube.rotation.toVecF();
				me.rotation.x = -me.rotation.x;
				me.rotation.y = -me.rotation.y;
				me.size = cube.to.toVecF().sub(cube.from.toVecF());
				Vec3f or = cube.origin.toVecF();
				Vec3f fr = cube.from.toVecF();
				me.offset.x = or.x - me.size.x - fr.x;
				me.offset.y = or.y - me.size.y - fr.y;
				me.offset.z = fr.z - or.z;
				me.name = cube.name;
				convertUV(me, cube);
				fixDecimals(me);
			}
		});
		log("Exported loose cubes");

		all.slice().forEach(group -> {
			List<Group> subgroups = new ArrayList<>();
			int[] group_i = new int[] {all.indexOf(group)};
			group.children.forEachReverse(in -> {
				if(!(in instanceof Cube) || !((Cube)in).export)return;
				Cube cube = (Cube) in;
				if(!cube.rotation.allEqual(0)) {
					Group sub = subgroups.stream().filter(s -> {
						if(!s.rotation.equals(cube.rotation))return false;
						if(s.rotation.hasValues(1)) {
							return s.origin.equals(cube.origin);
						} else {
							if(s.rotation.x == 0 && s.origin.x != cube.origin.x)return false;
							if(s.rotation.y == 0 && s.origin.y != cube.origin.y)return false;
							if(s.rotation.z == 0 && s.origin.z != cube.origin.z)return false;
							return true;
						}
					}).findFirst().orElse(null);
					if(sub == null) {
						GroupProperties g = new GroupProperties();
						g.rotation = cube.rotation;
						g.origin = cube.origin;
						g.name = cube.name + "_r1";
						sub = new Group(g);
						sub.parent = group;
						sub.is_rotation_subgroup = true;
						sub.createUniqueName(all);
						subgroups.add(sub);
						group_i[0]++;
						all.splice(group_i[0], 0, sub);
					}
					sub.children.push(cube);
				}
			});
		});

		log("Generated rotation subgroups");

		all.forEach(group -> {
			if ((!(group instanceof Group) && !group.is_catch_bone) || !group.export) return;
			if (group.parent != Group.ROOT) return;
			String groupName = group.name;
			VanillaModelPart part;
			ModelElement me;
			if(groupName.endsWith("_dup")) {
				groupName = groupName.substring(0, groupName.length() - 4);
				part = part(groupName);
				if(part == null) {
					rootWarning(group);
					return;
				}
				me = new ModelElement(editor, ElementType.ROOT_PART, part);
				me.duplicated = true;
				me.storeID = Math.abs(new Random().nextLong());
				editor.elements.add(me);
			} else {
				part = part(groupName);
				if(part == null) {
					rootWarning(group);
					return;
				}
				me = getPart(part);
			}
			me.hidden = true;
			me.disableVanillaAnim = group.disableVanillaAnim;
			uuidLookup.put(group.uuid, me);
			Vec3f o = group.origin.toVecF();
			o.x *= -1;
			o.y *= -1;
			o.y += 24;
			me.rotation.x = -group.rotation.x;
			me.rotation.y = -group.rotation.y;
			me.rotation.z = group.rotation.z;
			PartValues pv = DirectParts.getPartOverrides(part, editor.skinType);
			o = o.sub(pv.getPos());
			VanillaModelPart p = part.getCopyFrom();
			if(p != null) {
				ModelElement e = getPart(p);
				me.rotation = me.rotation.sub(e.rotation);
				o = o.sub(e.pos);
			} else if(pv instanceof DirectPartValues) {
				me.rotation = me.rotation.sub(((DirectPartValues)pv).getRotation());
			}
			me.pos = o;
			fixDecimals(me);
			modelTree.put(group.uuid, new CPMElem(me));
			group.children.forEach(in -> {
				if(!(in instanceof Cube))return;
				Cube cube = (Cube) in;
				if(!cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup))return;
				ModelElement e = new ModelElement(editor);
				add(me, e);
				e.texture = true;
				convert(e, group, cube);
				fixDecimals(e);
			});
		});

		log("Exported roots");

		all.forEach(group -> {
			if ((!(group instanceof Group) && !group.is_catch_bone) || !group.export) return;
			if (group.parent == Group.ROOT) return;
			Vec3f rot = new Vec3f();
			rot.x = -group.rotation.x;
			rot.y = -group.rotation.y;
			rot.z = group.rotation.z;
			Vec3f o = group.origin.toVecF().sub(group.parent.origin.toVecF());
			o.x *= -1;
			o.y *= -1;
			final Vec3f fo = o;
			List<CPMElem> children = new ArrayList<>();
			CPMElem ch = modelTree.get(group.parent.uuid);
			group.children.forEach(in -> {
				if(!(in instanceof Cube))return;
				Cube cube = (Cube) in;
				if(!cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup))return;
				CPMElem e = new CPMElem(group, cube, rot, fo);
				children.add(e);
			});
			if(children.isEmpty()) {
				CPMElem e = new CPMElem(group, null, rot, fo);
				children.add(e);
			} else if(children.size() > 1) {
				CPMElem e = new CPMElem(group, null, rot, fo);
				children.forEach(CPMElem::resetTransform);
				e.children.addAll(children);
				children.clear();
				children.add(e);
			}
			if(ch == null) {
				MultiWarnEntry.addOrIncEntry(warnings, I18n.format("bb-label.warn.cubeSkipped")).setPriority(-1);
				return;
			}
			ch.children.add(children.get(0));
			modelTree.put(group.uuid, children.get(0));
		});

		log("Exported model parts");

		modelTree.values().stream().filter(e -> e.part != null).forEach(CPMElem::finishConvert);
		postConvert.forEach(Runnable::run);

		log("Constructed model parts hierarchy");

		Promise<?> pr = Promise.all(loadedTextures.stream().toArray(Promise[]::new));
		return pr.then(__ -> {
			log("Textures finished");
			finishExport();
			if(!warnings.isEmpty())
				return openWarn.apply(warnings);
			else
				return Promise.resolve((Void) null);
		});
	}

	private void fixDecimals(ModelElement me) {
		me.size = fixDecimals(me.size, 1);
		me.pos = fixDecimals(me.pos, 2);
		me.rotation = fixDecimals(me.rotation, 1);
		me.offset = fixDecimals(me.offset, 2);
	}

	private Vec3f fixDecimals(Vec3f size, int dp) {
		if (size == null)return null;
		return new Vec3f(
				new BigDecimal(size.x).setScale(dp, RoundingMode.HALF_UP).floatValue(),
				new BigDecimal(size.y).setScale(dp, RoundingMode.HALF_UP).floatValue(),
				new BigDecimal(size.z).setScale(dp, RoundingMode.HALF_UP).floatValue()
				);
	}

	private void rootWarning(Group group) {
		warnings.add(new WarnEntry(I18n.format("bb-label.warn.unknownRoot", group.name), () -> {
			return new Promise<>((res, rej) -> {
				DialogProperties pr = new DialogProperties();
				pr.id = "cpm_fix_roots";
				pr.title = I18n.get("bb-label.selectRoot");
				pr.singleButton = false;
				Dialog.FormSelectElement selPart = Dialog.FormSelectElement.make(I18n.get("bb-label.selectRoot"));
				JsBuilder<String> b = new JsBuilder<>();
				for(PlayerModelParts p : PlayerModelParts.VALUES) {
					if(p == PlayerModelParts.CUSTOM_PART)continue;
					b.put(p.getName(), I18n.get("label.cpm.elem." + p.getName()));
				}
				for(RootModelType p : RootModelType.VALUES) {
					b.put(p.getName(), I18n.get("label.cpm.elem." + p.getName()));
				}
				selPart.value = PlayerModelParts.VALUES[0].getName();
				selPart.options = b.build();
				Dialog.FormCheckboxElement renameBox = Dialog.FormCheckboxElement.make(I18n.get("bb-label.export.renameGroup"), true, I18n.formatNl("bb-tooltip.export.renameGroup"));
				pr.form = new JsBuilder<>().
						put("root", selPart).
						put("dup", Dialog.FormCheckboxElement.make(I18n.get("bb-label.export.markDuplicateRoot"))).
						put("rename", renameBox).
						build();
				Dialog.link(pr.form);
				if(group.children.asList().stream().allMatch(n -> n instanceof Group)) {
					pr.buttons = new String[] {"action.resolve_group", "dialog.confirm", "dialog.cancel"};
					pr.confirmIndex = 1;
					pr.onButton = id -> {
						if(id == 0) {
							group.resolve();
							res.onInvoke(true);
						}
						return true;
					};
				}
				pr.onFormChange = d -> {
					JsPropertyMap<Object> dr = Js.uncheckedCast(d);
					String root = Js.cast(dr.get("root"));
					boolean dupRoot = Js.isTruthy(dr.get("dup"));
					boolean en = dupRoot || Arrays.stream(Group.all).noneMatch(gr -> gr.parent == Group.ROOT && gr.name.equals(root));
					renameBox.getBar().find("input#rename").disabled(!en);
				};
				pr.onOpen = () -> {
					boolean en = Arrays.stream(Group.all).noneMatch(gr -> gr.parent == Group.ROOT && gr.name.equals("head"));
					renameBox.getBar().find("input#rename").disabled(!en);
				};
				pr.onConfirm = d -> {
					JsPropertyMap<Object> dr = Js.uncheckedCast(d);
					String root = Js.cast(dr.get("root"));
					boolean dupRoot = Js.isTruthy(dr.get("dup"));
					boolean rename = Js.isTruthy(dr.get("rename"));
					UndoData dt = new UndoData();
					Undo.initEdit(dt);
					Group g;
					if(dupRoot) {
						if(rename) {
							group.name = root + "_dup";
							dt.group = group;
							Undo.finishEdit(I18n.format("bb-label.autoFixApplied", I18n.get("bb-label.autoFix.regroup")), dt);
							res.onInvoke(true);
							return true;
						}
						g = makeRoot(root);
						g.name = root + "_dup";
					} else if(rename && Arrays.stream(Group.all).noneMatch(gr -> gr.parent == Group.ROOT && gr.name.equals(root))) {
						group.name = root;
						dt.group = group;
						Undo.finishEdit(I18n.format("bb-label.autoFixApplied", I18n.get("bb-label.autoFix.regroup")), dt);
						res.onInvoke(true);
						return true;
					} else
						g = Arrays.stream(Group.all).filter(gr -> gr.parent == Group.ROOT && gr.name.equals(root)).findFirst().orElseGet(() -> makeRoot(root));
					group.addTo(g);

					dt.group = g;
					Undo.finishEdit(I18n.format("bb-label.autoFixApplied", I18n.get("bb-label.autoFix.regroup")), dt);
					ProjectGenerator.updateAll();

					res.onInvoke(true);
					return true;
				};
				pr.onCancel = () -> {
					res.onInvoke(false);
					return true;
				};
				new Dialog(pr).show();
			});
		}));
	}

	private Group makeRoot(String root) {
		VanillaModelPart part = part(root);
		PartValues pv = DirectParts.getPartOverrides(part, editor.skinType);
		Vec3f rot = new Vec3f();
		if(pv instanceof DirectPartValues) {
			rot = rot.add(((DirectPartValues)pv).getRotation());
		}
		Vec3f pos = pv.getPos();
		GroupProperties gp = new GroupProperties();
		gp.name = root;
		gp.origin = JsVec3.make(0, 24, 0);
		gp.rotation = JsVec3.make(-rot.x, -rot.y, rot.z);
		Group gr = new Group(gp).init();
		gp = new GroupProperties();
		gp.origin = JsVec3.make(-pos.x, 24 - pos.y, pos.z);
		gr.extend(gp);
		return gr;
	}

	private Promise<Boolean> fixTexture(Texture tex) {
		return new Promise<>((res, rej) -> {
			JsBuilder<String> b = new JsBuilder<>();
			for(TextureSheetType tst : TextureSheetType.VALUES) {
				if(tst == TextureSheetType.LIST_ICON)continue;
				if(Texture.all.asList().stream().anyMatch(t -> t.name.equals(tst.name().toLowerCase())))continue;
				b.put(tst.name().toLowerCase(), I18n.get("label.cpm.texture." + tst.name().toLowerCase()));
			}
			if(b.isEmpty()) {
				PopupDialogs.displayMessage(I18n.formatBr("bb-label.export.mergeTextureSheets"));
				rej.onInvoke("");
				return;
			}

			DialogProperties pr = new DialogProperties();
			pr.id = "cpm_fix_texture";
			pr.title = I18n.get("bb-label.selectTexture.title");
			pr.singleButton = false;
			Dialog.FormSelectElement selPart = Dialog.FormSelectElement.make(I18n.format("bb-label.selectTexture", tex.name));
			selPart.value = b.first();
			selPart.options = b.build();
			selPart.description = I18n.formatNl("bb-label.export.mergeTextureSheets");
			pr.form = new JsBuilder<>().put("root", selPart).build();
			pr.onConfirm = d -> {
				JsPropertyMap<String> dr = Js.uncheckedCast(d);
				String root = dr.get("root");
				UndoData dt = new UndoData();
				dt.textures = new Texture[] {tex};
				Undo.initEdit(dt);
				tex.name = root;
				Undo.finishEdit(I18n.format("bb-label.autoFixApplied", I18n.get("bb-label.autoFix.renameTexture")), dt);
				ProjectGenerator.updateAll();

				res.onInvoke(true);
				return true;
			};
			pr.onCancel = () -> {
				res.onInvoke(false);
				return true;
			};
			new Dialog(pr).show();
		});
	}

	private Promise<Boolean> showDecimalFixed(Set<String> decFixApplied) {
		return new Promise<>((res, rej) -> {
			DialogProperties pr = new DialogProperties();
			pr.id = "cpm_list_decimals";
			pr.title = I18n.get("bb-label.warn.uvDecimalFixer.list");
			pr.singleButton = true;
			pr.lines = new String[] {
					I18n.formatBr("bb-label.warn.uvDecimalFixer.info"),
					decFixApplied.stream().collect(Collectors.joining("<br>", "<div style='padding: 8px;'>", "</div>"))
			};
			pr.onCancel = () -> {
				res.onInvoke(false);
				return true;
			};
			new Dialog(pr).show();
		});
	}

	private void finishExport() {
		Set<String> decFixApplied = new HashSet<>();
		uvMul.entrySet().forEach(e -> {
			UVMul m = e.getValue();
			TextureSheetType tst = e.getKey();
			if(tst.editable) {
				ETextures tex = editor.textures.get(tst);
				if (tex.provider.size.x * 16 < 16384 && tex.provider.size.y * 16 < 16384) {
					List<DecimalFixOp> reqFix = m.elems.stream().filter(DecimalFixOp::needsFix).collect(Collectors.toList());
					if (!reqFix.isEmpty()) {
						for (DecimalFixOp op : reqFix) {
							decFixApplied.add(op.getDisplayName());
						}
						m.baseMul *= 16;
						tex.provider.size.x *= 16;
						tex.provider.size.y *= 16;
					}
				}
			}
			m.elems.forEach(DecimalFixOp::apply);
		});
		log("Applied UV decimal fixer");
		if (!decFixApplied.isEmpty()) {
			warnings.add(new WarnEntry(I18n.format("bb-label.warn.uvDecimalFixer.applied"), () -> showDecimalFixed(decFixApplied)).setInfo().setTooltip(I18n.format("bb-tooltip.warn.uvDecimalFixerApplied", I18n.get("bb-button.autoFix"))));
		}

		ProjectData pd = Project.getData();
		if(!pd.animations.isEmpty())new AnimEnc().load(new JsonMapImpl(pd.animations));
		editor.scalingElem.scaling.putAll(pd.scalingOpt);
		if(pd.renderPos != null)editor.scalingElem.pos = pd.renderPos;
		if(pd.renderRot != null)editor.scalingElem.rotation = pd.renderRot;
		if(pd.renderScl != null)editor.scalingElem.scale = pd.renderScl;

		if(pd.leftHandPos != null)editor.leftHandPos = pd.leftHandPos;
		if(pd.rightHandPos != null)editor.rightHandPos = pd.rightHandPos;
		editor.modelId = pd.modelId;
		editor.removeArmorOffset = pd.removeArmorOffset;//TODO test

		if (!pd.animTex.isEmpty()) {
			for(TextureSheetType tx : TextureSheetType.VALUES) {
				ETextures eTex = editor.textures.get(tx);
				if(eTex != null && eTex.isEditable() && pd.animTex.containsKey(tx)) {
					JsonMap map = new JsonMapImpl(pd.animTex.get(tx));
					JsonList list = map.getList("anim");
					list.forEachMap(elem -> eTex.animatedTexs.add(new AnimatedTex(editor, tx, elem)));
				}
			}
		}

		editor.description = pd.description;
		editor.hideHeadIfSkull = Project.hideHeadIfSkull;
		editor.removeBedOffset = Project.removeBedOffset;
		editor.enableInvisGlow = Project.invisGlow;

		Project.animations.forEach(this::convertAnimation);

		resetEditorState(editor);
	}

	private static native void resetEditorState(Editor e)/*-{
		e.@com.tom.cpm.shared.editor.Editor::undoQueue.@java.util.Stack::clear()();
		e.@com.tom.cpm.shared.editor.Editor::redoQueue.@java.util.Stack::clear()();
	}-*/;

	private void convertAnimation(Animation anim) {
		if(Js.typeof(anim.type) != "string") {
			warnings.add(new WarnEntry(I18n.formatBr("bb-label.warn.unknownAnimation", anim.name), () -> new Promise<>((res, rej) -> {
				AnimationWizard.open(anim, res::onInvoke);
			})));
			return;
		}

		boolean[] error = new boolean[] {false, false};
		List<BoneAnimator> anims = new ArrayList<>();
		anim.animators.forEach(k -> {
			GeneralAnimator ga = anim.animators.get(k);
			if(ga instanceof BoneAnimator) {
				BoneAnimator ba = (BoneAnimator) ga;
				if(!ba.hasTransform())return;
				anims.add(ba);
			}
		});

		Pair<AnimationType, VanillaPose> typeA = Animation.parseType(anim.type);
		Pair<AnimationType, VanillaPose> type = typeA;

		String setupName;
		if(typeA.getKey() == AnimationType.POSE) {
			setupName = "p:" + typeA.getKey().name().toLowerCase(Locale.ROOT);
		} else if(typeA.getKey() == AnimationType.CUSTOM_POSE) {
			setupName = "c:" + anim.name;
		} else if(typeA.getKey() == AnimationType.GESTURE || typeA.getKey() == AnimationType.LAYER) {
			setupName = "g:" + anim.name;
		} else
			setupName = null;

		boolean loop = false;
		String name = anim.name;

		if(type.getKey().isStaged()) {
			if(!anim.loop.equals("once")) {
				warnings.add(new WarnEntry(I18n.formatBr("bb-label.warn.stagedAnimLooping", anim.name), () -> new Promise<>((res, rej) -> {
					Undo.initEdit(UndoData.make(anim));
					anim.loop = "once";
					Undo.finishEdit("Edit animation properties");
					res.onInvoke(true);
				})));
				return;
			}
			String[] sp = anim.name.split(":", 2);
			boolean err = false;
			if(sp.length < 2)err = true;
			else if(sp[0].equals("p")) {
				VanillaPose pose = null;
				for (VanillaPose p : VanillaPose.VALUES) {
					if(sp[1].equalsIgnoreCase(p.name())) {
						pose = p;
						break;
					}
				}
				if(pose == null)err = true;
				else {
					String nm = sp[1] + " Base";
					String fn = AnimationsLoaderV1.getFileName(pose, nm);
					EditorAnim ea = new EditorAnim(editor, fn, AnimationType.POSE, true);
					ea.intType = InterpolatorType.LINEAR_SINGLE;
					ea.add = true;
					ea.displayName = nm;
					ea.pose = pose;
					editor.animations.add(ea);
				}
			} else if(!sp[0].equals("c") && !sp[0].equals("g")) {
				err = true;
			}
			if(err) {
				warnings.add(new WarnEntry(I18n.formatBr("bb-label.warn.unknownStagedAnimation", anim.name), () -> fixStaged(anim)));
				return;
			}
		} else {
			if (!anim.loop.equals("loop")) {
				if (type.getKey() == AnimationType.VALUE_LAYER || (type.getKey() == AnimationType.POSE && type.getValue().hasStateGetter())) {
					warnings.add(new WarnEntry(I18n.formatBr("bb-label.warn.valueAnimNotLooping", anim.name), () -> new Promise<>((res, rej) -> {
						Undo.initEdit(UndoData.make(anim));
						anim.loop = "loop";
						Undo.finishEdit("Edit animation properties");
						res.onInvoke(true);
					})));
					return;
				}
			}
			if(anim.loop.equals("loop")) {
				if(type.getKey().canLoop())loop = true;
			} else if(anim.loop.equals("once")) {
				if(!type.getKey().canLoop()) {
					type = Pair.of(AnimationType.SETUP, null);
					name = setupName;
				}
			} else if(anim.loop.equals("hold")) {
				IPose pose;
				if(type.getKey() == AnimationType.CUSTOM_POSE)
					pose = new CustomPose(anim.name, 0);
				else if(type.getKey() == AnimationType.POSE)
					pose = type.getValue();
				else
					pose = null;

				String fn = AnimationsLoaderV1.getFileName(pose, name);
				EditorAnim ea = new EditorAnim(editor, fn, type.getKey(), false);
				ea.intType = InterpolatorType.LINEAR_SINGLE;
				ea.add = anim.additive;
				ea.loop = true;
				ea.displayName = name;
				ea.pose = pose;
				generateFrame(anims, ea, anim.length, error);
				editor.animations.add(ea);

				type = Pair.of(AnimationType.SETUP, null);
				name = setupName;
			} else return;//Error
		}

		IPose pose;
		if(type.getKey() == AnimationType.CUSTOM_POSE)
			pose = new CustomPose(anim.name, 0);
		else if(type.getKey() == AnimationType.POSE)
			pose = type.getValue();
		else
			pose = null;

		String fn = AnimationsLoaderV1.getFileName(pose, name);
		EditorAnim ea = new EditorAnim(editor, fn, type.getKey(), false);
		ea.intType = InterpolatorType.LINEAR_SINGLE;
		ea.add = anim.additive;
		ea.command = anim.commandCtrl;
		ea.layerControlled = anim.layerCtrl;
		ea.priority = anim.getPriority();
		ea.isProperty = anim.isProperty;
		ea.order = anim.getOrder();
		ea.group = anim.group;
		ea.layerDefault = anim.getLayerDefault();
		ea.loop = loop;
		ea.displayName = name;
		ea.pose = pose;

		if(!anims.isEmpty()) {
			float time = anim.length;
			ea.duration = (int) (time * 1000);

			int frameCount = MathHelper.ceil(time * anim.snapping);
			for (int i = 0; i < frameCount; i++) {
				float ftime = i / (float) anim.snapping;
				generateFrame(anims, ea, ftime, error);
			}
		}

		editor.animations.add(ea);
		if(error[0])
			MultiWarnEntry.addOrIncEntry(warnings, I18n.format("bb-label.warn.rootScaling")).setPriority(-1);
		if(error[1])
			MultiWarnEntry.addOrIncEntry(warnings, I18n.format("bb-label.warn.globalRotation")).setPriority(-1);
	}

	private void generateFrame(List<BoneAnimator> anims, EditorAnim ea, float ftime, boolean[] error) {
		ea.addFrame();
		AnimFrame f = ea.getSelectedFrame();

		anims.forEach(a -> {
			ModelElement me = uuidLookup.get(a.uuid);
			if(me != null) {
				Vec3f addPos = me.pos;
				Vec3f addRot = me.rotation;
				if(!ea.add && me.type == ElementType.ROOT_PART) {
					VanillaModelPart part = (VanillaModelPart) me.typeData;
					PartValues pv = DirectParts.getPartOverrides(part, editor.skinType);
					if(pv instanceof DirectPartValues) {
						addRot = addRot.add(((DirectPartValues)pv).getRotation());
					}
					addPos = addPos.add(pv.getPos());
				}

				JsVec3 pos = Animation.interpolate(a.position, ftime);
				JsVec3 rot = Animation.interpolate(a.rotation, ftime);
				JsVec3 scl = Animation.interpolate(a.scale, ftime);
				JsVec3 color = me.recolor ? Animation.interpolate(a.color, ftime) : null;
				boolean vis = Animation.interpolateVisible(a, ftime, !me.hidden);

				FrameData dt = f.getComponents().get(me);
				if(dt == null) {
					dt = f.new FrameData(me);
					f.getComponents().put(me, dt);
				}

				if(pos != null) {
					Vec3f v = pos.toVecF();
					v.y *= -1;
					if(!ea.add)v = v.add(addPos);
					dt.setPos(v);
				}
				if(rot != null) {
					Vec3f v = rot.toVecF();
					if(!ea.add)v = v.add(addRot);
					ActionBuilder.limitVec(v, 0, 360, true);
					dt.setRot(v);
					if(a.rotation_global)error[1] = true;
				}
				if(scl != null) {
					if(me.type == ElementType.ROOT_PART)
						error[0] = true;
					else
						dt.setScale(scl.toVecF());
				}
				dt.setShow(vis);
				if(color != null) {
					dt.setColor(color.toVecF());
				}
			}
		});
	}

	private Promise<Boolean> fixStaged(Animation a) {
		return new Promise<>((res, rej) -> {
			JsBuilder<String> b = new JsBuilder<>();
			for (VanillaPose p : VanillaPose.VALUES) {
				if(p == VanillaPose.CUSTOM)continue;
				String id = p.name().toLowerCase(Locale.ROOT);
				b.put("p:" + id, I18n.get("label.cpm.pose." + id));
			}
			Project.animations.forEach(an -> {
				Pair<AnimationType, VanillaPose> type = Animation.parseType(an.type);
				if(type.getKey() == AnimationType.CUSTOM_POSE) {
					b.put("c:" + an.name, an.name);
				} else if(type.getKey() == AnimationType.GESTURE || type.getKey() == AnimationType.LAYER) {
					b.put("g:" + an.name, an.name);
				}
			});

			DialogProperties pr = new DialogProperties();
			pr.id = "cpm_fix_staged";
			pr.title = I18n.get("bb-label.fixStagedAnim.title");
			pr.singleButton = false;
			Dialog.FormSelectElement selPart = Dialog.FormSelectElement.make(I18n.format("bb-label.fixStagedAnim.selectBase"));
			selPart.value = b.first();
			selPart.options = b.build();
			pr.form = new JsBuilder<>().put("root", selPart).build();
			pr.onConfirm = d -> {
				JsPropertyMap<String> dr = Js.uncheckedCast(d);
				String root = dr.get("root");

				Undo.initEdit(UndoData.make(a));
				a.name = root;
				Undo.finishEdit(I18n.format("bb-label.autoFixApplied", I18n.get("bb-label.autoFix.stagedAnim")));

				res.onInvoke(true);
				return true;
			};
			pr.onCancel = () -> {
				res.onInvoke(false);
				return true;
			};
			new Dialog(pr).show();
		});
	}

	protected static PartPosition loadPartPos(JsonMap fpHand, String name) {
		PartPosition p = new PartPosition();
		JsonMap map = fpHand.getMap(name);
		if(map != null) {
			Vec3f pos = new Vec3f(map.getMap("position"), new Vec3f());
			Vec3f rotation = new Vec3f(map.getMap("rotation"), new Vec3f());
			Vec3f scale = new Vec3f(map.getMap("scale"), new Vec3f());
			p.setRenderScale(pos, new Rotation(rotation, true), scale);
		}
		return p;
	}

	private class AnimEnc extends AnimationsLoaderV1 {

		public void load(JsonMap data) {
			JsonMap an = data.getMap("anims");
			an.asMap().keySet().forEach(a -> loadAnimation(editor, a, an.getMap(a)));
			if(data.containsKey("free") && data.containsKey("def")) {
				editor.animEnc = new AnimationEncodingData();
				data.getList("free").forEach(v -> editor.animEnc.freeLayers.add(PlayerSkinLayer.getLayer((String) v)));
				data.getMap("def").forEach((k, v) -> editor.animEnc.defaultLayerValue.put(PlayerSkinLayer.getLayer(k), (Boolean) v));
			}
		}

		@Override
		protected void findPartRef(AnimFrame frm, JsonMap map, Consumer<ModelElement> me) {
			ModelElement el = uuidLookup.get(map.getString("uuid"));
			if(el != null)me.accept(el);
		}
	}

	private Promise<Void> loadTexture(Texture t, TextureSheetType tst) {
		ETextures e = editor.textures.get(tst);
		if(e == null) {
			e = new ETextures(editor, tst);
			editor.textures.put(tst, e);
			Image def = new Image(tst.getDefSize().x, tst.getDefSize().y);
			e.provider.size = new Vec2i(tst.getDefSize());
			try(InputStream is = ModelDefinitionLoader.class.getResourceAsStream("/assets/cpm/textures/template/" + tst.name().toLowerCase() + ".png")) {
				def = Image.loadFrom(is);
			} catch (IOException ex) {
			}
			e.setDefaultImg(def);
			e.setImage(new Image(def));
		}
		if(tst.editable) {
			final ETextures fe = e;
			return DomGlobal.fetch(t.source).then(b -> b.blob()).
					then(blob -> new Promise<>((ResolveCallbackFn<String> res, RejectCallbackFn rej) -> {
						FileReader reader = new FileReader();
						reader.onloadend = __ -> {
							res.onInvoke(reader.result.asString());
							return null;
						};
						reader.onerror = __ -> {
							rej.onInvoke("File read error");
							return null;
						};
						reader.readAsDataURL(blob);
					})).then(dataUrl -> {
						String b64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
						return ImageIO.loadImage(b64, true, false);
					}).then(img -> {
						fe.provider.size = new Vec2i(t.uv_width, t.uv_height);
						fe.setImage(img);
						fe.setEdited(true);
						return Promise.resolve((Void) null);
					}).catch_(er -> {
						Throwable error = Java.convertRejectObject(er);
						return Promise.reject(new UserException(I18n.formatBr("bb-label.error.failedToLoadTexture", t.name, error.toString())));
					});
		}
		return Promise.resolve((Void) null);
	}

	private VanillaModelPart part(String name) {
		for(PlayerModelParts pmp : PlayerModelParts.VALUES) {
			if(pmp.getName().equalsIgnoreCase(name))return pmp;
		}
		for(RootModelType pmp : RootModelType.VALUES) {
			if(pmp.getName().equalsIgnoreCase(name))return pmp;
		}
		return null;
	}

	private void convert(ModelElement elem, Group group, Cube cube) {
		elem.name = cube.name;
		elem.mcScale = cube.inflate;
		elem.size = cube.to.toVecF().sub(cube.from.toVecF());
		elem.offset.x = group.origin.x - cube.to.x;
		elem.offset.y = -cube.from.y - elem.size.y + group.origin.y;
		elem.offset.z = cube.from.z - group.origin.z;
		elem.glow = cube.glow;
		int rc = cube.getRecolor();
		elem.recolor = rc != -1;
		if(rc != -1)elem.rgb = rc;
		elem.extrude = cube.extrude;
		convertUV(elem, cube);
		CubeData cd = cube.getData();
		if(cd.getExtrude() != null) {
			elem.extrude = true;
			CubeData.Extrude ext = cd.getExtrude();
			elem.u = ext.getU();
			elem.v = ext.getV();
			elem.textureSize = ext.getTs();
			elem.faceUV = null;
		}
	}

	private void convertGroup(ModelElement elem, Group group) {
		uuidLookup.put(group.uuid, elem);
		elem.hidden = group.hidden;
		if(group.copyTransform != null && !group.copyTransform.isEmpty()) {
			postConvert.add(() -> {
				JsonMap s = JsonUtil.fromJson(group.copyTransform);
				ModelElement me = uuidLookup.get(s.get("uuid"));
				if(me != null) {
					CopyTransformEffect ct = new CopyTransformEffect(elem);
					ct.load(s);
					ct.from = me;
					elem.copyTransform = ct;
				}
			});
		}
		GroupData gd = group.getData();
		if(gd.getItemRenderer() != null) {
			String name = gd.getItemRenderer();
			for(ItemSlot slot : ItemSlot.VALUES) {
				if(name.equalsIgnoreCase(slot.name())) {
					elem.itemRenderer = new ItemRenderer(slot, 0);
					elem.size = new Vec3f();
					elem.faceUV = null;
					break;
				}
			}
		}
	}

	private static boolean isDecimal(float v) {
		return Math.abs(v - Math.floor(v)) > 0.01f;
	}

	private void convertUV(ModelElement elem, Cube cube) {
		VanillaModelPart part = (VanillaModelPart) elem.getRoot().typeData;
		TextureSheetType tst = TextureSheetType.SKIN;
		if(part instanceof RootModelType) {
			tst = RootGroups.getGroup((RootModelType) part).getTexSheet((RootModelType) part);
		}
		UVMul m = uvMul.get(tst);
		if(m == null) {
			warnings.add(new WarnEntry(I18n.format("bb-label.warn.invalidTexture", cube.name)));
			return;
		}

		if(cube.box_uv && !isDecimal(elem.size.x) && !isDecimal(elem.size.y) && !isDecimal(elem.size.z)) {
			m.elems.add(new DecimalFixOp(cube, null, cube.uv_offset.x, 0, m, a -> elem.u = a));
			m.elems.add(new DecimalFixOp(cube, null, cube.uv_offset.y, 1, m, a -> elem.v = a));
			m.elems.add(new DecimalFixOp(cube, null, 1, 4, m, a -> elem.textureSize = a));
			elem.mirror = cube.mirror_uv;
		} else {
			elem.faceUV = new PerFaceUV();
			for(Direction d : Direction.VALUES) {
				CubeFace cf = cube.faces.getFace(d);
				if(cf.uv.isEmpty()) {
					elem.faceUV.faces.remove(d);
					continue;
				}
				Face f = elem.faceUV.faces.computeIfAbsent(d, __ -> new Face());
				m.elems.add(new DecimalFixOp(cube, d, cf.uv.sx, 2, m, a -> f.sx = a));
				m.elems.add(new DecimalFixOp(cube, d, cf.uv.sy, 3, m, a -> f.sy = a));
				m.elems.add(new DecimalFixOp(cube, d, cf.uv.ex, 2, m, a -> f.ex = a));
				m.elems.add(new DecimalFixOp(cube, d, cf.uv.ey, 3, m, a -> f.ey = a));
				if(d == Direction.UP || d == Direction.DOWN)
					f.rotation = intToRot((cf.rotation + 180) % 360);
				else
					f.rotation = intToRot(cf.rotation);
				f.autoUV = Js.isTruthy(cube.autouv) ? true : false;
			}
		}
		if (cube.isColorCube() && cube.getRecolor() != -1) {
			elem.texture = false;
		} else {
			boolean badTexture = false;
			for(Direction d : Direction.VALUES) {
				CubeFace cf = cube.faces.getFace(d);
				if(!cf.uv.isEmpty() && (m.texId == null || !m.texId.equals(cf.texture))) {
					badTexture = true;
					break;
				}
			}
			if(badTexture)warnings.add(new WarnEntry(I18n.format("bb-label.warn.invalidTexture", cube.name)));
		}
	}

	private static class DecimalFixOp {
		private float val;
		private int mode;
		private UVMul mul;
		private IntConsumer set;
		private Cube cube;
		private Direction face;

		public DecimalFixOp(Cube cube, Direction face, float val, int mode, UVMul mul, IntConsumer set) {
			this.cube = cube;
			this.val = val;
			this.mode = mode;
			this.mul = mul;
			this.set = set;
			this.face = face;
		}

		public String getDisplayName() {
			if (face != null)return I18n.format("bb-label.warn.uvDecimalFixer.face", cube.name, I18n.get("label.cpm.dir." + face.name().toLowerCase(Locale.ROOT)));
			else return cube.name;
		}

		public boolean needsFix() {
			if((mode & 4) != 0)return false;
			float m = val * mul.baseMul * ((mode & 1) != 0 ? mul.y : mul.x);
			return (m - Math.round(m)) > 0.05f;
		}

		public void apply() {
			if((mode & 4) != 0) {
				set.accept(mul.baseMul);
			} else if((mode & 2) != 0) {
				set.accept(Math.round(val * mul.baseMul * ((mode & 1) != 0 ? mul.y : mul.x)));
			} else
				set.accept(Math.round(val * ((mode & 1) != 0 ? mul.y : mul.x)));
		}
	}

	private static Rot intToRot(int r) {
		switch (r) {
		case 0: return Rot.ROT_0;
		case 180: return Rot.ROT_180;
		case 270: return Rot.ROT_270;
		case 90: return Rot.ROT_90;
		default: throw new RuntimeException();
		}
	}

	private ModelElement getPart(VanillaModelPart pmp) {
		for(ModelElement e : editor.elements) {
			if(e.typeData == pmp)return e;
		}
		ModelElement e = new ModelElement(editor, ElementType.ROOT_PART, pmp);
		editor.elements.add(e);
		if(pmp instanceof RootModelType) {
			RootGroups rg = RootGroups.getGroup((RootModelType) pmp);
			if(rg != null) {
				//TODO
			}
		}
		return e;
	}

	private void add(ModelElement parent, ModelElement me) {
		parent.children.add(me);
		me.parent = parent;
	}

	private static class UVMul {
		private int baseMul = 1;
		private float x = 1, y = 1;
		private Set<DecimalFixOp> elems = new HashSet<>();
		private String texId;
	}

	private class CPMElem {
		private Group group;
		private Cube cube;
		private List<CPMElem> children = new ArrayList<>();
		private ModelElement part;
		private Vec3f rotation;
		private Vec3f pos;
		private boolean reGrouped;

		public CPMElem(Group group, Cube cube, Vec3f rotation, Vec3f pos) {
			this.group = group;
			this.cube = cube;
			this.rotation = rotation;
			this.pos = pos;
		}

		public CPMElem(ModelElement part) {
			this.part = part;
		}

		public void finishConvert() {
			children.forEach(c -> {
				c.makePart(part);
				c.finishConvert();
			});
		}

		private void makePart(ModelElement parent) {
			if(part == null) {
				part = new ModelElement(editor);
				add(parent, part);
				part.rotation = rotation;
				part.pos = pos;
				part.texture = true;
				if(cube != null) {
					part.showInEditor = cube.visibility;
					convert(part, group, cube);
					if(!reGrouped)convertGroup(part, group);
				} else {
					part.name = group.name;
					part.size = new Vec3f();
					convertGroup(part, group);
				}
				fixDecimals(part);
			}
		}

		public void resetTransform() {
			rotation = new Vec3f();
			pos = new Vec3f();
			reGrouped = true;
		}
	}
}
