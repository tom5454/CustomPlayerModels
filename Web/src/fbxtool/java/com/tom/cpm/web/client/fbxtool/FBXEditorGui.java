package com.tom.cpm.web.client.fbxtool;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.web.client.WebMC;
import com.tom.cpm.web.client.fbxtool.FBXRenderer.FBXDefinition;
import com.tom.cpm.web.client.fbxtool.three.Mesh;
import com.tom.cpm.web.client.fbxtool.three.SkeletonHelper;
import com.tom.cpm.web.client.fbxtool.three.ThreeModule;

import elemental2.dom.DomGlobal;

public class FBXEditorGui extends EditorGui {
	private ThreePreview three;

	public FBXEditorGui(IGui gui) {
		super(gui);
		Editor editor = getEditor();
		editor.definition = new FBXDefinition(editor);
		editor.playerTpose.accept(true);
	}

	private void updatePreview() {
		try {
			Editor editor = getEditor();
			FBXCreator c = new FBXCreator();
			c.setHumanoidRig(true);
			ThreeModule.clearScene();
			three.hover = null;
			editor.applyAnim = true;
			editor.preRender();
			c.render((FBXDefinition) editor.definition, e -> {
				ThreeModule.scene.add(e);
				if(e instanceof Mesh) {
					SkeletonHelper h = new SkeletonHelper((Mesh) e);
					h.material.lineWidth = 2f;
					ThreeModule.scene.add(h);
					three.hover = h;
					DomGlobal.console.log(h);
				}
			});
			editor.applyAnim = false;
		} catch (Exception e) {
			WebMC.getInstance().error("Error updating FBX", e);
		}
	}

	@Override
	public void initFrame(int width, int height) {
		super.initFrame(width / 2, height);
		setBounds(new Box(0, 0, width, height));

		three = new ThreePreview(gui, width / 2, 0, width / 2, height, this::updatePreview);
		addElement(three);

		getEditor().updateGui.add(three::markDirty);
	}
}
