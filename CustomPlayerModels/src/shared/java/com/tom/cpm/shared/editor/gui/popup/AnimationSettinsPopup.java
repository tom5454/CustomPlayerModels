package com.tom.cpm.shared.editor.gui.popup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.ListPicker;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.Gesture;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimationDisplayData;
import com.tom.cpm.shared.editor.anim.AnimationDisplayData.Type;

public class AnimationSettinsPopup extends PopupPanel {
	private final Editor editor;
	private String title;

	public AnimationSettinsPopup(IGui gui, Editor editor, boolean edit) {
		super(gui);
		this.editor = editor;
		setBounds(new Box(0, 0, 200, 170));

		AnimType sel = null;
		List<AnimType> ats = new ArrayList<>();
		for (VanillaPose p : VanillaPose.VALUES) {
			if(p == VanillaPose.CUSTOM)continue;
			AnimType type = new AnimType(p);
			ats.add(type);
			if(edit && editor.selectedAnim != null) {
				if(p == editor.selectedAnim.pose) {
					sel = type;
				}
			}
		}
		AnimType typePose = new AnimType("pose", false);
		ats.add(typePose);
		AnimType typeGesture = new AnimType("gesture", true);
		ats.add(typeGesture);
		AnimType typeLayer = new AnimType("layer", false);
		ats.add(typeLayer);
		AnimType typeValue = new AnimType("value", false);
		ats.add(typeValue);
		if(edit && editor.selectedAnim != null && sel == null) {
			if(editor.selectedAnim.pose == null) {
				if(editor.selectedAnim.displayName.startsWith(Gesture.LAYER_PREFIX))
					sel = typeLayer;
				else if(editor.selectedAnim.displayName.startsWith(Gesture.VALUE_LAYER_PREFIX))
					sel = typeValue;
				else
					sel = typeGesture;
			} else sel = typePose;
		}

		ListPicker<AnimType> typeDd = new ListPicker<>(editor.frame, ats);
		typeDd.setListLoader(l -> {
			l.setComparator(Comparator.comparing(AnimType::getType).thenComparing(Comparator.comparing(AnimType::toString)));
			l.setRenderer(AnimType::draw);
			l.setGetTooltip(AnimType::getTooltip);
		});
		typeDd.setBounds(new Box(5, 5, 190, 20));
		this.addElement(typeDd);
		if(sel != null)typeDd.setSelected(sel);

		Checkbox boxAdd = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_additive"));
		if(edit && editor.selectedAnim != null)boxAdd.setSelected(editor.selectedAnim.add);
		else boxAdd.setSelected(true);
		boxAdd.setBounds(new Box(5, 30, 60, 18));
		this.addElement(boxAdd);
		boxAdd.setAction(() -> boxAdd.setSelected(!boxAdd.isSelected()));

		Checkbox boxLoop = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_loop"));
		if(edit && editor.selectedAnim != null)boxLoop.setSelected(editor.selectedAnim.loop);
		boxLoop.setBounds(new Box(5, 50, 60, 18));
		this.addElement(boxLoop);

		this.addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 70, 0, 0)));

		TextField nameField = new TextField(gui);
		if(edit && editor.selectedAnim != null)nameField.setText(editor.selectedAnim.getDisplayName());
		nameField.setBounds(new Box(5, 80, 190, 20));
		this.addElement(nameField);

		addElement(new Label(gui, gui.i18nFormat("label.cpm.animIntType")).setBounds(new Box(5, 105, 190, 10)));
		NameMapper<InterpolatorType> intMap = new NameMapper<>(InterpolatorType.VALUES, e -> gui.i18nFormat("label.cpm.animIntType." + e.name().toLowerCase()));
		DropDownBox<NamedElement<InterpolatorType>> intBox = new DropDownBox<>(editor.frame, intMap.asList());
		intMap.setSetter(intBox::setSelected);
		if(edit && editor.selectedAnim != null) {
			intMap.setValue(editor.selectedAnim.intType);
		} else {
			intMap.setValue(InterpolatorType.POLY_LOOP);
		}
		intBox.setBounds(new Box(5, 115, 190, 20));
		addElement(intBox);

		Runnable r = () -> {
			AnimType at = typeDd.getSelected();
			boxLoop.setEnabled(at.loop);
			if(!edit)intMap.setValue(intBox.getSelected().getElem().getAlt(at.useLooping()));
		};
		typeDd.setAction(r);
		r.run();

		boxLoop.setAction(() -> {
			boolean s = !boxLoop.isSelected();
			if(!edit)intMap.setValue(intBox.getSelected().getElem().getAlt(s));
			boxLoop.setSelected(s);
		});

		Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			String name = nameField.getText();
			AnimType at = typeDd.getSelected();
			IPose pose = at.pose == null && at.option.equals("pose") ? new CustomPose(name) : at.pose;
			if(at.pose == null) {
				if(at.option.equals("layer")) {
					name = Gesture.LAYER_PREFIX + name;
				} else if(at.option.equals("value")) {
					name = Gesture.VALUE_LAYER_PREFIX + name;
				}
			}
			if(edit) {
				editor.editAnim(pose, name, boxAdd.isSelected(), at.loop && boxLoop.isSelected(), intBox.getSelected().getElem());
			} else {
				editor.addNewAnim(pose, name, boxAdd.isSelected(), at.loop && boxLoop.isSelected(), intBox.getSelected().getElem());
			}
			this.close();
		});
		okBtn.setBounds(new Box(80, 140, 40, 20));
		this.addElement(okBtn);

		title = gui.i18nFormat("label.cpm.animationSettings." + (edit ? "edit" : "new"));
	}

	@Override
	public String getTitle() {
		return title;
	}

	private class AnimType {
		private VanillaPose pose;
		private AnimationDisplayData display;
		private String option;
		private boolean loop;
		public AnimType(VanillaPose pose) {
			this.pose = pose;
			this.display = AnimationDisplayData.getFor(pose);
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

		private AnimationDisplayData.Type getType() {
			return display == null ? Type.CUSTOM : display.type;
		}

		private Tooltip getTooltip() {
			String tooltip = gui.i18nFormat("tooltip.cpm.animType.group." + getType().name().toLowerCase());
			String tip = "tooltip.cpm.animType.pose." + (pose != null ? pose.name().toLowerCase() : "opt_" + option);
			String desc = gui.i18nFormat(tip);
			String name = toString();
			String fullTip = name + "\\" + tooltip;
			if(!tip.equals(desc))fullTip = fullTip + "\\" + desc;
			return new Tooltip(editor.frame, fullTip);
		}

		private void draw(int x, int y, int w, int h, boolean hovered, boolean selected) {
			int bg = gui.getColors().select_background;
			if(hovered)bg = gui.getColors().popup_background;
			if(selected || hovered)gui.drawBox(x, y, w, h, bg);
			gui.drawText(x + 3, y + h / 2 - 4, toString(), getType().color);
		}

		private boolean useLooping() {
			return pose == null ? true : !pose.hasStateGetter();
		}
	}
}
