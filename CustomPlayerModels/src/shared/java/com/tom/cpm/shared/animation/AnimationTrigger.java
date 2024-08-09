package com.tom.cpm.shared.animation;

import java.util.List;
import java.util.Set;

import com.tom.cpl.math.MathHelper;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.network.ServerCaps;

public class AnimationTrigger {
	public final Set<IPose> onPoses;
	public final List<IAnimation> animations;
	public final boolean looping, mustFinish;
	public final VanillaPose valuePose;

	public AnimationTrigger(Set<IPose> onPoses, VanillaPose valuePose, List<IAnimation> animations, boolean looping, boolean mustFinish) {
		this.onPoses = onPoses;
		this.valuePose = valuePose;
		this.animations = animations;
		this.looping = looping;
		this.mustFinish = mustFinish;
	}

	public long getTime(AnimationState state, long time) {
		return valuePose != null ? valuePose.getTime(state, time) : time;
	}

	public boolean canPlay(Player<?> pl, AnimationMode mode) {
		return true;
	}

	public static class LayerTrigger extends AnimationTrigger {
		private final int id, mask;
		private final boolean bitmask;

		public LayerTrigger(Set<IPose> onPoses, List<IAnimation> animations, int id, int mask, boolean bitmask, boolean mustFinish) {
			super(onPoses, null, animations, true, mustFinish);
			this.id = id;
			this.mask = mask;
			this.bitmask = bitmask;
		}

		@Override
		public boolean canPlay(Player<?> pl, AnimationMode mode) {
			if (pl.animState.gestureData != null && pl.animState.gestureData.length > id) {
				byte v = pl.animState.gestureData[id];
				if (bitmask)return (v & mask) == mask;
				else return v == mask;
			}
			return false;
		}
	}

	public static class GestureTrigger extends AnimationTrigger {
		private final int value, gid;

		public GestureTrigger(Set<IPose> onPoses, List<IAnimation> animations, int value, int gid, boolean looping, boolean mustFinish) {
			super(onPoses, null, animations, looping, mustFinish);
			this.value = value;
			this.gid = gid;
		}

		@Override
		public boolean canPlay(Player<?> pl, AnimationMode mode) {
			if (MinecraftClientAccess.get().getNetHandler().hasServerCap(ServerCaps.GESTURES)) {
				if (pl.animState.gestureData != null && pl.animState.gestureData.length > 1) {
					byte v = pl.animState.gestureData[1];
					return v == value;
				}
			} else if(gid != -1) {
				return pl.animState.encodedState == gid;
			}
			return false;
		}
	}

	public static class ValueTrigger extends AnimationTrigger {
		private final int id;
		private boolean interpolate;

		public ValueTrigger(Set<IPose> onPoses, List<IAnimation> animations, int id, boolean interpolate) {
			super(onPoses, null, animations, true, false);
			this.id = id;
			this.interpolate = interpolate;
		}

		@Override
		public long getTime(AnimationState state, long animTime) {
			if (state.gestureData != null && state.gestureData.length > id) {
				float val = Byte.toUnsignedInt(state.gestureData[id]) / 256f;
				long time = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime();
				if (interpolate && state.prevGestureData != null && state.prevGestureData.length == state.gestureData.length && state.lastGestureReceiveTime + 50 >= time) {
					float prev = Byte.toUnsignedInt(state.prevGestureData[id]) / 256f;
					val = MathHelper.lerp((time - state.lastGestureReceiveTime) / 50f, prev, val);
				}
				return (long) (val * VanillaPose.DYNAMIC_DURATION_MUL);
			} else
				return 0L;
		}
	}
}
