package com.tom.cpm.shared.editor.gui;

import java.util.Collections;
import java.util.Comparator;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.KeybindHandler;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.ListPicker;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.CombinedListView;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimFrame.FrameData;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.gui.popup.AnimEncConfigPopup;
import com.tom.cpm.shared.editor.gui.popup.AnimationSettingsPopup;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;
import com.tom.cpm.shared.editor.gui.popup.LayerDefaultPopup;
import com.tom.cpm.shared.gui.Keybinds;

public class AnimPanel extends Panel {
	private Editor editor;
	private ListPicker<NamedElement<EditorAnim>> animSel;
	private TabFocusHandler tabHandler;
	private Button prevFrm, nextFrm, clearAnimData;
	private AnimFrame cpyFrame;
	private FrameData cpyData;

	public AnimPanel(IGui gui, EditorGui e) {
		super(gui);
		tabHandler = new TabFocusHandler(gui);
		editor = e.getEditor();
		setBounds(new Box(0, 0, 170, 330));
		setBackgroundColor(gui.getColors().panel_background);

		FlowLayout layout = new FlowLayout(this, 4, 1);

		NameMapper<EditorAnim> mapper = new NameMapper<>(new CombinedListView<>(Collections.singletonList((EditorAnim) null), editor.animations), ea -> {
			if(ea == null)return gui.i18nFormat("label.cpm.no_animation");
			else return ea.toString();
		});

		animSel = new ListPicker<>(e, mapper.asList());
		mapper.setSetter(animSel::setSelected);
		animSel.setBounds(new Box(5, 5, 160, 20));
		animSel.setListLoader(l -> {
			l.setComparator(
					mapper.cmp(Comparator.comparing(a -> a == null ? null : "", Comparator.nullsFirst(Comparator.naturalOrder()))).
					thenComparing(mapper.cmp(Comparator.comparing(AnimPanel::getPose0, Comparator.nullsLast(Comparator.naturalOrder())))).
					thenComparing(mapper.cmp(Comparator.comparing(AnimPanel::getPose1, Comparator.nullsLast(Comparator.naturalOrder())))).
					thenComparing(Comparator.comparing(NamedElement::toString))
					);
		});
		addElement(animSel);
		animSel.setAction(() -> {
			editor.selectedAnim = animSel.getSelected().getElem();
			editor.updateGui();
		});
		editor.setSelAnim.add(v -> {
			mapper.refreshValues();
			mapper.setValue(v);
		});

		Panel p = new Panel(gui);
		p.setBounds(new Box(0, 0, bounds.w, 18));
		addElement(p);

		ButtonIcon newAnimBtn = new ButtonIcon(gui, "editor", 0, 16, () -> e.openPopup(new AnimationSettingsPopup(gui, editor, false)));
		newAnimBtn.setBounds(new Box(5, 0, 18, 18));
		p.addElement(newAnimBtn);

		ButtonIcon delAnimBtn = new ButtonIcon(gui, "editor", 14, 16, ConfirmPopup.confirmHandler(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnim));
		delAnimBtn.setBounds(new Box(25, 0, 18, 18));
		p.addElement(delAnimBtn);
		editor.setAnimDelEn.add(delAnimBtn::setEnabled);

		Button editBtn = new Button(gui, gui.i18nFormat("button.cpm.edit"), () -> e.openPopup(new AnimationSettingsPopup(gui, editor, true)));
		editBtn.setBounds(new Box(45, 0, 80, 18));
		p.addElement(editBtn);
		editor.setAnimDelEn.add(editBtn::setEnabled);

		Button optBtn = new Button(gui, "...", null) {

			@Override
			public void mouseClick(MouseEvent evt) {
				if(evt.isHovered(bounds)) {
					Vec2i p = evt.getPos();
					PopupMenu animPopup = new PopupMenu(gui, e);
					if(editor.selectedAnim != null) {
						animPopup.addButton(gui.i18nFormat("button.cpm.dupAnim"), () -> {
							EditorAnim cpy = new EditorAnim(editor.selectedAnim);
							editor.action("add", "action.cpm.anim").addToList(editor.animations, cpy).onUndo(() -> editor.selectedAnim = null).execute();
							editor.selectedAnim = cpy;
							editor.updateGui();
						});
						AnimPanel this0 = AnimPanel.this;
						animPopup.addButton(gui.i18nFormat("buttom.cpm.copyAnimFrame"), this0::copyFrame).setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.anim.copyFrame", Keybinds.COPY_ANIM_FRAME.getSetKey(gui))));
						if(cpyFrame != null) {
							animPopup.addButton(gui.i18nFormat("button.cpm.pasteAnimFrame"), this0::pasteFrame).setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.anim.pasteFrame", Keybinds.PASTE_ANIM_FRAME.getSetKey(gui))));
						}
						if(editor.getSelectedElement() != null) {
							animPopup.addButton(gui.i18nFormat("buttom.cpm.copyAnimFrameData"), this0::copyData).setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.anim.copyData", Keybinds.COPY_ANIM_PART.getSetKey(gui))));
							if(cpyData != null) {
								animPopup.addButton(gui.i18nFormat("button.cpm.pasteAnimFrameData"), this0::pasteData).setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.anim.pasteData", Keybinds.PASTE_ANIM_PART.getSetKey(gui))));
							}
						}
					}
					if(animPopup.getY() != 0)
						animPopup.display(p.x - evt.x + bounds.x / 2, p.y - evt.y + bounds.h + bounds.y, 160);
					evt.consume();
				}
			}
		};
		optBtn.setBounds(new Box(130, 0, 30, 18));
		editor.setAnimDelEn.add(optBtn::setEnabled);
		p.addElement(optBtn);

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

		ButtonIcon delFrmBtn = new ButtonIcon(gui, "editor", 14, 16, ConfirmPopup.confirmHandler(e, gui.i18nFormat("label.cpm.confirmDel"), editor::delSelectedAnimFrame));
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

		clearAnimData = new Button(gui, gui.i18nFormat("button.cpm.clearAnimData"), () -> {
			boolean all = gui.isCtrlDown();
			ConfirmPopup.confirm(e, gui.i18nFormat("label.cpm.confirmDel"), () -> editor.delSelectedAnimPartData(all));
		});
		clearAnimData.setBounds(new Box(110, 10, 55, 18));
		clearAnimData.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.clearAnimData")));
		p.addElement(clearAnimData);

		PosPanel.addVec3("rotation", v -> editor.setAnimRot(v), this, editor.setAnimRot, 1, tabHandler);
		PosPanel.addVec3("position", v -> editor.setAnimPos(v), this, editor.setAnimPos, 2, tabHandler);
		PosPanel.addVec3("render_scale", v -> editor.setAnimScale(v), this, editor.setAnimScale, 2, tabHandler);
		editor.updateGui.add(() -> {
			clearAnimData.setEnabled(editor.getSelectedElement() != null && editor.selectedAnim != null && editor.selectedAnim.getSelectedFrame() != null);
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

		Button defLayerSettings = new Button(gui, gui.i18nFormat("button.cpm.defLayerSettings"), () -> {
			if(editor.selectedAnim != null) {
				if(editor.selectedAnim.type == AnimationType.LAYER) {
					e.openPopup(new LayerDefaultPopup.Toggle(gui, editor));
				} else if(editor.selectedAnim.type == AnimationType.VALUE_LAYER) {
					e.openPopup(new LayerDefaultPopup.Value(gui, editor));
				}
			}
		});
		defLayerSettings.setBounds(new Box(5, 0, 155, 20));
		addElement(defLayerSettings);
		editor.updateGui.add(() -> {
			defLayerSettings.setEnabled(editor.selectedAnim != null && editor.selectedAnim.isLayer());
		});

		p = new Panel(gui);
		p.setBounds(new Box(0, 0, bounds.w, 28));
		addElement(p);

		Label lblOrder = new Label(gui, gui.i18nFormat("label.cpm.anim_order"));
		lblOrder.setBounds(new Box(5, 0, 160, 10));
		lblOrder.setTooltip(new Tooltip(e, gui.i18nFormat("tooltip.cpm.anim_order")));
		p.addElement(lblOrder);

		Spinner animOrder = new Spinner(gui);
		editor.setAnimOrder.add(v -> {
			animOrder.setEnabled(v != null);
			if(v != null)animOrder.setValue(v);
			else animOrder.setValue(0);
		});
		animOrder.setBounds(new Box(5, 10, 100, 18));
		animOrder.setDp(0);
		animOrder.addChangeListener(() -> editor.setAnimOrder((int) animOrder.getValue()));
		p.addElement(animOrder);
		tabHandler.add(animOrder);
		editor.updateGui.add(() -> {
			animOrder.setEnabled(editor.selectedAnim != null && editor.selectedAnim.isCustom() && !editor.selectedAnim.type.isStaged());
		});

		layout.reflow();
		addElement(tabHandler);
	}

	private void copyFrame() {
		if(editor.selectedAnim != null) {
			cpyFrame = new AnimFrame(editor.selectedAnim.getSelectedFrame());
		}
	}

	private void pasteFrame() {
		if(editor.selectedAnim != null && cpyFrame != null) {
			editor.selectedAnim.addFrame(cpyFrame);
			cpyFrame = null;
			editor.updateGui();
		}
	}

	private void copyData() {
		ModelElement me = editor.getSelectedElement();
		if(editor.selectedAnim != null && me != null) {
			cpyData = editor.selectedAnim.getSelectedFrame().copy(me);
		}
	}

	private void pasteData() {
		ModelElement me = editor.getSelectedElement();
		if(editor.selectedAnim != null && cpyData != null && me != null) {
			ActionBuilder ab = editor.action("setAnim", "action.cpm.value");
			editor.selectedAnim.getSelectedFrame().importFrameData(ab, me, cpyData);
			ab.execute();
			cpyData = null;
			editor.updateGui();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible) {
			editor.playFullAnim = false;
			editor.selectedAnim = animSel.getSelected().getElem();
			editor.setAnimPlay.accept(false);
			editor.updateGui();
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		if(gui.isCtrlDown()) {
			prevFrm.setText("<<");
			nextFrm.setText(">>");
			clearAnimData.setText(gui.i18nFormat("button.cpm.clearAnimDataAll"));
		} else {
			prevFrm.setText("<");
			nextFrm.setText(">");
			clearAnimData.setText(gui.i18nFormat("button.cpm.clearAnimData"));
		}
		super.draw(event, partialTicks);
	}

	private static VanillaPose getPose0(EditorAnim ea) {
		if(ea == null)return null;
		IPose pose = ea.getPose();
		if(pose == null || !(pose instanceof VanillaPose))
			return null;
		return (VanillaPose) pose;
	}

	private static String getPose1(EditorAnim ea) {
		if(ea == null)return null;
		IPose pose = ea.getPose();
		if(pose == null || !(pose instanceof CustomPose))
			return null;
		return ((CustomPose) pose).getName();
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		KeybindHandler h = editor.frame.getKeybindHandler();
		h.registerKeybind(Keybinds.COPY_ANIM_FRAME, this::copyFrame);
		h.registerKeybind(Keybinds.PASTE_ANIM_FRAME, this::pasteFrame);
		h.registerKeybind(Keybinds.COPY_ANIM_PART, this::copyData);
		h.registerKeybind(Keybinds.PASTE_ANIM_PART, this::pasteData);
		super.keyPressed(event);
	}
}
