package com.tom.cpm.shared.animation;

import com.tom.cpm.shared.animation.AnimationRegistry.Gesture;
import com.tom.cpm.shared.config.Player;

public class AnimationHandler {
	private final Player player;

	private Animation currentAnimation;
	private long currentStart;

	private Gesture currentGesture;
	private long gestureStart;

	private Animation nextAnimation;

	public AnimationHandler(Player player) {
		this.player = player;
	}

	public void animate(long currentTime) {
		if (nextAnimation != null) {
			currentAnimation = nextAnimation;
			currentStart = currentTime;
			nextAnimation = null;
		}

		player.getModelDefinition().resetAnimationPos();

		if (currentAnimation != null) {
			long currentStep = (currentTime - currentStart);
			currentAnimation.animate(currentStep, player.getModelDefinition());
		}

		if(currentGesture != null) {
			long currentStep = (currentTime - gestureStart);
			currentGesture.animation.animate(currentStep, player.getModelDefinition());

			if(currentStep > currentGesture.animation.duration && !currentGesture.isLoop)
				currentGesture = null;
		}
	}

	public void setNextAnimation(Animation next) {
		if(currentAnimation != next)
			nextAnimation = next;
	}

	public void setGesture(Gesture next) {
		if(currentGesture != next)
			currentGesture = next;
	}
}
