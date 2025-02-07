package com.tom.cpm.shared.editor.gui.popup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.ListPicker;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimationDisplayData;
import com.tom.cpm.shared.editor.anim.AnimationDisplayData.Type;
import com.tom.cpm.shared.editor.anim.AnimationProperties;

public class AnimationSettingsPopup extends PopupPanel {
	private final Editor editor;
	private String title;

	public AnimationSettingsPopup(IGui gui, Editor editor, boolean edit) {
		super(gui);
		this.editor = editor;
		setBounds(new Box(0, 0, 200, 195));

		FlowLayout layout = new FlowLayout(this, 4, 1);

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
		for(AnimationType t : AnimationType.VALUES) {
			if(t.isCustom()) {
				AnimType at = new AnimType(t);
				ats.add(at);
				if(edit && editor.selectedAnim != null && sel == null && editor.selectedAnim.type == t) {
					sel = at;
				}
			}
		}

		ListPicker<AnimType> typeDd = new ListPicker<>(gui.getFrame(), ats);
		typeDd.setListLoader(l -> {
			l.setComparator(Comparator.comparing(AnimType::getType).thenComparing(Comparator.comparing(AnimType::toString)));
			l.setRenderer(AnimType::draw);
			l.setGetTooltip(AnimType::getTooltip);
		});
		typeDd.setBounds(new Box(5, 0, 190, 20));
		this.addElement(typeDd);
		if(sel != null)typeDd.setSelected(sel);

		Checkbox boxAdd = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_additive"));
		if(edit && editor.selectedAnim != null)boxAdd.setSelected(editor.selectedAnim.add);
		else boxAdd.setSelected(true);
		boxAdd.setBounds(new Box(5, 0, 60, 20));
		this.addElement(boxAdd);
		boxAdd.setAction(() -> boxAdd.setSelected(!boxAdd.isSelected()));

		Checkbox boxLoop = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_loop"));
		if(edit && editor.selectedAnim != null)boxLoop.setSelected(editor.selectedAnim.loop);
		boxLoop.setBounds(new Box(5, 0, 60, 20));
		this.addElement(boxLoop);

		this.addElement(new Label(gui, gui.i18nFormat("label.cpm.name")).setBounds(new Box(5, 0, 190, 10)));

		Panel nameFieldPanel = new Panel(gui);
		nameFieldPanel.setBounds(new Box(5, 0, 190, 20));
		this.addElement(nameFieldPanel);

		TextField nameField = new TextField(gui);
		if(edit && editor.selectedAnim != null)nameField.setText(editor.selectedAnim.getDisplayName());
		nameField.setBounds(new Box(0, 0, 190, 20));
		nameFieldPanel.addElement(nameField);

		List<AnimSel> selAnims = new ArrayList<>();
		editor.animations.stream().map(a -> a.pose).
		filter(p -> p != null && p != VanillaPose.CUSTOM && p != VanillaPose.GLOBAL).
		distinct().map(AnimSel::new).forEach(selAnims::add);

		editor.animations.stream().
		filter(a -> a.pose == null && !a.type.isStaged() && (a.type != AnimationType.GESTURE || a.loop) && a.type != AnimationType.VALUE_LAYER).
		map(a -> new AnimSel(a.getId(), a.getDisplayGroup(), a.type)).distinct().forEach(selAnims::add);

		ListPicker<AnimSel> dropDownAnimSel = new ListPicker<>(gui.getFrame(), selAnims);
		dropDownAnimSel.setBounds(new Box(0, 0, 190, 20));
		dropDownAnimSel.setVisible(false);
		nameFieldPanel.addElement(dropDownAnimSel);
		dropDownAnimSel.setListLoader(l -> {
			l.setRenderer(AnimSel::draw);
		});

		if(edit && editor.selectedAnim != null && editor.selectedAnim.type.isStaged()) {
			nameField.setVisible(false);
			dropDownAnimSel.setVisible(true);

			String[] nm = editor.selectedAnim.displayName.split(":", 2);
			if(nm.length == 2) {
				Predicate<AnimSel> search = null;
				switch (nm[0]) {
				case "p":
					for(VanillaPose p : VanillaPose.VALUES) {
						if(nm[1].equals(p.name().toLowerCase(Locale.ROOT))) {
							search = a -> a.pose == p;
							break;
						}
					}
					break;

				case "c":
					search = a -> a.pose instanceof CustomPose && ((CustomPose)a.pose).getName().equals(nm[1]);
					break;

				case "g":
					search = a -> a.gesture != null && a.gesture.equals(nm[1]);
					break;

				default:
					break;
				}

				if(search != null) {
					AnimSel s = selAnims.stream().filter(search).findFirst().orElse(null);
					if(s != null)dropDownAnimSel.setSelected(s);
				}
			}
		}

		addElement(new Label(gui, gui.i18nFormat("label.cpm.animIntType")).setBounds(new Box(5, 0, 190, 10)));
		NameMapper<InterpolatorType> intMap = new NameMapper<>(InterpolatorType.VALUES, e -> gui.i18nFormat("label.cpm.animIntType." + e.name().toLowerCase(Locale.ROOT)));
		DropDownBox<NamedElement<InterpolatorType>> intBox = new DropDownBox<>(gui.getFrame(), intMap.asList());
		intMap.setSetter(intBox::setSelected);
		if(edit && editor.selectedAnim != null) {
			intMap.setValue(editor.selectedAnim.intType);
		} else {
			intMap.setValue(InterpolatorType.POLY_LOOP);
		}
		intBox.setBounds(new Box(5, 0, 190, 20));
		addElement(intBox);

		Checkbox boxCommand = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_command"));

		Checkbox boxLayerCtrl = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_layerCtrl"));

		Checkbox boxMustFinish = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_mustFinish"));

		Checkbox boxHidden = new Checkbox(gui, gui.i18nFormat("label.cpm.anim_hidden"));

		Runnable r = () -> {
			AnimType at = typeDd.getSelected();
			boxLoop.setEnabled(at.canLoop());
			boxCommand.setEnabled(at.option.isCustom() && !at.option.isStaged());
			boxLayerCtrl.setEnabled(at.option == AnimationType.CUSTOM_POSE || at.option == AnimationType.GESTURE);
			if(!edit)intMap.setValue(intBox.getSelected().getElem().getAlt(at.useLooping()));
			boolean st = at.option.isStaged();
			nameField.setVisible(!st);
			dropDownAnimSel.setVisible(st);
			boxMustFinish.setEnabled(!st);
			boxHidden.setEnabled(at.option.isCustom() && !st);
		};
		typeDd.setAction(r);
		r.run();

		boxLoop.setAction(() -> {
			boolean s = !boxLoop.isSelected();
			if(!edit)intMap.setValue(intBox.getSelected().getElem().getAlt(s));
			boxLoop.setSelected(s);
		});

		if(edit && editor.selectedAnim != null)boxCommand.setSelected(editor.selectedAnim.command);
		boxCommand.setBounds(new Box(5, 0, 190, 20));
		boxCommand.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.anim_command")));
		boxCommand.setAction(() -> {
			boxCommand.setSelected(!boxCommand.isSelected());
		});
		this.addElement(boxCommand);

		if(edit && editor.selectedAnim != null)boxLayerCtrl.setSelected(editor.selectedAnim.layerControlled);
		else boxLayerCtrl.setSelected(true);
		boxLayerCtrl.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.anim_layerCtrl")));
		boxLayerCtrl.setBounds(new Box(5, 0, 190, 20));
		boxLayerCtrl.setAction(() -> {
			boxLayerCtrl.setSelected(!boxLayerCtrl.isSelected());
		});
		this.addElement(boxLayerCtrl);

		if(edit && editor.selectedAnim != null)boxMustFinish.setSelected(editor.selectedAnim.mustFinish);
		boxMustFinish.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.anim_mustFinish")));
		boxMustFinish.setBounds(new Box(5, 0, 190, 20));
		boxMustFinish.setAction(() -> {
			boxMustFinish.setSelected(!boxMustFinish.isSelected());
		});
		this.addElement(boxMustFinish);

		if(edit && editor.selectedAnim != null)boxHidden.setSelected(editor.selectedAnim.hidden);
		boxHidden.setTooltip(new Tooltip(gui.getFrame(), gui.i18nFormat("tooltip.cpm.anim_hidden")));
		boxHidden.setBounds(new Box(5, 0, 190, 20));
		boxHidden.setAction(() -> {
			boxHidden.setSelected(!boxHidden.isSelected());
		});
		this.addElement(boxHidden);

		Button okBtn = new Button(gui, gui.i18nFormat("button.cpm.ok"), () -> {
			String name = nameField.getText();
			AnimType at = typeDd.getSelected();
			IPose pose = at.option == AnimationType.CUSTOM_POSE ? new CustomPose(name, 0) : at.pose;
			if(at.option.isStaged()) {
				AnimSel s = dropDownAnimSel.getSelected();
				if(s == null)return;
				name = s.getName();
			}
			AnimationProperties pr = new AnimationProperties(pose, name, at.option, boxAdd.isSelected(), at.canLoop() && boxLoop.isSelected(), intBox.getSelected().getElem(), boxCommand.isSelected(), boxLayerCtrl.isSelected(), boxMustFinish.isSelected(), boxHidden.isSelected());
			if(edit) {
				editor.editAnim(pr);
			} else {
				editor.addNewAnim(pr);
			}
			this.close();
		});
		okBtn.setBounds(new Box(80, 0, 40, 20));
		this.addElement(okBtn);

		layout.reflow();

		title = gui.i18nFormat("label.cpm.animationSettings." + (edit ? "edit" : "new"));
	}

	@Override
	public String getTitle() {
		return title;
	}

	private class AnimType {
		private VanillaPose pose;
		private AnimationDisplayData display;
		private final AnimationType option;
		public AnimType(VanillaPose pose) {
			this.option = AnimationType.POSE;
			this.pose = pose;
			this.display = AnimationDisplayData.getFor(pose);
		}

		public AnimType(AnimationType option) {
			this.option = option;
		}

		@Override
		public String toString() {
			if(pose != null)return gui.i18nFormat("label.cpm.anim_pose", pose.getName(gui, null));
			return gui.i18nFormat("label.cpm.new_anim_" + option.name().toLowerCase(Locale.ROOT));
		}

		private AnimationDisplayData.Type getType() {
			return display == null ? Type.CUSTOM : display.type;
		}

		private Tooltip getTooltip() {
			String tooltip = gui.i18nFormat("tooltip.cpm.animType.group." + getType().name().toLowerCase(Locale.ROOT));
			String tip = "tooltip.cpm.animType.pose." + (pose != null ? pose.name().toLowerCase(Locale.ROOT) : "opt_" + option.name().toLowerCase(Locale.ROOT));
			String desc = gui.i18nFormat(tip);
			String name = toString();
			String fullTip = name + "\\" + tooltip;
			if(!tip.equals(desc))fullTip = fullTip + "\\" + desc;
			return new Tooltip(gui.getFrame(), fullTip);
		}

		private void draw(int x, int y, int w, int h, boolean hovered, boolean selected) {
			int bg = gui.getColors().select_background;
			if(hovered)bg = gui.getColors().popup_background;
			if(selected || hovered)gui.drawBox(x, y, w, h, bg);
			gui.drawText(x + 3, y + h / 2 - 4, toString(), getType().color);
		}

		private boolean useLooping() {
			return option.isStaged() ? false : pose == null ? true : !pose.hasStateGetter();
		}

		private boolean canLoop() {
			return option.canLoop();
		}
	}

	private class AnimSel {
		private IPose pose;
		private String gesture, name;
		private AnimationType type;

		public AnimSel(IPose pose) {
			this.pose = pose;
		}

		public AnimSel(String gesture, String name, AnimationType type) {
			this.gesture = gesture;
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			if(pose != null)return editor.ui.i18nFormat("label.cpm.anim_pose", pose.getName(gui, null));
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gesture == null) ? 0 : gesture.hashCode());
			result = prime * result + ((pose == null) ? 0 : pose.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			AnimSel other = (AnimSel) obj;
			if (gesture == null) {
				if (other.gesture != null) return false;
			} else if (!gesture.equals(other.gesture)) return false;
			if (pose == null) {
				if (other.pose != null) return false;
			} else if (!pose.equals(other.pose)) return false;
			return true;
		}

		public String getName() {
			if(pose instanceof VanillaPose)return "p:" + ((VanillaPose)pose).name().toLowerCase(Locale.ROOT);
			else if(pose instanceof CustomPose)return "c:" + ((CustomPose)pose).getName();
			return "g:" + gesture;
		}

		private void draw(int x, int y, int w, int h, boolean hovered, boolean selected) {
			int bg = gui.getColors().select_background;
			if(hovered)bg = gui.getColors().popup_background;
			if(selected || hovered)gui.drawBox(x, y, w, h, bg);
			int anType = Type.GLOBAL.color;
			if (pose instanceof VanillaPose) {
				anType = AnimationDisplayData.getFor((VanillaPose) pose).type.color;
			} else if (pose != null) {
				anType = Type.CUSTOM.color;
			} else if (type != null) {
				switch (type) {
				case GESTURE:
					anType = 0x8888FF;
					break;
				case LAYER:
					anType = 0xFF88FF;
					break;
				case VALUE_LAYER:
					anType = 0x88FFFF;
					break;
				default:
					anType = Type.CUSTOM.color;
					break;
				}
			}
			gui.drawText(x + 3, y + h / 2 - 4, toString(), anType);
		}
	}
}
