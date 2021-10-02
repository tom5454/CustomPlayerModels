package com.tom.cpm.shared.editor.template.args;

import java.util.List;
import java.util.Map;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec3i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplayType;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler.TemplateArg;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.template.args.TexArg;

public class TexEditorArg implements TemplateArg<TexArg> {
	public static final String NAME = "__tex";
	private int u, v, texSize;
	private TexArg arg;
	private EditorTemplate et;

	@Override
	public void saveProject(Map<String, Object> map) {

	}

	@Override
	public void loadProject(Map<String, Object> map) {

	}

	@Override
	public TexArg export() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void applyArgs(Map<String, Object> map, List<ModelElement> parts) {}

	@Override
	public void loadTemplate(TexArg arg) {
		this.arg = arg;
		u = arg.u;
		v = arg.v;
		texSize = arg.texSize;
	}

	@Override
	public void applyToArg() {
		arg.u = u;
		arg.v = v;
		arg.texSize = texSize;
	}

	@Override
	public boolean requiresParts() {
		throw new UnsupportedOperationException();
	}

	private void setUV(Editor editor, Vec3f vec) {
		editor.action("set", "action.cpm.texUV").
		updateValueOp(this, u, (int) vec.x, 0, Integer.MAX_VALUE, (a, b) -> a.u = b, v -> editor.setTexturePanel.accept(new Vec3i(u, v, texSize))).
		updateValueOp(this, v, (int) vec.y, 0, Integer.MAX_VALUE, (a, b) -> a.v = b, v -> editor.setTexturePanel.accept(new Vec3i(u, v, texSize))).
		updateValueOp(this, texSize, (int) vec.z, 0, 64, (a, b) -> a.texSize = b, v -> editor.setTexturePanel.accept(new Vec3i(u, v, texSize))).
		execute();
		editor.templates.forEach(EditorTemplate::applyToModel);
	}

	private void draw(Editor editor, IGui gui, int x, int y, float xs, float ys, int alpha) {
		if(texSize != 0 && et.getTemplateTexture() != null) {
			int bx = (int) (xs * u * texSize);
			int by = (int) (ys * v * texSize);
			Image img = et.getTemplateTexture().getImage();

			gui.drawBox(x + bx, y + by, img.getWidth() * xs * texSize, img.getHeight() * ys * texSize, 0xffffff | (alpha << 24));
		}
	}

	@Override
	public void createTreeElements(List<TreeElement> c, Editor editor) {
		c.add(new TreeElement() {

			@Override
			public String getName() {
				return editor.gui().i18nFormat("label.cpm.template_tex_setup");
			}

			@Override
			public void updateGui() {
				if(texSize != 0) {
					editor.setModeBtn.accept(editor.gui().i18nFormat("button.cpm.template_remove_tex"));
				}
				editor.setModePanel.accept(ModeDisplayType.TEX);
				editor.setTexturePanel.accept(new Vec3i(u, v, texSize));
			}

			@Override
			public void modeSwitch() {
				if(texSize != 0) {
					editor.frame.openPopup(new ConfirmPopup(editor.frame, editor.gui().i18nFormat("label.cpm.confirmDel"),
							editor.gui().i18nFormat("label.cpm.template_remove_tex"), () -> {
								editor.action("i", "label.cpm.template_remove_tex").updateValueOp(TexEditorArg.this, texSize, 0, (a, b) -> a.texSize = b).execute();
								editor.updateGui();
							}, null));
				}
			}

			@Override
			public void setVec(Vec3f vec, VecType object) {
				if(object == VecType.TEXTURE) {
					setUV(editor, vec);
				}
			}

			@Override
			public ETextures getTexture() {
				return editor.textures.get(TextureSheetType.SKIN);
			}

			@Override
			public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
				draw(editor, gui, x, y, xs, ys, editor.selectedElement == this ? 0xcc : 0x55);
			}
		});
		c.add(new TreeElement() {

			@Override
			public String getName() {
				return editor.gui().i18nFormat("label.cpm.template_place_tex");
			}

			@Override
			public void modeSwitch() {
				if(texSize == 0)texSize = 1;
				editor.frame.openPopup(new ConfirmPopup(editor.frame,
						editor.gui().i18nFormat("label.cpm.template_place_tex.title"),
						editor.gui().i18nFormat("label.cpm.template_place_tex.desc"),
						() -> {
							ETextures tex = editor.textures.get(TextureSheetType.SKIN);
							ActionBuilder ab = editor.action("placeTex").
									updateValueOp(tex, new Image(tex.getImage()), tex.getImage(), ETextures::setImage).
									onAction(editor::restitchTextures);
							tex.getImage().draw(et.getTemplateTexture().getImage(), u, v);
							ab.execute();
							editor.updateGui();
						}, null));
			}

			@Override
			public void updateGui() {
				editor.setModeBtn.accept(editor.gui().i18nFormat("button.cpm.template_place_tex"));
				editor.setModePanel.accept(ModeDisplayType.TEX);
				editor.setTexturePanel.accept(new Vec3i(u, v, texSize));
			}

			@Override
			public void setVec(Vec3f vec, VecType object) {
				if(object == VecType.TEXTURE) {
					setUV(editor, vec);
				}
			}

			@Override
			public ETextures getTexture() {
				return editor.textures.get(TextureSheetType.SKIN);
			}

			@Override
			public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
				if(editor.selectedElement == this)
					draw(editor, gui, x, y, xs, ys, 0xcc);
			}
		});
	}

	public void bind(EditorTemplate et) {
		this.et = et;
	}
}
