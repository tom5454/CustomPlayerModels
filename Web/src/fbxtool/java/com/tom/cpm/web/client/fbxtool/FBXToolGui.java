package com.tom.cpm.web.client.fbxtool;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.util.EmbeddedLocalizations;
import com.tom.cpl.util.Hand;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.project.ProjectFile;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel;
import com.tom.cpm.shared.gui.panel.ModelDisplayPanel.IModelDisplayPanel;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.model.builtin.VanillaPlayerModel;
import com.tom.cpm.shared.model.render.PlayerModelSetup;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.web.client.LocalStorageFS;
import com.tom.cpm.web.client.fbxtool.FBXRenderer.FBXDefinition;
import com.tom.cpm.web.client.fbxtool.three.GLTFExporter;
import com.tom.cpm.web.client.fbxtool.three.Mesh;
import com.tom.cpm.web.client.fbxtool.three.Scene;
import com.tom.cpm.web.client.fbxtool.three.SkeletonHelper;
import com.tom.cpm.web.client.fbxtool.three.ThreeModule;

public class FBXToolGui extends Frame implements IModelDisplayPanel {
	private Editor editor;
	private ThreePreview three;
	private Checkbox humanoidRig;

	public FBXToolGui(IGui gui) {
		super(gui);
		this.editor = new Editor();
		this.editor.definition = new FBXDefinition(editor);
		this.editor.setUI(gui);
		this.editor.loadDefaultPlayerModel();
		this.editor.playerTpose.accept(true);
	}

	@Override
	public void initFrame(int width, int height) {
		Panel leftPanel = new Panel(gui);
		leftPanel.setBackgroundColor(gui.getColors().button_fill);
		leftPanel.setBounds(new Box(0, 0, width / 2, height));
		ModelDisplayPanel modelPanel = new ModelDisplayPanel(this, this) {

			@Override
			protected void poseModel(VanillaPlayerModel p, MatrixStack stack, float partialTicks) {
				p.reset();
				p.setAllVisible(true);
				PlayerModelSetup.setRotationAngles(p, 0, 0, Hand.RIGHT, false);
				p.rightArm.zRot = (float) Math.toRadians(90);
				p.leftArm.zRot = (float) Math.toRadians(-90);
			}
		};
		modelPanel.setBounds(new Box(1, 1, width / 2 - 2, height / 2 - 2));
		modelPanel.setLoadingText(gui.i18nFormat("label.cpm.loading"));
		modelPanel.setBackgroundColor(gui.getColors().panel_background);
		leftPanel.addElement(modelPanel);

		addElement(leftPanel);

		three = new ThreePreview(gui, 0, height / 2 + 1, width / 2, height / 2, this::updatePreview);
		addElement(three);

		Panel right = new Panel(gui);
		right.setBounds(new Box(width / 2, 0, width / 2, height));
		addElement(right);

		Button load = new Button(gui, gui.i18nFormat("button.cpm.file.load"), () -> {
			FileChooserPopup fc = new FileChooserPopup(this);
			fc.setTitle(EmbeddedLocalizations.loadProject);
			fc.setFileDescText(EmbeddedLocalizations.fileProject);
			fc.setFilter(new FileFilter("cpmproject"));
			fc.setAccept(this::load);
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			openPopup(fc);
		});
		load.setBounds(new Box(5, 25, 100, 20));
		right.addElement(load);

		Label title = new Label(gui, "CPM FBX Tool (Alpha)");
		title.setBounds(new Box(5, 5, 0, 0));
		right.addElement(title);

		Button export = new Button(gui, "Export GLTF", this::export);
		export.setBounds(new Box(5, height - 25, 100, 20));
		right.addElement(export);

		ScrollPanel scp = new ScrollPanel(gui);
		Panel p = new Panel(gui);
		scp.setBounds(new Box(0, 50, width / 2, height - 80));
		p.setBounds(new Box(0, 0, width / 2, 0));
		scp.setDisplay(p);
		right.addElement(scp);

		FlowLayout layout = new FlowLayout(p, 4, 1);

		boolean hr = humanoidRig != null ? humanoidRig.isSelected() : true;
		humanoidRig = new Checkbox(gui, "Humanoid Rig");
		humanoidRig.setSelected(hr);
		humanoidRig.setBounds(new Box(5, 0, 120, 20));
		humanoidRig.setAction(() -> {
			humanoidRig.setSelected(!humanoidRig.isSelected());
			three.markDirty();
		});
		p.addElement(humanoidRig);

		layout.reflow();
	}

	@Override
	public void filesDropped(List<File> files) {
		super.filesDropped(files);
		//TODO
	}

	@Override
	public ModelDefinition getSelectedDefinition() {
		return editor.definition;
	}

	@Override
	public ViewportCamera getCamera() {
		return editor.camera;
	}

	@Override
	public void preRender() {
		editor.preRender();
	}

	@Override
	public boolean doRender() {
		return true;
	}

	private void load(File file) {
		editor.load(file).handleAsync((v, e) -> {
			if(e != null) {
				Log.warn("Error loading project file", e);
				ErrorLog.addFormattedLog(LogLevel.ERROR, "label.cpm.error.load", e);
				showError("load", e.toString());
				editor.setInfoMsg.accept(Pair.of(0, ""));
				editor.loadDefaultPlayerModel();
				editor.updateGui();
			}
			three.markDirty();
			return null;
		}, gui::executeLater);
	}

	private void showError(String msg, String error) {
		openPopup(new MessagePopup(this, gui.i18nFormat("label.cpm.error." + msg), error));
	}

	public void export() {
		Scene sc = new Scene();
		FBXCreator c = new FBXCreator();
		c.setHumanoidRig(humanoidRig.isSelected());
		editor.applyAnim = true;
		editor.preRender();
		c.render((FBXDefinition) editor.definition, sc::add);
		editor.applyAnim = false;
		Map<TextureSheetType, Image> tex = c.getTextures();
		GLTFExporter ex = new GLTFExporter();
		ProjectFile pf = new ProjectFile();
		for(Entry<TextureSheetType, Image> e : tex.entrySet()) {
			try(OutputStream os = pf.setAsStream(e.getKey().name().toLowerCase(Locale.ROOT) + ".png")) {
				ImageIO.write(e.getValue(), os);
			} catch (IOException e1) {
			}
		}
		String file = editor.file == null ? gui.i18nFormat("label.cpm.new_project") : editor.file.getName();
		ex.export(sc).then(s -> {
			pf.setEntry("model.gltf", s.getBytes(StandardCharsets.UTF_8));
			return pf.save();
		}).then(b -> {
			LocalStorageFS.saveAs(b, file.substring(0, file.length() - ".cpmproject".length()) + ".zip");
			return null;
		});
	}

	private void updatePreview() {
		FBXCreator c = new FBXCreator();
		c.setHumanoidRig(humanoidRig.isSelected());
		ThreeModule.clearScene();
		editor.applyAnim = true;
		editor.preRender();
		c.render((FBXDefinition) editor.definition, e -> {
			ThreeModule.scene.add(e);
			if(e instanceof Mesh) {
				SkeletonHelper h = new SkeletonHelper((Mesh) e);
				h.material.lineWidth = 2f;
				ThreeModule.scene.add(h);
			}
		});
		editor.applyAnim = false;
	}
}
