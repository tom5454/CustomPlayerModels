package com.tom.cpm.shared.animation;

import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.config.Player;

public class AnimationHandler {
	private final Player<?, ?> player;

	private List<PlayingAnim> currentAnimations = new ArrayList<>();
	private List<Animation> nextAnims = new ArrayList<>();

	private Gesture currentGesture;

	public AnimationHandler(Player<?, ?> player) {
		this.player = player;
	}

	public void animate(long currentTime) {
		boolean needsSort = false;
		currentAnimations.removeIf(a -> !nextAnims.contains(a.currentAnimation));
		for (Animation animation : nextAnims) {
			boolean found = false;
			for (int i = 0; i < currentAnimations.size(); i++) {
				if(currentAnimations.get(i).currentAnimation == animation) {
					found = true;
					break;
				}
			}
			if(!found) {
				currentAnimations.add(new PlayingAnim(animation, currentTime,
						currentGesture != null ? (currentGesture.animation.contains(animation) ? currentGesture.isLoop : true) : true));
				needsSort = true;
			}
		}
		if(needsSort)
			currentAnimations.sort((a, b) -> Integer.compare(a.currentAnimation.priority, b.currentAnimation.priority));

		player.getModelDefinition().resetAnimationPos();

		for (PlayingAnim a : currentAnimations) {
			if(!a.finished) {
				long currentStep = (currentTime - a.currentStart);
				a.currentAnimation.animate(currentStep, player.getModelDefinition());

				if(!a.loop && currentStep > a.currentAnimation.duration) {
					a.finished = true;
				}
			}
		}

		nextAnims.clear();
	}

	public void addAnimations(List<Animation> next) {
		nextAnims.addAll(next);
	}

	public void setGesture(Gesture next) {
		currentGesture = next;
		if(next != null) {
			nextAnims.addAll(next.animation);
		}
	}

	public void clear() {
		currentGesture = null;
		nextAnims.clear();
		currentAnimations.clear();
	}

	private static class PlayingAnim {
		private Animation currentAnimation;
		private long currentStart;
		private boolean loop, finished;

		public PlayingAnim(Animation currentAnimation, long currentStart, boolean loop) {
			this.currentAnimation = currentAnimation;
			this.currentStart = currentStart;
			this.loop = loop;
			this.finished = false;
		}
	}
}
