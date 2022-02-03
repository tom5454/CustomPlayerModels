package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.NamedElement;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimationDisplayData;
import com.tom.cpm.shared.editor.anim.AnimationDisplayData.Slot;
import com.tom.cpm.shared.editor.anim.AnimationDisplayData.Type;
import com.tom.cpm.shared.editor.anim.EditorAnim;

public class AnimTestPanel extends Panel {
	private Editor editor;
	private DropDownPanel<IPose> poseSel;
	private DropDownPanel<String> gestureSel;
	private DropDownPanel<VanillaPose> mainPose;
	private Slider progressSlider;
	private DropDownPanel<VanillaPose> otherHandLeft;
	private DropDownPanel<VanillaPose> otherHandRight;
	private List<DropDownPanel<VanillaPose>> poseBoxes;
	private Checkbox chbxLeftParrot, chbxRightParrot;

	public AnimTestPanel(IGui gui, EditorGui e) {
		super(gui);
		editor = e.getEditor();
		setBounds(new Box(0, 0, 170, 80));
		setBackgroundColor(gui.getColors().panel_background);
		FlowLayout layout = new FlowLayout(this, 5, 1);

		List<NamedElement<IPose>> poses = new ArrayList<>();
		poses.add(new NamedElement<IPose>(VanillaPose.STANDING, this::poseToString));
		List<NamedElement<String>> gestures = new ArrayList<>();
		gestures.add(new NamedElement<>(null, k -> gui.i18nFormat("label.cpm.no_gesture")));
		editor.updateGui.add(() -> {
			poses.clear();
			gestures.clear();
			gestures.add(new NamedElement<>(null, k -> gui.i18nFormat("label.cpm.no_gesture")));
			for (VanillaPose p : VanillaPose.VALUES) {
				if(p == VanillaPose.CUSTOM || p == VanillaPose.GLOBAL)continue;
				AnimationDisplayData d = AnimationDisplayData.getFor(p);
				if(d.type == Type.POSE || d.type == Type.POSE_SERVER)
					poses.add(new NamedElement<IPose>(p, this::poseToString));
			}
			Set<String> addedGestures = new HashSet<>();
			Set<String> addedPoses = new HashSet<>();
			editor.animations.forEach(a -> {
				if(a.isCustom()) {
					if(a.pose != null) {
						String name = ((CustomPose)a.pose).getId();
						if(!addedPoses.contains(name)) {
							addedPoses.add(name);
							poses.add(new NamedElement<>(a.pose, k -> name));
						}
					} else {
						if(!addedGestures.contains(a.getId())) {
							addedGestures.add(a.getId());
							gestures.add(new NamedElement<>(a.getId(), k -> k));
						}
					}
				}
			});
		});

		poseSel = createDropDown("label.cpm.pose", poses);

		gestureSel = createDropDown("label.cpm.gesture", gestures);
		gestureSel.dropDown.setAction(() -> editor.gestureStartTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime());

		Map<Slot, List<NamedElement<VanillaPose>>> slots = new TreeMap<>();
		for (VanillaPose p : VanillaPose.VALUES) {
			AnimationDisplayData d = AnimationDisplayData.getFor(p);
			if(d == null || d.layerSlot == null)continue;
			slots.computeIfAbsent(d.layerSlot, __ -> {
				List<NamedElement<VanillaPose>> l = new ArrayList<>();
				l.add(new NamedElement<VanillaPose>(null, ___ -> gui.i18nFormat("label.cpm.no_elements")));
				return l;
			}).add(new NamedElement<>(p, this::poseToString));
		}

		poseBoxes = new ArrayList<>();

		List<NamedElement<VanillaPose>> handItems = new ArrayList<>();
		handItems.addAll(slots.get(Slot.LEFT_HAND));
		slots.get(Slot.RIGHT_HAND).stream().filter(i -> i.getElem() != null).forEach(handItems::add);
		handItems.add(new NamedElement<>(VanillaPose.PUNCH_LEFT, this::poseToString));
		handItems.add(new NamedElement<>(VanillaPose.PUNCH_RIGHT, this::poseToString));
		handItems.sort(Comparator.comparing(NamedElement::toString));
		mainPose = createDropDown("label.cpm.animSlot.heldItem", handItems);
		mainPose.dropDown.setAction(() -> {
			VanillaPose p = mainPose.getSelected().getElem();
			AnimationDisplayData d = AnimationDisplayData.getFor(p);
			progressSlider.setVisible(p != null && p.hasStateGetter());
			otherHandLeft.setVisible(d != null && (d.item == null || !d.item.pose.isTwoHanded()) && d.slot == ItemSlot.RIGHT_HAND);
			otherHandRight.setVisible(d != null && (d.item == null || !d.item.pose.isTwoHanded()) && d.slot == ItemSlot.LEFT_HAND);
			layout.reflow();
		});
		poseBoxes.add(mainPose);

		progressSlider = new Slider(gui, gui.i18nFormat("label.cpm.animProgress", 0));
		progressSlider.setVisible(false);
		progressSlider.setBounds(new Box(5, 0, 160, 20));
		progressSlider.setValue(editor.animTestSlider);
		progressSlider.setAction(() -> {
			progressSlider.setText(gui.i18nFormat("label.cpm.animProgress", (int) (progressSlider.getValue() * 100)));
			editor.animTestSlider = progressSlider.getValue();
		});
		addElement(progressSlider);

		List<NamedElement<VanillaPose>> handItemsOtherLeft = slots.get(Slot.LEFT_HAND).stream().
				filter(i -> i.getElem() != null && !AnimationDisplayData.getFor(i.getElem()).item.pose.isTwoHanded()).
				collect(Collectors.toList());
		handItemsOtherLeft.add(0, new NamedElement<VanillaPose>(null, ___ -> gui.i18nFormat("label.cpm.no_elements")));
		List<NamedElement<VanillaPose>> handItemsOtherRight = slots.get(Slot.RIGHT_HAND).stream().
				filter(i -> i.getElem() != null && !AnimationDisplayData.getFor(i.getElem()).item.pose.isTwoHanded()).
				collect(Collectors.toList());
		handItemsOtherRight.add(0, new NamedElement<VanillaPose>(null, ___ -> gui.i18nFormat("label.cpm.no_elements")));

		otherHandLeft = createDropDown("label.cpm.animSlot.left_hand", handItemsOtherLeft);
		otherHandLeft.setVisible(false);
		poseBoxes.add(otherHandLeft);
		otherHandRight = createDropDown("label.cpm.animSlot.right_hand", handItemsOtherRight);
		otherHandRight.setVisible(false);
		poseBoxes.add(otherHandRight);

		slots.remove(Slot.LEFT_HAND);
		slots.remove(Slot.RIGHT_HAND);
		slots.remove(Slot.PARROTS);

		for (Entry<Slot, List<NamedElement<VanillaPose>>> entry : slots.entrySet()) {
			Slot key = entry.getKey();
			List<NamedElement<VanillaPose>> val = entry.getValue();

			DropDownPanel<VanillaPose> dropDown = createDropDown("label.cpm.animSlot." + key.name().toLowerCase(), val);
			poseBoxes.add(dropDown);
		}

		chbxLeftParrot = new Checkbox(gui, gui.i18nFormat("label.cpm.animSlot.leftParrot"));
		chbxLeftParrot.setBounds(new Box(5, 0, 160, 20));
		chbxLeftParrot.setAction(() -> chbxLeftParrot.setSelected(!chbxLeftParrot.isSelected()));
		addElement(chbxLeftParrot);

		chbxRightParrot = new Checkbox(gui, gui.i18nFormat("label.cpm.animSlot.rightParrot"));
		chbxRightParrot.setBounds(new Box(5, 0, 160, 20));
		chbxRightParrot.setAction(() -> chbxRightParrot.setSelected(!chbxRightParrot.isSelected()));
		addElement(chbxRightParrot);

		layout.reflow();

		editor.gestureFinished.add(() -> gestureSel.dropDown.setSelected(null));
	}

	private <T> DropDownPanel<T> createDropDown(String name, List<NamedElement<T>> val) {
		DropDownPanel<T> panel = new DropDownPanel<>(name, val);
		addElement(panel);
		return panel;
	}

	private class DropDownPanel<T> extends Panel {
		private DropDownBox<NamedElement<T>> dropDown;

		public DropDownPanel(String name, List<NamedElement<T>> val) {
			super(AnimTestPanel.this.getGui());
			setBounds(new Box(0, 0, 170, 30));
			addElement(new Label(gui, gui.i18nFormat(name)).setBounds(new Box(5, 2, 160, 10)));

			dropDown = new DropDownBox<>(editor.frame, val);
			dropDown.setBounds(new Box(5, 10, 160, 20));
			addElement(dropDown);
		}

		public NamedElement<T> getSelected() {
			return dropDown.getSelected();
		}
	}

	private String poseToString(IPose p) {
		return p.getName(gui, null);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible) {
			editor.playStartTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
			editor.playFullAnim = true;
			editor.selectedAnim = null;
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		super.draw(event, partialTicks);
		editor.animsToPlay.clear();
		NamedElement<IPose> pose = poseSel.getSelected();
		if(pose != null) {
			editor.poseToApply = pose.getElem();
			for (EditorAnim anim : editor.animations) {
				if(anim.pose != null && (anim.pose == pose.getElem() ||
						(pose.getElem() instanceof CustomPose && anim.pose instanceof CustomPose &&
								((CustomPose)pose.getElem()).getId().equals(((CustomPose)anim.pose).getId())
								))) {
					editor.animsToPlay.add(anim);
				} else if(anim.pose == VanillaPose.GLOBAL) {
					editor.animsToPlay.add(anim);
				}
			}
		}
		NamedElement<String> gesture = gestureSel.getSelected();
		if(gesture != null && gesture.getElem() != null) {
			for (EditorAnim anim : editor.animations) {
				if(anim.isCustom() && anim.pose == null && anim.getId().equals(gesture.getElem())) {
					editor.animsToPlay.add(anim);
				}
			}
		}
		editor.testPoses.clear();
		poseBoxes.forEach(ddb -> {
			if(!ddb.isVisible())return;
			VanillaPose p = ddb.getSelected().getElem();
			if(p != null)handlePose(p);
		});
		if(chbxLeftParrot.isSelected())handlePose(VanillaPose.PARROT_LEFT);
		if(chbxRightParrot.isSelected())handlePose(VanillaPose.PARROT_RIGHT);
	}

	private void handlePose(VanillaPose p) {
		editor.testPoses.add(p);
		for (EditorAnim anim : editor.animations) {
			if(anim.pose == p) {
				editor.animsToPlay.add(anim);
			}
		}
	}
}
