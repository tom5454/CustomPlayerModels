package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.gui.elements.Slider;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.StagedAnimation;
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
	private PopupMenu customLayers;
	private Set<String> enabledLayers = new HashSet<>();
	private Set<PlayableAnim> animations = new HashSet<>();

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
			customLayers = new PopupMenu(gui, e);
			gestures.add(new NamedElement<>(null, k -> gui.i18nFormat("label.cpm.no_gesture")));
			for (VanillaPose p : VanillaPose.VALUES) {
				if(p == VanillaPose.CUSTOM || p == VanillaPose.GLOBAL)continue;
				AnimationDisplayData d = AnimationDisplayData.getFor(p);
				if(d.type == Type.POSE || d.type == Type.POSE_SERVER)
					poses.add(new NamedElement<IPose>(p, this::poseToString));
			}

			animations = editor.animations.stream().map(a -> Pair.of(new AnimKey(a), a)).filter(a -> a.getKey().type != null).
					collect(Collectors.groupingBy(Pair::getKey)).entrySet().stream().
					map(me -> new PlayableAnim(me.getKey(), me.getValue().stream().map(Pair::getValue).collect(Collectors.toList()))).
					collect(Collectors.toSet());

			Set<String> addedLayers = new HashSet<>();
			animations.forEach(a -> {
				if(a.key.type.isCustom()) {
					if(a.key.type == AnimationType.CUSTOM_POSE) {
						String name = a.key.name;
						poses.add(new NamedElement<>(a.key.pose, k -> name));
					} else if(a.key.type.isLayer()) {
						String id = a.key.name;
						addedLayers.add(id);
						if(a.key.type == AnimationType.VALUE_LAYER) {
							Slider progressSlider = new Slider(gui, id + ": 0");
							progressSlider.setBounds(new Box(0, 0, 160, 20));
							progressSlider.setValue(editor.animTestSliders.getOrDefault(id, a.anims.get(0).layerDefault));
							progressSlider.setAction(() -> {
								progressSlider.setText(id + ": " + ((int) (progressSlider.getValue() * 100)));
								editor.animTestSliders.put(id, progressSlider.getValue());
							});
							customLayers.add(progressSlider);
						} else {
							customLayers.addCheckbox(id, b -> {
								if(enabledLayers.contains(id)) {
									enabledLayers.remove(id);
								} else {
									enabledLayers.add(id);
								}
								b.setSelected(enabledLayers.contains(id));
							}).setSelected(enabledLayers.contains(id));
						}
					} else if(a.key.type == AnimationType.GESTURE) {
						gestures.add(new NamedElement<>(a.key.name, Function.identity()));
					}
				}
			});
			if(addedLayers.isEmpty()) {
				customLayers.add(new Label(gui, gui.i18nFormat("label.cpm.no_elements")).setBounds(new Box(5, 5, 0, 0)));
			}
		});

		poseSel = createDropDown("label.cpm.pose", poses);

		gestureSel = createDropDown("label.cpm.gesture", gestures);

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
		handItems.add(new NamedElement<>(VanillaPose.SPEAKING, this::poseToString));
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
		progressSlider.setValue(editor.animTestSliders.getOrDefault("__pose", 0f));
		progressSlider.setAction(() -> {
			progressSlider.setText(gui.i18nFormat("label.cpm.animProgress", (int) (progressSlider.getValue() * 100)));
			editor.animTestSliders.put("__pose", progressSlider.getValue());
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

			DropDownPanel<VanillaPose> dropDown = createDropDown("label.cpm.animSlot." + key.name().toLowerCase(Locale.ROOT), val);
			poseBoxes.add(dropDown);
		}

		List<NamedElement<VanillaPose>> val = new ArrayList<>();
		val.add(new NamedElement<>(null, k -> gui.i18nFormat("label.cpm.animSlot.noEffect")));
		for (VanillaPose p : VanillaPose.VALUES) {
			AnimationDisplayData d = AnimationDisplayData.getFor(p);
			if(d == null || d.layerSlot != null || d.type != Type.LAYERS || p.hasStateGetter())continue;
			val.add(new NamedElement<>(p, this::poseToString));
		}
		DropDownPanel<VanillaPose> dropDown = createDropDown("label.cpm.animSlot.effects", val);
		poseBoxes.add(dropDown);

		chbxLeftParrot = new Checkbox(gui, gui.i18nFormat("label.cpm.animSlot.leftParrot"));
		chbxLeftParrot.setBounds(new Box(5, 0, 160, 20));
		chbxLeftParrot.setAction(() -> chbxLeftParrot.setSelected(!chbxLeftParrot.isSelected()));
		addElement(chbxLeftParrot);

		chbxRightParrot = new Checkbox(gui, gui.i18nFormat("label.cpm.animSlot.rightParrot"));
		chbxRightParrot.setBounds(new Box(5, 0, 160, 20));
		chbxRightParrot.setAction(() -> chbxRightParrot.setSelected(!chbxRightParrot.isSelected()));
		addElement(chbxRightParrot);

		Panel layersPanel = new Panel(gui);
		layersPanel.setBounds(new Box(5, 0, 160, 20));
		layersPanel.addElement(new Label(gui, gui.i18nFormat("label.cpm.customLayers")).setBounds(new Box(0, 6, 0, 0)));

		Button layersDropDown = new ButtonIcon(gui, "editor", 24, 8, null) {

			@Override
			public void mouseClick(MouseEvent evt) {
				if(evt.isHovered(bounds)) {
					Vec2i p = evt.getPos();
					customLayers.display(p.x - evt.x, p.y - evt.y + bounds.h + bounds.y, 160);
					evt.consume();
				}
			}
		};
		layersDropDown.setBounds(new Box(140, 4, 12, 12));
		layersPanel.addElement(layersDropDown);
		addElement(layersPanel);

		layout.reflow();
	}

	private <T> DropDownPanel<T> createDropDown(String name, List<NamedElement<T>> val) {
		DropDownPanel<T> panel = new DropDownPanel<>(name, val);
		addElement(panel);
		return panel;
	}

	private void gestureFinished() {
		gestureSel.dropDown.setSelected(null);
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
			editor.playFullAnim = true;
			editor.selectedAnim = null;
		}
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		super.draw(event, partialTicks);
		editor.animsToPlay.clear();
		animations.forEach(a -> a.enable = false);
		NamedElement<IPose> pose = poseSel.getSelected();
		if(pose != null) {
			editor.poseToApply = pose.getElem();
			for (PlayableAnim anim : animations) {
				if(anim.key.matches(editor.poseToApply)) {
					anim.enable = true;
				} else if(anim.key.pose == VanillaPose.GLOBAL) {
					anim.enable = true;
				}
			}
		}
		NamedElement<String> gesture = gestureSel.getSelected();
		if(gesture != null && gesture.getElem() != null) {
			for (PlayableAnim anim : animations) {
				if(anim.key.type == AnimationType.GESTURE && anim.key.name.equals(gesture.getElem())) {
					anim.enable = true;
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
		enabledLayers.forEach(l -> {
			for (PlayableAnim anim : animations) {
				if(anim.key.type == AnimationType.LAYER && anim.key.name.equals(l)) {
					anim.enable = true;
				}
			}
		});
		for (PlayableAnim anim : animations) {
			if(anim.key.type == AnimationType.VALUE_LAYER) {
				anim.enable = true;
			}
		}
		animations.stream().flatMap(PlayableAnim::get).sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority())).forEach(editor.animsToPlay::add);
	}

	private void handlePose(VanillaPose p) {
		editor.testPoses.add(p);
		for (PlayableAnim anim : animations) {
			if(anim.key.pose == p) {
				anim.enable = true;
			}
		}
	}

	private class AnimKey {
		private AnimationType type;
		private String name;
		private IPose pose;

		public AnimKey(EditorAnim anim) {
			if(anim.type.isStaged()) {
				anim = anim.findLinkedAnim();
				if(anim == null)return;
			}
			type = anim.type;
			name = anim.getId();
			pose = anim.pose;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((pose == null) ? 0 : pose.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			AnimKey other = (AnimKey) obj;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			if (pose == null) {
				if (other.pose != null) return false;
			} else if (!pose.equals(other.pose)) return false;
			if (type != other.type) return false;
			return true;
		}

		public boolean matches(IPose pose) {
			return (type == AnimationType.POSE || type == AnimationType.CUSTOM_POSE) && this.pose.equals(pose);
		}
	}

	private class PlayableAnim {
		private final List<EditorAnim> anims;
		private final AnimKey key;
		private StagedAnimation handler;
		private long playStartTime;
		private boolean enable, playing;
		private int duration;

		public PlayableAnim(AnimKey key, List<EditorAnim> anims) {
			this.anims = anims;
			this.key = key;
			if(anims.stream().anyMatch(e -> e.type.isStaged())) {
				handler = new StagedAnimation();
				anims.forEach(a -> {
					if(a.type == AnimationType.SETUP)handler.addPre(a);
					else if(a.type == AnimationType.FINISH)handler.addPost(a);
					else handler.addPlay(a);
				});
			}
			if(key.type == AnimationType.GESTURE) {
				duration = anims.stream().mapToInt(a -> a.loop ? 0 : a.duration).max().orElse(0);
			}
		}

		public Stream<TestAnim> get() {
			long playTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
			if(enable && !playing) {
				playStartTime = playTime;
				playing = true;
				if(handler != null) {
					handler.getAll().forEach(IAnimation::prepare);
				}
			} else if(!enable) {
				playing = false;
			}

			if(enable && key.type == AnimationType.GESTURE) {
				if(playTime - playStartTime > duration && !anims.stream().anyMatch(a -> a.loop))
					gestureFinished();
			}
			return (handler != null ?
					handler.getAll().stream().filter(v -> enable || !v.checkAndUpdateRemove()) :
						(enable ? anims.stream() : Stream.<IAnimation>empty())
					).map(TestAnim::new);
		}

		private class TestAnim implements Runnable {
			private IAnimation anim;

			public TestAnim(IAnimation anim) {
				this.anim = anim;
			}

			@Override
			public void run() {
				long playTime = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
				anim.animate(playTime - playStartTime, editor.definition);
			}

			public int getPriority() {
				return anim.getPriority();
			}
		}
	}
}
