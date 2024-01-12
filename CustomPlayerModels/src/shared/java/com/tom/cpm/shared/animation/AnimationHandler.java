package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;

public class AnimationHandler {
	private final Supplier<ModelDefinition> player;
	private final AnimationMode mode;

	private List<PlayingAnim> currentAnimations = new ArrayList<>();
	private List<NextAnim> nextAnims = new ArrayList<>();

	private Gesture currentGesture;

	public AnimationHandler(Supplier<ModelDefinition> player, AnimationMode mode) {
		this.player = player;
		this.mode = mode;
	}

	public void animate(AnimationState state, long currentTime) {
		boolean needsSort = false;
		currentAnimations.removeIf(a -> nextAnims.stream().noneMatch(n -> n.animation == a.currentAnimation) && a.currentAnimation.checkAndUpdateRemove(mode));
		for (NextAnim animation : nextAnims) {
			boolean found = false;
			for (int i = 0; i < currentAnimations.size(); i++) {
				if(currentAnimations.get(i).currentAnimation == animation.animation) {
					found = true;
					break;
				}
			}
			if(!found) {
				currentAnimations.add(new PlayingAnim(animation, currentTime,
						currentGesture != null ? (currentGesture.animation.contains(animation.animation) ? currentGesture.isLoop : true) : true, mode));
				needsSort = true;
			}
		}
		if(needsSort)
			currentAnimations.sort(Comparator.comparingInt(e -> e.currentAnimation.getPriority(mode)));

		player.get().resetAnimationPos();

		for (PlayingAnim a : currentAnimations) {
			if(!a.finished) {
				long currentStep = (currentTime - a.currentStart);
				a.currentAnimation.animate(a.getTime(state, currentStep), player.get(), mode);

				if(!a.loop && currentStep > a.currentAnimation.getDuration(mode)) {
					a.finished = true;
				}
			}
		}

		nextAnims.clear();
	}

	public void addAnimations(List<? extends IAnimation> next, IPose pose) {
		Player<?> pl = player.get().getPlayerObj();
		next.stream().filter(a -> a.canPlay(pl, mode)).map(a -> new NextAnim(a, pose)).forEach(nextAnims::add);
	}

	public void setGesture(Gesture next) {
		currentGesture = next;
		if(next != null) {
			addAnimations(next.animation, null);
		}
	}

	public void clear() {
		currentGesture = null;
		nextAnims.clear();
		currentAnimations.clear();
	}

	private static class NextAnim {
		private IAnimation animation;
		private IPose pose;

		public NextAnim(IAnimation animation, IPose pose) {
			this.animation = animation;
			this.pose = pose;
		}
	}

	private static class PlayingAnim {
		private IAnimation currentAnimation;
		private IPose pose;
		private long currentStart;
		private boolean loop, finished;

		public PlayingAnim(NextAnim anim, long currentStart, boolean loop, AnimationMode mode) {
			this.currentAnimation = anim.animation;
			currentAnimation.prepare(mode);
			this.pose = anim.pose;
			this.currentStart = currentStart;
			this.loop = loop;
			this.finished = false;
		}

		public long getTime(AnimationState state, long time) {
			if(pose == null)return time;
			else return pose.getTime(state, time);
		}
	}
}
