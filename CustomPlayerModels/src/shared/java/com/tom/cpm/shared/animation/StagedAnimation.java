package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.definition.ModelDefinition;

public class StagedAnimation {
	private Map<AnimationMode, Stage> currentStage = new EnumMap<>(AnimationMode.class);
	private List<Anim> pre = new ArrayList<>();
	private List<Anim> play = new ArrayList<>();
	private List<Anim> post = new ArrayList<>();
	private List<IAnimation> all = new ArrayList<>();

	public StagedAnimation() {
	}

	public IAnimation addPre(IAnimation anim) {
		Anim a = new Anim(anim, Stage.SETUP);
		pre.add(a);
		all.add(a);
		return a;
	}

	public IAnimation addPlay(IAnimation anim) {
		if(anim instanceof Anim)return anim;
		Anim a = new Anim(anim, Stage.PLAY);
		play.add(a);
		all.add(a);
		return a;
	}

	public IAnimation addPost(IAnimation anim) {
		Anim a = new Anim(anim, Stage.FINISH);
		post.add(a);
		all.add(a);
		return a;
	}

	private static class AnimData {
		private long offset, lastFrame;
		private boolean finished;
	}

	private class Anim implements IAnimation {
		private final IAnimation parent;
		private final Stage stage;
		private Map<AnimationMode, AnimData> data = new EnumMap<>(AnimationMode.class);

		public Anim(IAnimation parent, Stage stage) {
			this.parent = parent;
			this.stage = stage;
		}

		@Override
		public int getDuration(AnimationMode mode) {
			if(stage == Stage.FINISH)return Integer.MAX_VALUE;
			return parent.getDuration(mode);
		}

		@Override
		public int getPriority(AnimationMode mode) {
			return parent.getPriority(mode);
		}

		@Override
		public void animate(long millis, ModelDefinition def, AnimationMode mode) {
			AnimData d = data.get(mode);
			d.lastFrame = millis;
			if(currentStage.get(mode) == stage) {
				if(stage == Stage.FINISH && d.lastFrame >= d.offset + parent.getDuration(mode))return;
				if(stage == Stage.SETUP && (millis - d.offset) >= parent.getDuration(mode)) {
					d.finished = true;
					if(pre.stream().allMatch(a -> a.data.get(mode).finished)) {
						currentStage.put(mode, Stage.PLAY);
					}
				} else
					parent.animate(millis - d.offset, def, mode);
			} else
				d.offset = millis;
		}

		@Override
		public boolean checkAndUpdateRemove(AnimationMode mode) {
			AnimData d = data.get(mode);
			if (d == null)return true;
			if(stage == Stage.FINISH) {
				currentStage.put(mode, Stage.FINISH);
				if(d.lastFrame >= d.offset + parent.getDuration(mode)) {
					d.offset = 0;
					return true;
				}
				return false;
			}
			d.finished = false;
			d.offset = 0;
			return true;
		}

		@Override
		public void prepare(AnimationMode mode) {
			currentStage.put(mode, pre.isEmpty() ? Stage.PLAY : Stage.SETUP);
			all.forEach(a -> ((Anim) a).reset(mode));
		}

		private void reset(AnimationMode mode) {
			AnimData d = data.get(mode);
			if (d == null)data.put(mode, d = new AnimData());
			d.offset = 0;
			d.finished = false;
		}
	}

	public static enum Stage {
		SETUP,
		PLAY,
		FINISH
	}

	public List<IAnimation> getAll() {
		return all;
	}
}
