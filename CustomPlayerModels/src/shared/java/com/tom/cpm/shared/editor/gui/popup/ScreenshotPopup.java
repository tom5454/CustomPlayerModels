package com.tom.cpm.shared.editor.gui.popup;

import java.util.function.Consumer;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.gui.ViewportPanelBase;
import com.tom.cpm.shared.gui.ViewportPanelBase.ViewportCamera;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;

public class ScreenshotPopup extends PopupPanel {

	public ScreenshotPopup(EditorGui eg, Consumer<Image> setter, Runnable onClosed) {
		super(eg.getGui());
		Editor e = eg.getEditor();
		ViewportCamera cam = e.description != null ? e.description.camera : new ViewportCamera();
		DisplayPanel camPanel = new DisplayPanel(eg, cam);
		e.displayViewport.accept(false);
		setOnClosed(() -> {
			e.displayViewport.accept(true);
			onClosed.run();
		});
		camPanel.setBounds(new Box(12, 10, 256, 256));
		addElement(camPanel);

		Button take = new Button(gui, gui.i18nFormat("button.cpm.takeScreenshot"), () -> {
			if(e.description == null)e.description = new ModelDescription();
			e.description.camera = cam;
			e.description.icon = camPanel.takeScreenshot(new Vec2i(256, 256));
			setter.accept(e.description.icon);
			e.markDirty();
			close();
		});
		take.setBounds(new Box(90, 270, 100, 20));
		addElement(take);

		setBounds(new Box(0, 0, 280, 300));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.screenshotPopup");
	}

	private static class DisplayPanel extends ViewportPanelBase {
		private Def def;
		private ViewportCamera cam;
		private EditorGui e;

		public DisplayPanel(EditorGui e, ViewportCamera cam) {
			super(e.getGui());
			this.cam = cam;
			this.def = new Def(e.getEditor());
			this.e = e;
		}

		@Override
		public void draw0(float partialTicks) {
			gui.drawBox(0, 0, bounds.w, bounds.h, 0xff333333);

			if(enabled) {
				nat.renderSetup();
				nat.render(partialTicks);
				nat.renderFinish();
			}
		}

		@Override
		public ViewportCamera getCamera() {
			return cam;
		}

		@Override
		public void preRender() {
			e.getEditor().preRender();
		}

		@Override
		public SkinType getSkinType() {
			return e.getEditor().skinType;
		}

		@Override
		public ModelDefinition getDefinition() {
			return def;
		}

		@Override
		public boolean isTpose() {
			return false;
		}

		@Override
		public boolean applyLighting() {
			return true;
		}
	}

	private static class Def extends ModelDefinition {
		private Editor editor;

		public Def(Editor editor) {
			this.editor = editor;
		}

		@Override
		public PartRoot getModelElementFor(VanillaModelPart part) {
			PartRoot root = new PartRoot();
			editor.elements.forEach(e -> {
				RootModelElement el = (RootModelElement) e.rc;
				if(el.getPart() == part) {
					root.add(el);
					if(!e.duplicated)root.setMainRoot(el);
				}
			});
			return root;
		}

		@Override
		public TextureProvider getTexture(TextureSheetType key) {
			ETextures tex = editor.textures.get(key);
			return tex != null ? tex.getRenderTexture() : null;
		}

		@Override
		public SkinType getSkinType() {
			return editor.skinType;
		}

		@Override
		public boolean hasRoot(VanillaModelPart type) {
			return editor.elements.stream().map(e -> ((RootModelElement) e.rc).getPart()).anyMatch(t -> t == type);
		}
	}
}
