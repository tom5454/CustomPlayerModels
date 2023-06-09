package com.tom.cpm.shared.editor.gui;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.UpdaterRegistry;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.model.PartPosition;

public class FirstPersonHandPosGui extends Frame {
	private Editor e;
	private ModelDefinition tempDef;

	public FirstPersonHandPosGui(IGui gui) {
		super(gui);
	}

	@Override
	public void initFrame(int width, int height) {
		e = EditorGui.getActiveTestingEditor();
		if(e == null) {
			String str = "How did you get here?";
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			addElement(lbl);
			return;
		}
		Player<?> pl = MinecraftClientAccess.get().getCurrentClientPlayer();
		if(pl == null || pl.getModelDefinition() == null) {
			String str = "How did you get here?";
			Label lbl = new Label(gui, str);
			lbl.setBounds(new Box(width / 2 - gui.textWidth(str) / 2, height / 2 - 4, 0, 0));
			addElement(lbl);
			return;
		}
		tempDef = pl.getModelDefinition();
		if(tempDef.fpLeftHand == null)tempDef.fpLeftHand = new PartPosition();
		if(tempDef.fpRightHand == null)tempDef.fpRightHand = new PartPosition();

		makeSettings(width, height, 0, e.leftHandPos, tempDef.fpLeftHand, 0);
		makeSettings(width, height, 1, e.rightHandPos, tempDef.fpRightHand, 170);
	}

	private void makeSettings(int width, int height, int name, PartPosition pp, PartPosition pp2, int x) {
		Panel p = new Panel(gui);
		p.setBounds(new Box(width / 2 - 170 + x, height / 2 - 50, 170, 100));
		addElement(p);

		p.addElement(new Label(gui, gui.i18nFormat("label.cpm.firstPersonHand.offset_" + name)).setBounds(new Box(0, 0, 100, 10)));

		TabFocusHandler tabHandler = new TabFocusHandler(gui);
		FlowLayout layout = new FlowLayout(p, 4, 1);

		addPart("rotation", p, tabHandler, 1, pp, pp2, PartPosition::getRRotation, PartPosition::setRRotation, PartPosition::setRotationDeg);
		addPart("position", p, tabHandler, 2, pp, pp2, PartPosition::getRPos, PartPosition::setRPos);
		addPart("scale", p, tabHandler, 2, pp, pp2, PartPosition::getRScale, PartPosition::setRScale);

		addElement(tabHandler);

		layout.reflow();
	}

	private void addPart(String name, Panel panel, TabFocusHandler tabHandler, int dp, PartPosition pp, PartPosition pp2, Function<PartPosition, Vec3f> getter, BiConsumer<PartPosition, Vec3f> setter) {
		addPart(name, panel, tabHandler, dp, pp, pp2, getter, setter, setter);
	}

	private void addPart(String name, Panel panel, TabFocusHandler tabHandler, int dp, PartPosition pp, PartPosition pp2, Function<PartPosition, Vec3f> getter, BiConsumer<PartPosition, Vec3f> setter, BiConsumer<PartPosition, Vec3f> setter2) {
		PosPanel.addVec3(name, v -> {
			e.action("set", "label.cpm." + name).updateValueOp(pp, getter.apply(pp), v, (a, p) -> {
				setter.accept(pp, p);
				setter2.accept(pp2, p);
			}).execute();
		}, panel, UpdaterRegistry.makeStatic(() -> getter.apply(pp)), dp, tabHandler);
	}
}
