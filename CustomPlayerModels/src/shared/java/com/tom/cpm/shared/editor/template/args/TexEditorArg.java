package com.tom.cpm.shared.editor.template.args;

import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.EditorTexture;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.gui.PosPanel.ModeDisplType;
import com.tom.cpm.shared.editor.template.EditorTemplate;
import com.tom.cpm.shared.editor.template.TemplateArgHandler.TemplateArg;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.editor.util.ValueOp;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.ConfirmPopup;
import com.tom.cpm.shared.math.Vec3f;
import com.tom.cpm.shared.math.Vec3i;
import com.tom.cpm.shared.template.args.TexArg;
import com.tom.cpm.shared.util.Image;

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
		editor.addUndo(
				new ValueOp<>(TexEditorArg.this, u, (a, b) -> a.u = b),
				new ValueOp<>(TexEditorArg.this, v, (a, b) -> a.v = b),
				new ValueOp<>(TexEditorArg.this, texSize, (a, b) -> a.texSize = b)
				);
		u = (int) vec.x;
		v = (int) vec.y;
		texSize = (int) vec.z;
		boolean refreshGui = false;
		if(u < 0) {
			u = 0;
			refreshGui = true;
		}
		if(v < 0) {
			v = 0;
			refreshGui = true;
		}
		if(texSize < 0) {
			texSize = 0;
			refreshGui = true;
		}
		editor.setCurrentOp(
				new ValueOp<>(TexEditorArg.this, u, (a, b) -> a.u = b),
				new ValueOp<>(TexEditorArg.this, v, (a, b) -> a.v = b),
				new ValueOp<>(TexEditorArg.this, texSize, (a, b) -> a.texSize = b)
				);
		if(refreshGui)editor.setTexturePanel.accept(new Vec3i(u, v, texSize));
		editor.templates.forEach(EditorTemplate::applyToModel);
		editor.markDirty();
	}

	private void draw(Editor editor, IGui gui, int x, int y, float xs, float ys) {
		if(texSize != 0 && et.getTemplateTexture() != null) {
			int bx = (int) (xs * u * texSize);
			int by = (int) (ys * v * texSize);
			Image img = et.getTemplateTexture().getImage();
			Image skin = editor.skinProvider.getImage();

			gui.drawBox(x + bx, y + by, img.getWidth() * xs, img.getHeight() * ys, 0xccffffff);
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
				editor.setModePanel.accept(ModeDisplType.TEX);
				editor.setTexturePanel.accept(new Vec3i(u, v, texSize));
			}

			@Override
			public void modeSwitch() {
				if(texSize != 0) {
					editor.frame.openPopup(new ConfirmPopup(editor.frame, editor.gui().i18nFormat("label.cpm.confirmDel"),
							editor.gui().i18nFormat("label.cpm.template_remove_tex"), () -> {
								editor.addUndo(new ValueOp<>(TexEditorArg.this, texSize, (a, b) -> a.texSize = b));
								editor.runOp(new ValueOp<>(TexEditorArg.this, 0, (a, b) -> a.texSize = b));
								editor.markDirty();
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
			public EditorTexture getTexture() {
				return editor.skinProvider;
			}

			@Override
			public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
				draw(editor, gui, x, y, xs, ys);
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
							Image bak = new Image(editor.skinProvider.getImage());
							editor.addUndo(() -> {
								editor.skinProvider.setImage(bak);
								editor.restitchTexture();
							});
							editor.runOp(() -> {
								editor.skinProvider.getImage().draw(et.getTemplateTexture().getImage(), u, v);
								editor.restitchTexture();
								editor.skinProvider.markDirty();
								editor.renderTexture.markDirty();
							});
							editor.updateGui();
							editor.markDirty();
						}, null));
			}

			@Override
			public void updateGui() {
				editor.setModeBtn.accept(editor.gui().i18nFormat("button.cpm.template_place_tex"));
				editor.setModePanel.accept(ModeDisplType.TEX);
				editor.setTexturePanel.accept(new Vec3i(u, v, texSize));
			}

			@Override
			public void setVec(Vec3f vec, VecType object) {
				if(object == VecType.TEXTURE) {
					setUV(editor, vec);
				}
			}

			@Override
			public EditorTexture getTexture() {
				return editor.skinProvider;
			}

			@Override
			public void drawTexture(IGui gui, int x, int y, float xs, float ys) {
				draw(editor, gui, x, y, xs, ys);
			}
		});
	}

	public void bind(EditorTemplate et) {
		this.et = et;
	}
}
