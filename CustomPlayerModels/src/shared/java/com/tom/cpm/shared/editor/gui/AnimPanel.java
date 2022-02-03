package com.tom.cpm.shared.editor.gui;

import java.util.Collections;
import java.util.Comparator;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.ListPicker;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.CombinedListView;
import com.tom.cpl.util.ListView;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.gui.popup.AnimEncConfigPopup;
import com.tom.cpm.shared.editor.gui.popup.AnimationSettinsPopup;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;

public class AnimPanel extends Panel {
	private Editor editor;
	private ListPicker<IAnim> animSel;
	private TabFocusHandler tabHandler;
	private Button prevFrm, nextFrm;

	public AnimPanel(IGui gui, EditorGui e) {
		super(gui);
		tabHandler = new TabFocusHandler(gui);
		editor = e.getEditor();
		setBounds(new Box(0, 0, 170, 330));
		setBackgroundColor(gui.getColors().panel_background);

		animSel = new ListPicker<>(e, new CombinedListView<IAnim>(Collections.singletonList(new IAnim() {
			@Override
			public String toString() {
				return gui.i18nFormat("label.cpm.no_animation");
			}

			@Override
			public String noAnim() {
				return "";
			}
		}), new ListView<>(editor.animations, v -> (IAnim) v)));
		animSel.setBounds(new Box(5, 5, 160, 20));
		animSel.setListLoader(l -> {
			l.setComparator(
					Comparator.comparing(IAnim::noAnim, Comparator.nullsLast(Comparator.naturalOrder())).
					thenComparing(Comparator.comparing(IAnim::getPose0, Comparator.nullsLast(Comparator.naturalOrder()))).
					thenComparing(Comparator.comparing(IAnim::getPose1, Comparator.nullsLast(Comparator.naturalOrder()))).
					thenComparing(Comparator.comparing(IAnim::toString))
					);
		});
		addElement(animSel);
		animSel.setAction(() -> {
			IAnim sel = animSel.getSelected();
			editor.selectedAnim = sel instanceof EditorAnim ? (EditorAnim) sel : null;
			editor.updateGui();
		});
		editor.setSelAnim.add(animSel::setSelected);

		ButtonIcon newAnimBtn = new ButtonIcon(gui, "editor", 0, 16, () -> e.openPopup(new AnimationSettinsPopup(gui, editor, false)));
		newAnimBtn.setBounds(new Box(5, 30, 18, 18));
		addElement(newAnimBtn);

		ButtonIcon delAnimBtn = new ButtonIcon(gui, "editor", 14, 16, new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnim, null));
		delAnimBtn.setBounds(new Box(25, 30, 18, 18));
		addElement(delAnimBtn);
		editor.setAnimDelEn.add(delAnimBtn::setEnabled);

		Button editBtn = new Button(gui, gui.i18nFormat("button.cpm.edit"), () -> e.openPopup(new AnimationSettinsPopup(gui, editor, true)));
		editBtn.setBounds(new Box(45, 30, 80, 18));
		addElement(editBtn);
		editor.setAnimDelEn.add(editBtn::setEnabled);

		Label currFrame = new Label(gui, "");
		currFrame.setBounds(new Box(30, 65, 110, 10));
		addElement(currFrame);

		prevFrm = new Button(gui, "<", () -> {
			if(gui.isCtrlDown())editor.animMoveFrame(-1);
			else editor.animPrevFrm();
		});
		prevFrm.setBounds(new Box(5, 60, 18, 18));
		addElement(prevFrm);

		nextFrm = new Button(gui, ">", () -> {
			if(gui.isCtrlDown())editor.animMoveFrame(1);
			else editor.animNextFrm();
		});
		nextFrm.setBounds(new Box(145, 60, 18, 18));
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

		ButtonIcon delFrmBtn = new ButtonIcon(gui, "editor", 14, 16, new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnimFrame, null));
		delFrmBtn.setBounds(new Box(25, 85, 18, 18));
		addElement(delFrmBtn);
		editor.setFrameDelEn.add(delFrmBtn::setEnabled);

		ButtonIcon playBtn = new ButtonIcon(gui, "editor", 56, 16, () -> {
			editor.playFullAnim = !editor.playFullAnim;
			editor.playStartTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
			editor.setAnimPlay.accept(editor.playFullAnim);
		});
		playBtn.setBounds(new Box(45, 85, 18, 18));
		addElement(playBtn);
		editor.setAnimPlayEn.add(playBtn::setEnabled);
		editor.setAnimPlay.add(v -> playBtn.setU(v ? 72 : 56));

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
		tabHandler.add(duration);

		PosPanel.addVec3("rotation", 145, v -> editor.setAnimRot(v), this, editor.setAnimRot, 1, tabHandler);
		PosPanel.addVec3("position", 175, v -> editor.setAnimPos(v), this, editor.setAnimPos, 2, tabHandler);

		ColorButton colorBtn = new ColorButton(gui, e, editor::setAnimColor);
		colorBtn.setBounds(new Box(5, 205, 140, 20));
		editor.setAnimColor.add(c -> {
			colorBtn.setEnabled(c != null);
			if(c != null)colorBtn.setColor(c);
			else colorBtn.setColor(0);
		});
		addElement(colorBtn);

		Checkbox boxShow = new Checkbox(gui, gui.i18nFormat("label.cpm.visible"));
		boxShow.setBounds(new Box(5, 230, 60, 18));
		boxShow.setAction(editor::switchAnimShow);
		editor.setAnimShow.add(boxShow::updateState);
		addElement(boxShow);

		Button encSettings = new Button(gui, gui.i18nFormat("button.cpm.animEncSettings"), () -> e.openPopup(new AnimEncConfigPopup(gui, editor, null)));
		encSettings.setBounds(new Box(5, 260, 155, 20));
		addElement(encSettings);

		Label lblPri = new Label(gui, gui.i18nFormat("label.cpm.anim_priority"));
		lblPri.setBounds(new Box(5, 290, 160, 10));
		lblPri.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.anim_priority")));
		addElement(lblPri);

		Button clearAnimData = new Button(gui, gui.i18nFormat("button.cpm.clearAnimData"), new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnimPartData, null));
		clearAnimData.setBounds(new Box(110, 120, 55, 18));
		clearAnimData.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.clearAnimData")));
		addElement(clearAnimData);

		Spinner animPriority = new Spinner(gui);
		editor.setAnimPriority.add(v -> {
			animPriority.setEnabled(v != null);
			if(v != null)animPriority.setValue(v);
			else animPriority.setValue(0);
		});
		animPriority.setBounds(new Box(5, 300, 100, 18));
		animPriority.setDp(0);
		animPriority.addChangeListener(() -> editor.setAnimPriority((int) animPriority.getValue()));
		addElement(animPriority);
		tabHandler.add(animPriority);
	}

	public static interface IAnim {
		default IPose getPose() {
			return null;
		}

		default VanillaPose getPose0() {
			IPose pose = getPose();
			if(pose == null || !(pose instanceof VanillaPose))
				return null;
			return (VanillaPose) pose;
		}

		default String getPose1() {
			IPose pose = getPose();
			if(pose == null || !(pose instanceof CustomPose))
				return null;
			return ((CustomPose) pose).getName();
		}

		default String noAnim() {
			return null;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible) {
			editor.playFullAnim = false;
			IAnim sel = animSel.getSelected();
			editor.selectedAnim = sel instanceof EditorAnim ? (EditorAnim) sel : null;
			editor.setAnimPlay.accept(false);
			editor.updateGui();
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if(gui.isCtrlDown()) {
			prevFrm.setText("<<");
			nextFrm.setText(">>");
		} else {
			prevFrm.setText("<");
			nextFrm.setText(">");
		}
		super.draw(event, partialTicks);
	}
}
