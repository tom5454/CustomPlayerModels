package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.gui.popup.AnimEncConfigPopup;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.Button;
import com.tom.cpm.shared.gui.elements.ButtonIcon;
import com.tom.cpm.shared.gui.elements.Checkbox;
import com.tom.cpm.shared.gui.elements.ConfirmPopup;
import com.tom.cpm.shared.gui.elements.DropDownBox;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.gui.elements.PopupPanel;
import com.tom.cpm.shared.gui.elements.Spinner;
import com.tom.cpm.shared.gui.elements.TextField;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.util.CombinedListView;
import com.tom.cpm.shared.util.ListView;

public class AnimPanel extends Panel {

	public AnimPanel(IGui gui, EditorGui e, int height) {
		super(gui);
		Editor editor = e.getEditor();
		setBounds(new Box(0, 0, 145, height));
		setBackgroundColor(gui.getColors().panel_background);

		DropDownBox<IAnim> animSel = new DropDownBox<>(e, new CombinedListView<IAnim>(Collections.singletonList(new IAnim() {
			@Override
			public String toString() {
				return gui.i18nFormat("label.cpm.no_animation");
			}
		}), new ListView<>(editor.animations, v -> (IAnim) v)));
		animSel.setBounds(new Box(5, 5, 135, 20));
		addElement(animSel);
		animSel.setAction(() -> {
			IAnim sel = animSel.getSelected();
			editor.selectedAnim = sel instanceof EditorAnim ? (EditorAnim) sel : null;
			editor.updateGui();
		});
		editor.setSelAnim.add(animSel::setSelected);

		ButtonIcon newAnimBtn = new ButtonIcon(gui, "editor", 0, 16, () -> {
			PopupPanel pp = new PopupPanel(gui);
			pp.setBounds(new Box(0, 0, 200, 150));
			List<AnimType> ats = new ArrayList<>();
			for (VanillaPose p : VanillaPose.VALUES) {
				if(p == VanillaPose.CUSTOM)continue;
				if(editor.animations.stream().filter(a -> a.pose instanceof VanillaPose).noneMatch(v -> v.pose == p)) {
					ats.add(new AnimType(p));
				}
			}
			ats.add(new AnimType("pose", false));
			ats.add(new AnimType("gesture", true));

			DropDownBox<AnimType> typeDd = new DropDownBox<>(e, ats);
			typeDd.setBounds(new Box(5, 5, 190, 20));
			pp.addElement(typeDd);

			Checkbox boxAdd = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_additive"));
			boxAdd.setSelected(true);
			boxAdd.setBounds(new Box(5, 30, 60, 18));
			pp.addElement(boxAdd);
			boxAdd.setAction(() -> boxAdd.setSelected(!boxAdd.isSelected()));

			Checkbox boxLoop = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_loop"));
			boxLoop.setBounds(new Box(5, 50, 60, 18));
			pp.addElement(boxLoop);
			boxLoop.setAction(() -> boxLoop.setSelected(!boxLoop.isSelected()));

			pp.addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 70, 0, 0)));

			TextField nameField = new TextField(gui);
			nameField.setBounds(new Box(5, 80, 190, 20));
			pp.addElement(nameField);

			Runnable r = () -> {
				AnimType at = typeDd.getSelected();
				boxLoop.setEnabled(at.loop);
				nameField.setEnabled(at.pose == null);
			};
			typeDd.setAction(r);
			r.run();

			Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
				AnimType at = typeDd.getSelected();
				IPose pose = at.pose == null && at.option.equals("pose") ? new CustomPose(nameField.getText()) : at.pose;
				editor.addNewAnim(pose, pose == null ? nameField.getText() : null, boxAdd.isSelected(), at.loop && boxLoop.isSelected());
				pp.close();
			});
			okBtn.setBounds(new Box(80, 110, 40, 20));
			pp.addElement(okBtn);

			e.openPopup(pp);
		});
		newAnimBtn.setBounds(new Box(5, 30, 18, 18));
		addElement(newAnimBtn);

		ButtonIcon delAnimBtn = new ButtonIcon(gui, "editor", 14, 16, new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnim, null));
		delAnimBtn.setBounds(new Box(25, 30, 18, 18));
		addElement(delAnimBtn);
		editor.setAnimDelEn.add(delAnimBtn::setEnabled);

		Label currFrame = new Label(gui, "");
		currFrame.setBounds(new Box(30, 65, 90, 10));
		addElement(currFrame);

		Button prevFrm = new Button(gui, "<", editor::animPrevFrm);
		prevFrm.setBounds(new Box(5, 60, 18, 18));
		addElement(prevFrm);

		Button nextFrm = new Button(gui, ">", editor::animNextFrm);
		nextFrm.setBounds(new Box(120, 60, 18, 18));
		addElement(nextFrm);

		editor.setAnimFrame.add(i -> {
			prevFrm.setEnabled(i != null);
			nextFrm.setEnabled(i != null);
			if(i != null) {
				currFrame.setText(gui.i18nFormat("label.cpm.anim_frame_x", i));
			} else {
				currFrame.setText(gui.i18nFormat("label.cpm.anim_frame_none"));
			}
		});

		ButtonIcon newFrmBtn = new ButtonIcon(gui, "editor", 0, 16, editor::addNewAnimFrame);
		newFrmBtn.setBounds(new Box(5, 85, 18, 18));
		addElement(newFrmBtn);
		editor.setFrameAddEn.add(newFrmBtn::setEnabled);

		ButtonIcon delFrmBtn = new ButtonIcon(gui, "editor", 14, 16, new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnim, null));
		delFrmBtn.setBounds(new Box(25, 85, 18, 18));
		addElement(delFrmBtn);
		editor.setFrameDelEn.add(delFrmBtn::setEnabled);

		ButtonIcon playBtn = new ButtonIcon(gui, "editor", 56, 16, () -> {
			editor.playFullAnim = !editor.playFullAnim;
			editor.playStartTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
		});
		playBtn.setBounds(new Box(45, 85, 18, 18));
		addElement(playBtn);
		editor.setAnimPlayEn.add(playBtn::setEnabled);

		Label lblDuration = new Label(gui, gui.i18nFormat("label.cpm.duration"));
		lblDuration.setBounds(new Box(5, 110, 0, 0));
		addElement(lblDuration);

		Spinner duration = new Spinner(gui);
		duration.setDp(0);
		duration.setBounds(new Box(5, 120, 100, 18));
		addElement(duration);
		editor.setAnimDuration.add(i -> {
			duration.setEnabled(i != null);
			if(i != null)duration.setValue(i);
			else duration.setValue(1000);
		});
		duration.addChangeListener(() -> editor.setAnimDuration((int) duration.getValue()));

		PosPanel.addVec3("rotation", 145, v -> editor.setAnimRot(v), this, editor.setAnimRot, 1);
		PosPanel.addVec3("position", 175, v -> editor.setAnimPos(v), this, editor.setAnimPos, 2);

		ColorButton colorBtn = new ColorButton(gui, e, editor::setAnimColor);
		colorBtn.setBounds(new Box(5, 205, 100, 20));
		editor.setAnimColor.add(c -> {
			colorBtn.setEnabled(c != null);
			if(c != null)colorBtn.setColor(c);
			else colorBtn.setColor(0);
		});
		addElement(colorBtn);

		Checkbox boxShow = new Checkbox(gui, gui.i18nFormat("label.cpm.visible"));
		boxShow.setBounds(new Box(5, 230, 60, 18));
		boxShow.setAction(editor::switchAnimShow);
		editor.setAnimShow.add(b -> {
			boxShow.setEnabled(b != null);
			if(b != null)boxShow.setSelected(b);
			else boxShow.setSelected(false);
		});
		addElement(boxShow);

		Button encSettings = new Button(gui, gui.i18nFormat("button.cpm.animEncSettings"), () -> e.openPopup(new AnimEncConfigPopup(gui, editor, null)));
		encSettings.setBounds(new Box(5, 260, 135, 20));
		addElement(encSettings);
	}

	public interface IAnim {}

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
			if(pose != null)return gui.i18nFormat("label.cpm.anim_pose", pose.getName(gui::i18nFormat));
			return gui.i18nFormat("label.cpm.new_anim_" + option);
		}
	}
}
