package com.tom.cpm.shared.editor.anim;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimatedTexture;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.ModeDisplayType;
import com.tom.cpm.shared.editor.project.JsonMap;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.model.TextureSheetType;

public class AnimatedTex implements TreeElement {
	private final Editor editor;
	private List<TreeElement> options;
	public Vec2i uvStart, uvSize;
	public Vec2i animStart;
	public int frameTime;
	public int frameCount;
	public boolean anX, interpolate;
	private final TextureSheetType sheet;

	public AnimatedTex(Editor editor, TextureSheetType sheet, JsonMap map) {
		this(editor, sheet);
		frameTime = map.getInt("frameTime", 0);
		frameCount = map.getInt("frameCount", 0);
		uvStart = map.getObject("start", Vec2i::new, new Vec2i());
		uvSize = map.getObject("size", Vec2i::new, new Vec2i());
		animStart = map.getObject("from", Vec2i::new, new Vec2i());
		anX = map.getBoolean("anX");
		interpolate = map.getBoolean("interpolate");
	}

	public AnimatedTex(Editor editor, TextureSheetType sheet) {
		this.editor = editor;
		this.sheet = sheet;
		uvStart = new Vec2i();
		uvSize = new Vec2i();
		animStart = new Vec2i();
		options = new ArrayList<>();
		options.add(new TexElem("start", () -> uvStart));
		options.add(new TexElem("size", () -> uvSize));
		options.add(new TexElem("from", () -> animStart));
		options.add(new ValElem("frameTime", () -> frameTime, v -> frameTime = v));
		options.add(new ValElem("frameCount", () -> frameCount, v -> frameCount = v));
		options.add(new BoolElem("anX", () -> anX, v -> anX = v));
		options.add(new BoolElem("interpolate", () -> interpolate, v -> interpolate = v));
	}

	public void save(JsonMap map) {
		map.put("frameTime", frameTime);
		map.put("frameCount", frameCount);
		map.put("start", uvStart.toMap());
		map.put("size", uvSize.toMap());
		map.put("from", animStart.toMap());
		map.put("anX", anX);
		map.put("interpolate", interpolate);
	}

	private int lastFrame;
	private long lastUpdate;

	public boolean apply(Image ri, Image img) {
		if(frameCount == 0 || frameTime == 0)return false;
		long time = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
		int frm = (int) (time / frameTime % frameCount);
		if(interpolate) {
			if(time - lastUpdate > 50) {
				float t = frm + (time % frameTime) / (float) frameTime;
				int uvx = uvStart.x;
				int uvy = uvStart.y;
				int sx = uvSize.x;
				int sy = uvSize.y;
				int ax = animStart.x;
				int ay = animStart.y;
				AnimatedTexture.copyTextureInt(ri, img, uvx, uvy, sx, sy, ax, ay, t, anX, frameCount);
				return true;
			}
		} else if(frm != lastFrame) {
			lastFrame = frm;
			int uvx = uvStart.x;
			int uvy = uvStart.y;
			int sx = uvSize.x;
			int sy = uvSize.y;
			int ax = animStart.x;
			int ay = animStart.y;
			AnimatedTexture.copyTexture(ri, img, uvx, uvy, sx, sy, ax, ay, frm, anX);
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return editor.gui().i18nFormat("label.cpm.tree.animatedRegion");
	}

	@Override
	public void getTreeElements(Consumer<TreeElement> c) {
		options.forEach(c);
	}

	@Override
	public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
		gui.drawBox(x + uvStart.x * xs, y + uvStart.y * ys, uvSize.x * xs, uvSize.y * ys, 0x80ffff00);
		gui.drawBox(x + animStart.x * xs, y + animStart.y * ys, uvSize.x * xs, uvSize.y * ys, 0x80ffffff);
		if(anX)
			gui.drawBox(x + (animStart.x + uvSize.x) * xs, y + animStart.y * ys, (uvSize.x * (frameCount - 1)) * xs, uvSize.y * ys, 0x80aaaaaa);
		else
			gui.drawBox(x + animStart.x * xs, y + (animStart.y + uvSize.y) * ys, uvSize.x * xs, (uvSize.y * (frameCount - 1)) * ys, 0x80aaaaaa);
	}

	@Override
	public ETextures getTexture() {
		ETextures tex = editor.textures.get(sheet);
		if(tex != null)return tex;
		return editor.textures.get(TextureSheetType.SKIN);
	}

	@Override
	public void delete() {
		ETextures tex = editor.textures.get(sheet);
		editor.action("remove", "action.cpm.animTex").removeFromList(tex.animatedTexs, this).onRun(() -> {
			editor.selectedElement = null;
			tex.refreshTexture();
		}).execute();
		editor.updateGui();
	}

	@Override
	public void updateGui() {
		editor.setDelEn.accept(true);
	}

	private class OptionElem implements TreeElement {
		protected String name;

		public OptionElem(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return editor.gui().i18nFormat("label.cpm.tree.at." + name);
		}

		@Override
		public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
			AnimatedTex.this.drawTexture(gui, x, y, xs, ys);
		}

		@Override
		public ETextures getTexture() {
			return AnimatedTex.this.getTexture();
		}

		@Override
		public Tooltip getTooltip() {
			return new Tooltip(editor.frame, editor.gui().i18nFormat("tooltip.cpm.tree.at." + name));
		}
	}

	private class TexElem extends OptionElem {
		private Supplier<Vec2i> vec;

		public TexElem(String name, Supplier<Vec2i> vec) {
			super(name);
			this.vec = vec;
		}

		@Override
		public void updateGui() {
			Vec2i vec = this.vec.get();
			editor.setModePanel.accept(ModeDisplayType.TEX);
			editor.setTexturePanel.accept(new Vec3i(vec.x, vec.y, 0));
		}

		@Override
		public void setVec(Vec3f v, VecType object) {
			if(object == VecType.TEXTURE) {
				Vec2i vec = this.vec.get();
				vec.x = (int) v.x;
				vec.y = (int) v.y;
				editor.action("set", "label.cpm.tree.at." + name).
				updateValueOp(vec, vec.x, (int) v.x, (a, b) -> a.x = b).
				updateValueOp(vec, vec.y, (int) v.y, (a, b) -> a.y = b).execute();
			}
		}
	}

	private class ValElem extends OptionElem {
		private IntSupplier get;
		private IntConsumer set;

		public ValElem(String name, IntSupplier get, IntConsumer set) {
			super(name);
			this.get = get;
			this.set = set;
		}

		@Override
		public void updateGui() {
			editor.setModePanel.accept(ModeDisplayType.VALUE);
			editor.setValue.accept((float) get.getAsInt());
		}

		@Override
		public void setValue(float value) {
			set.accept((int) value);
			editor.action("set", "label.cpm.tree.at." + name).
			updateValueOp(set, get.getAsInt(), (int) value, IntConsumer::accept).execute();
		}
	}

	private class BoolElem extends OptionElem {
		private BooleanSupplier get;
		private Consumer<Boolean> set;

		public BoolElem(String name, BooleanSupplier get, Consumer<Boolean> set) {
			super(name);
			this.get = get;
			this.set = set;
		}

		@Override
		public void updateGui() {
			editor.setModeBtn.accept(editor.gui().i18nFormat("button.cpm.at." + name + "." + (get.getAsBoolean() ? "on" : "off")));
		}

		@Override
		public void modeSwitch() {
			editor.action("set", "label.cpm.tree.at." + name).
			updateValueOp(set, get.getAsBoolean(), !get.getAsBoolean(), Consumer::accept).
			execute();
			updateGui();
		}
	}
}
