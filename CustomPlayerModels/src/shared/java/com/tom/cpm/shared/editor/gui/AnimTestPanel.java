package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.gui.IGui;
import com.tom.cpm.shared.gui.elements.DropDownBox;
import com.tom.cpm.shared.gui.elements.Label;
import com.tom.cpm.shared.gui.elements.Panel;
import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.util.NamedElement;

public class AnimTestPanel extends Panel {
	private Editor editor;
	private DropDownBox<NamedElement<IPose>> poseSel;
	private DropDownBox<NamedElement<String>> gestureSel;

	public AnimTestPanel(IGui gui, EditorGui e) {
		super(gui);
		editor = e.getEditor();
		setBounds(new Box(0, 0, 170, 80));
		setBackgroundColor(gui.getColors().panel_background);

		List<NamedElement<IPose>> poses = new ArrayList<>();
		List<NamedElement<String>> gestures = new ArrayList<>();
		editor.updateGui.add(() -> {
			poses.clear();
			gestures.clear();
			gestures.add(new NamedElement<>(null, k -> gui.i18nFormat("label.cpm.no_gesture")));
			for (VanillaPose p : VanillaPose.VALUES) {
				if(p == VanillaPose.CUSTOM || p == VanillaPose.GLOBAL)continue;
				poses.add(new NamedElement<IPose>(p, k -> k.getName(gui, null)));
			}
			Set<String> addedGestures = new HashSet<>();
			Set<String> addedPoses = new HashSet<>();
			editor.animations.forEach(a -> {
				if(a.isCustom()) {
					if(a.pose != null) {
						String name = ((CustomPose)a.pose).getName();
						if(!addedPoses.contains(name)) {
							addedPoses.add(name);
							poses.add(new NamedElement<>(a.pose, k -> name));
						}
					} else {
						if(!addedGestures.contains(a.displayName)) {
							addedGestures.add(a.displayName);
							gestures.add(new NamedElement<>(a.displayName, k -> k));
						}
					}
				}
			});
		});

		addElement(new Label(gui, gui.i18nFormat("label.cpm.pose")).setBounds(new Box(5, 5, 160, 10)));

		poseSel = new DropDownBox<>(e, poses);
		poseSel.setBounds(new Box(5, 15, 160, 20));
		addElement(poseSel);

		addElement(new Label(gui, gui.i18nFormat("label.cpm.gesture")).setBounds(new Box(5, 40, 160, 10)));

		gestureSel = new DropDownBox<>(e, gestures);
		gestureSel.setBounds(new Box(5, 50, 160, 20));
		gestureSel.setAction(() -> editor.gestureStartTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime());
		addElement(gestureSel);

		editor.gestureFinished.add(() -> gestureSel.setSelected(null));
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
	public void draw(int mouseX, int mouseY, float partialTicks) {
		super.draw(mouseX, mouseY, partialTicks);
		editor.animsToPlay.clear();
		NamedElement<IPose> pose = poseSel.getSelected();
		if(pose != null) {
			editor.poseToApply = pose.getElem();
			for (EditorAnim anim : editor.animations) {
				if(anim.pose != null && (anim.pose == pose.getElem() ||
						(pose.getElem() instanceof CustomPose && anim.pose instanceof CustomPose &&
								((CustomPose)pose.getElem()).getName().equals(((CustomPose)anim.pose).getName())
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
				if(anim.isCustom() && anim.pose == null && anim.displayName.equals(gesture.getElem())) {
					editor.animsToPlay.add(anim);
				}
			}
		}
	}
}
