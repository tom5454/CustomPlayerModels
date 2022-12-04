package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.definition.ModelDefinition;

public class StagedAnimation {
	private Stage currentStage;
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

	private class Anim implements IAnimation {
		private final IAnimation parent;
		private final Stage stage;
		private long offset, lastFrame;
		private boolean finished;

		public Anim(IAnimation parent, Stage stage) {
			this.parent = parent;
			this.stage = stage;
		}

		@Override
		public int getDuration() {
			if(stage == Stage.FINISH)return Integer.MAX_VALUE;
			return parent.getDuration();
		}

		@Override
		public int getPriority() {
			return parent.getPriority();
		}

		@Override
		public void animate(long millis, ModelDefinition def) {
			lastFrame = millis;
			if(currentStage == stage) {
				if(stage == Stage.FINISH && lastFrame >= offset + parent.getDuration())return;
				if(stage == Stage.SETUP && (millis - offset) >= parent.getDuration()) {
					finished = true;
					if(pre.stream().allMatch(a -> a.finished)) {
						currentStage = Stage.PLAY;
					}
				} else
					parent.animate(millis - offset, def);
			} else
				offset = millis;
		}

		@Override
		public boolean checkAndUpdateRemove() {
			if(stage == Stage.FINISH) {
				currentStage = Stage.FINISH;
				if(lastFrame >= offset + parent.getDuration()) {
					offset = 0;
					return true;
				}
				return false;
			}
			finished = false;
			offset = 0;
			return true;
		}

		@Override
		public void prepare() {
			currentStage = Stage.SETUP;
			all.forEach(a -> ((Anim) a).reset());
		}

		private void reset() {
			offset = 0;
			finished = false;
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
