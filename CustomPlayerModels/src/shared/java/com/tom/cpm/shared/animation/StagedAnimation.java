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
		Anim a = new Anim(null, anim, Stage.SETUP);
		pre.add(a);
		all.add(a);
		return a;
	}

	public IAnimation addPlay(IAnimation anim) {
		if(anim instanceof Anim)return anim;
		Anim a = new Anim(null, anim, Stage.PLAY);
		play.add(a);
		all.add(a);
		return a;
	}

	public IAnimation addPost(IAnimation anim) {
		Anim a = new Anim(null, anim, Stage.FINISH);
		post.add(a);
		all.add(a);
		return a;
	}

	public void addPre(AnimationTrigger tr) {
		tr.animations.forEach(anim -> {
			Anim a = new Anim(tr, anim, Stage.SETUP);
			pre.add(a);
			all.add(a);
		});
	}

	public void addPlay(AnimationTrigger tr) {
		tr.animations.forEach(anim -> {
			Anim a = new Anim(tr, anim, Stage.PLAY);
			play.add(a);
			all.add(a);
		});
	}

	public void addPost(AnimationTrigger tr) {
		tr.animations.forEach(anim -> {
			Anim a = new Anim(tr, anim, Stage.FINISH);
			post.add(a);
			all.add(a);
		});
	}

	private static class AnimData {
		private long offset, lastFrame;
		private boolean finished;
		private boolean mustFinish, finishing;
	}

	private class Anim implements IAnimation {
		private final AnimationTrigger tr;
		private final IAnimation parent;
		private final Stage stage;
		private Map<AnimationMode, AnimData> data = new EnumMap<>(AnimationMode.class);

		public Anim(AnimationTrigger tr, IAnimation parent, Stage stage) {
			this.tr = tr;
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
			long lf = d.lastFrame;
			d.lastFrame = millis;
			if(currentStage.get(mode) == stage) {
				if(stage == Stage.FINISH && millis >= d.offset + parent.getDuration(mode))return;
				if(stage == Stage.SETUP && (millis - d.offset) >= parent.getDuration(mode)) {
					d.finished = true;
					if(pre.stream().allMatch(a -> a.data.get(mode).finished)) {
						currentStage.put(mode, Stage.PLAY);
					}
				} else if (stage == Stage.PLAY && d.finishing && (millis - d.offset) / parent.getDuration(mode) != (lf - d.offset) / parent.getDuration(mode)) {
					d.finished = true;
				} else if (tr == null || tr.canPlay(def.getPlayerObj(), mode))
					parent.animate(millis - d.offset, def, mode);
			} else
				d.offset = millis;
		}

		@Override
		public boolean checkAndUpdateRemove(AnimationMode mode) {
			AnimData d = data.get(mode);
			if (d == null)return true;
			if(stage == Stage.FINISH) {
				if (play.stream().allMatch(a -> {
					AnimData dt = a.data.get(mode);
					return !dt.mustFinish || dt.finished;
				})) {
					currentStage.put(mode, Stage.FINISH);
					if(d.lastFrame >= d.offset + parent.getDuration(mode)) {
						d.offset = 0;
						return true;
					}
				}
				return false;
			} else if (stage == Stage.PLAY && d.mustFinish) {
				d.finishing = true;
				return d.finished;
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

		@Override
		public boolean useTriggerTime() {
			return stage != Stage.FINISH;
		}

		private void reset(AnimationMode mode) {
			AnimData d = data.get(mode);
			if (d == null)data.put(mode, d = new AnimData());
			d.offset = 0;
			d.finished = false;
			d.finishing = false;
			d.mustFinish = stage == Stage.PLAY && tr != null && tr.mustFinish;
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
