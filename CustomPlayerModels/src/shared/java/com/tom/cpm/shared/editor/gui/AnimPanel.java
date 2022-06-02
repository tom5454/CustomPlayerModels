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
import com.tom.cpl.gui.util.FlowLayout;
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

		FlowLayout layout = new FlowLayout(this, 4, 1);

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

		Panel p = new Panel(gui);
		p.setBounds(new Box(0, 0, bounds.w, 18));
		addElement(p);

		ButtonIcon newAnimBtn = new ButtonIcon(gui, "editor", 0, 16, () -> e.openPopup(new AnimationSettinsPopup(gui, editor, false)));
		newAnimBtn.setBounds(new Box(5, 0, 18, 18));
		p.addElement(newAnimBtn);

		ButtonIcon delAnimBtn = new ButtonIcon(gui, "editor", 14, 16, new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnim, null));
		delAnimBtn.setBounds(new Box(25, 0, 18, 18));
		p.addElement(delAnimBtn);
		editor.setAnimDelEn.add(delAnimBtn::setEnabled);

		Button editBtn = new Button(gui, gui.i18nFormat("button.cpm.edit"), () -> e.openPopup(new AnimationSettinsPopup(gui, editor, true)));
		editBtn.setBounds(new Box(45, 0, 80, 18));
		p.addElement(editBtn);
		editor.setAnimDelEn.add(editBtn::setEnabled);

		p = new Panel(gui);
		p.setBounds(new Box(0, 0, bounds.w, 18));
		addElement(p);

		Label currFrame = new Label(gui, "");
		currFrame.setBounds(new Box(30, 5, 110, 10));
		p.addElement(currFrame);

		prevFrm = new Button(gui, "<", () -> {
			if(gui.isCtrlDown())editor.animMoveFrame(-1);
			else editor.animPrevFrm();
		});
		prevFrm.setBounds(new Box(5, 0, 18, 18));
		p.addElement(prevFrm);

		nextFrm = new Button(gui, ">", () -> {
			if(gui.isCtrlDown())editor.animMoveFrame(1);
			else editor.animNextFrm();
		});
		nextFrm.setBounds(new Box(145, 0, 18, 18));
		p.addElement(nextFrm);

		editor.setAnimFrame.add(i -> {
			prevFrm.setEnabled(i != null);
			nextFrm.setEnabled(i != null);
			if(i != null) {
				currFrame.setText(gui.i18nFormat("label.cpm.anim_frame_x", i));
			} else {
				currFrame.setText(gui.i18nFormat("label.cpm.anim_frame_none"));
			}
		});

		p = new Panel(gui);
		p.setBounds(new Box(0, 0, bounds.w, 18));
		addElement(p);

		ButtonIcon newFrmBtn = new ButtonIcon(gui, "editor", 0, 16, editor::addNewAnimFrame);
		newFrmBtn.setBounds(new Box(5, 0, 18, 18));
		p.addElement(newFrmBtn);
		editor.setFrameAddEn.add(newFrmBtn::setEnabled);

		ButtonIcon delFrmBtn = new ButtonIcon(gui, "editor", 14, 16, new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnimFrame, null));
		delFrmBtn.setBounds(new Box(25, 0, 18, 18));
		p.addElement(delFrmBtn);
		editor.setFrameDelEn.add(delFrmBtn::setEnabled);

		ButtonIcon playBtn = new ButtonIcon(gui, "editor", 56, 16, true, () -> {
			editor.playFullAnim = !editor.playFullAnim;
			editor.playStartTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
			editor.setAnimPlay.accept(editor.playFullAnim);
		});
		playBtn.setBounds(new Box(45, 0, 18, 18));
		p.addElement(playBtn);
		editor.setAnimPlayEn.add(playBtn::setEnabled);
		editor.setAnimPlay.add(v -> playBtn.setU(v ? 72 : 56));

		p = new Panel(gui);
		p.setBounds(new Box(0, 0, bounds.w, 28));
		addElement(p);

		Label lblDuration = new Label(gui, gui.i18nFormat("label.cpm.duration"));
		lblDuration.setBounds(new Box(5, 0, 0, 0));
		p.addElement(lblDuration);

		Spinner duration = new Spinner(gui);
		duration.setDp(0);
		duration.setBounds(new Box(5, 10, 100, 18));
		p.addElement(duration);
		editor.setAnimDuration.add(i -> {
			duration.setEnabled(i != null);
			if(i != null)duration.setValue(i);
			else duration.setValue(1000);
		});
		duration.addChangeListener(() -> editor.setAnimDuration((int) duration.getValue()));
		tabHandler.add(duration);

		Button clearAnimData = new Button(gui, gui.i18nFormat("button.cpm.clearAnimData"), new ConfirmPopup(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnimPartData, null));
		clearAnimData.setBounds(new Box(110, 10, 55, 18));
		clearAnimData.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.clearAnimData")));
		p.addElement(clearAnimData);

		PosPanel.addVec3("rotation", v -> editor.setAnimRot(v), this, editor.setAnimRot, 1, tabHandler);
		PosPanel.addVec3("position", v -> editor.setAnimPos(v), this, editor.setAnimPos, 2, tabHandler);
		Panel sc = PosPanel.addVec3("render_scale", v -> editor.setAnimScale(v), this, editor.setAnimScale, 2, tabHandler);
		editor.updateGui.add(() -> {
			sc.setVisible(editor.displayAdvScaling);
			layout.reflow();
		});

		ColorButton colorBtn = new ColorButton(gui, e, editor::setAnimColor);
		colorBtn.setBounds(new Box(5, 0, 140, 20));
		editor.setAnimColor.add(c -> {
			colorBtn.setEnabled(c != null);
			if(c != null)colorBtn.setColor(c);
			else colorBtn.setColor(0);
		});
		addElement(colorBtn);

		Checkbox boxShow = new Checkbox(gui, gui.i18nFormat("label.cpm.visible"));
		boxShow.setBounds(new Box(5, 0, 60, 18));
		boxShow.setAction(editor::switchAnimShow);
		editor.setAnimShow.add(boxShow::updateState);
		addElement(boxShow);

		Button encSettings = new Button(gui, gui.i18nFormat("button.cpm.animEncSettings"), () -> e.openPopup(new AnimEncConfigPopup(gui, editor, null)));
		encSettings.setBounds(new Box(5, 0, 155, 20));
		addElement(encSettings);

		p = new Panel(gui);
		p.setBounds(new Box(0, 0, bounds.w, 28));
		addElement(p);

		Label lblPri = new Label(gui, gui.i18nFormat("label.cpm.anim_priority"));
		lblPri.setBounds(new Box(5, 0, 160, 10));
		lblPri.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.anim_priority")));
		p.addElement(lblPri);

		Spinner animPriority = new Spinner(gui);
		editor.setAnimPriority.add(v -> {
			animPriority.setEnabled(v != null);
			if(v != null)animPriority.setValue(v);
			else animPriority.setValue(0);
		});
		animPriority.setBounds(new Box(5, 10, 100, 18));
		animPriority.setDp(0);
		animPriority.addChangeListener(() -> editor.setAnimPriority((int) animPriority.getValue()));
		p.addElement(animPriority);
		tabHandler.add(animPriority);

		layout.reflow();
		addElement(tabHandler);
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
