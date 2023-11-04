package com.tom.cpm.shared.editor.gui.popup;

import java.util.function.Consumer;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.EmbeddedLocalizations;
import com.tom.cpl.util.Image;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.ETextures;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.util.ModelDescription;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ViewportPanelBase3d;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.render.VanillaModelPart;
import com.tom.cpm.shared.skin.TextureProvider;
import com.tom.cpm.shared.util.Log;

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

		Button openFile = new Button(gui, gui.i18nFormat("button.cpm.importIconImage"), () -> {
			FileChooserPopup fc = new FileChooserPopup(eg);
			fc.setTitle(EmbeddedLocalizations.importFile);
			fc.setFileDescText(EmbeddedLocalizations.filePng);
			fc.setFilter(new FileFilter("png"));
			fc.setExtAdder(n -> n + ".png");
			fc.setAccept(f -> {
				Image.loadFrom(f).thenAcceptAsync(img -> {
					Image i;
					if(img.getWidth() != 256 || img.getHeight() != 256) {
						i = new Image(256, 256);
						i.draw(img, 0, 0, 256, 256);
					} else i = img;
					if(e.description == null)e.description = new ModelDescription();
					e.description.camera = cam;
					e.description.icon = i;
					setter.accept(e.description.icon);
					e.markDirty();
					close();
				}, gui::executeLater).exceptionally(ex -> {
					Log.error("Failed to load image", ex);
					gui.displayMessagePopup(gui.i18nFormat("label.cpm.error"), gui.i18nFormat("error.cpm.img_load_failed", ex.getLocalizedMessage()));
					return null;
				});
			});
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			eg.openPopup(fc);
		});
		openFile.setBounds(new Box(40, 270, 100, 20));
		addElement(openFile);

		Button takeScreenshot = new Button(gui, gui.i18nFormat("button.cpm.takeScreenshot"), () -> {
			if(e.description == null)e.description = new ModelDescription();
			e.description.camera = cam;
			e.description.icon = camPanel.takeScreenshot(new Vec2i(256, 256));
			setter.accept(e.description.icon);
			e.markDirty();
			close();
		});
		takeScreenshot.setBounds(new Box(145, 270, 100, 20));
		addElement(takeScreenshot);

		setBounds(new Box(0, 0, 280, 300));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.screenshotPopup");
	}

	private static class DisplayPanel extends ViewportPanelBase3d {
		private Def def;
		private ViewportCamera cam;
		private EditorGui e;

		public DisplayPanel(EditorGui e, ViewportCamera cam) {
			super(e);
			this.cam = cam;
			this.def = new Def(e.getEditor());
			this.e = e;
		}

		@Override
		public void render(MatrixStack stack, VBuffers buf, float partialTicks) {
			renderModel(stack, buf, partialTicks);
		}

		@Override
		public ViewportCamera getCamera() {
			return cam;
		}

		@Override
		public void preRender(MatrixStack stack, VBuffers buf) {
			e.getEditor().preRender();
		}

		@Override
		public ModelDefinition getDefinition() {
			return def;
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
		public TextureProvider getTexture(TextureSheetType key, boolean inGui) {
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
