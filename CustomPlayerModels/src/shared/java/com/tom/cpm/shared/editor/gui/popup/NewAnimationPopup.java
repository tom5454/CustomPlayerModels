package com.tom.cpm.shared.editor.gui.popup;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.Checkbox;
import com.tom.cpm.shared.gui.elements.DropDownBox;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.TextField;
import com.tom.cpm.shared.math.Box;

public class NewAnimationPopup extends PopupPanel {

	public NewAnimationPopup(IGui gui, Editor editor) {
		super(gui);
		setBounds(new Box(0, 0, 200, 150));

		List<AnimType> ats = new ArrayList<>();
		for (VanillaPose p : VanillaPose.VALUES) {
			if(p == VanillaPose.CUSTOM)continue;
			ats.add(new AnimType(p));
		}
		ats.add(new AnimType("pose", false));
		ats.add(new AnimType("gesture", true));

		DropDownBox<AnimType> typeDd = new DropDownBox<>(editor.frame, ats);
		typeDd.setBounds(new Box(5, 5, 190, 20));
		this.addElement(typeDd);

		Checkbox boxAdd = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_additive"));
		boxAdd.setSelected(true);
		boxAdd.setBounds(new Box(5, 30, 60, 18));
		this.addElement(boxAdd);
		boxAdd.setAction(() -> boxAdd.setSelected(!boxAdd.isSelected()));

		Checkbox boxLoop = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_loop"));
		boxLoop.setBounds(new Box(5, 50, 60, 18));
		this.addElement(boxLoop);
		boxLoop.setAction(() -> boxLoop.setSelected(!boxLoop.isSelected()));

		this.addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 70, 0, 0)));

		TextField nameField = new TextField(gui);
		nameField.setBounds(new Box(5, 80, 190, 20));
		this.addElement(nameField);

		Runnable r = () -> {
			AnimType at = typeDd.getSelected();
			boxLoop.setEnabled(at.loop);
		};
		typeDd.setAction(r);
		r.run();

		Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			AnimType at = typeDd.getSelected();
			IPose pose = at.pose == null && at.option.equals("pose") ? new CustomPose(nameField.getText()) : at.pose;
			editor.addNewAnim(pose, nameField.getText(), boxAdd.isSelected(), at.loop && boxLoop.isSelected());
			this.close();
		});
		okBtn.setBounds(new Box(80, 110, 40, 20));
		this.addElement(okBtn);
	}

	private class AnimType {
		private VanillaPose pose;
		private String option;
		private boolean loop;
		public AnimType(VanillaPose pose) {
			this.pose = pose;
		}

		public AnimType(String option, boolean loop) {
			this.option = option;
			this.loop = loop;
		}

		@Override
		public String toString() {
			if(pose != null)return gui.i18nFormat("label.cpm.anim_pose", pose.getName(gui, null));
			return gui.i18nFormat("label.cpm.new_anim_" + option);
		}
	}
}
