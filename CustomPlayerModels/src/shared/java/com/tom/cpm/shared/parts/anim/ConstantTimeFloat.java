package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpl.function.FloatUnaryOperator;
import com.tom.cpm.shared.animation.InterpolationInfo;
import com.tom.cpm.shared.animation.interpolator.Interpolator;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.SerializedAnimation.AnimFrame;

public class ConstantTimeFloat implements AnimationFrameData {
	private InterpolatorType intType;
	private float[] frames;

	public ConstantTimeFloat(InterpolatorType intType, float[] frames) {
		this.intType = intType;
		this.frames = frames;
	}

	public static void parse(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedAnimation cA = state.getAnim();
		InterpolatorType intType = block.readEnum(InterpolatorType.VALUES);
		int frames = block.readVarInt();
		int compCount = block.readVarInt();
		for (int i = 0; i < compCount; i++) {
			int channelID = block.readVarInt();
			AnimatorChannel ch = cA.animatorChannels.get(channelID);
			float[] f = new float[frames];
			for (int j = 0; j < frames; j++) {
				f[j] = block.readVarFloat();
			}
			ch.frameData = new ConstantTimeFloat(intType, f);
		}
	}

	private static class AnimType {
		private final InterpolatorType intType;
		private final int frames;

		public AnimType(InterpolatorType intType, int frames) {
			this.intType = intType;
			this.frames = frames;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + frames;
			result = prime * result + ((intType == null) ? 0 : intType.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			AnimType other = (AnimType) obj;
			if (frames != other.frames) return false;
			if (intType != other.intType) return false;
			return true;
		}
	}

	public static void write(List<AnimFrame<ConstantTimeFloat>> frames, IOHelper h) throws IOException {
		Map<AnimType, List<AnimFrame<ConstantTimeFloat>>> groups = new HashMap<>();
		frames.forEach(f -> groups.computeIfAbsent(new AnimType(f.data.intType, f.data.frames.length), __ -> new ArrayList<>()).add(f));
		for (Entry<AnimType, List<AnimFrame<ConstantTimeFloat>>> e : groups.entrySet()) {
			try (IOHelper d = h.writeNextObjectBlock(TagType.CONSTANT_FRAME_TIME_FLOAT)) {
				AnimType t = e.getKey();
				d.writeEnum(t.intType);
				d.writeVarInt(t.frames);
				d.writeVarInt(e.getValue().size());
				for (AnimFrame<ConstantTimeFloat> f : e.getValue()) {
					d.writeVarInt(f.channel);
					for (int i = 0;i<t.frames;i++) {
						d.writeVarFloat(f.data.frames[i]);
					}
				}
			}
		}
	}

	@Override
	public AnimationFrameDataType getType() {
		return AnimationFrameDataType.CONSTANT_FRAME_FLOAT;
	}

	public static class ConstantTimeFloatDriver implements FloatUnaryOperator {
		private final Interpolator intp;
		private final int frames;

		public ConstantTimeFloatDriver(InterpolationInfo output, InterpolatorType intType, float[] frames) {
			this.frames = frames.length;
			intp = intType.create();
			intp.init(frames, output.createInterpolatorSetup());
		}

		@Override
		public float apply(float value) {
			if(frames == 0)return 0f;
			return (float) intp.applyAsDouble(value * frames);
		}
	}

	@Override
	public FloatUnaryOperator createDriver(InterpolationInfo part) {
		return new ConstantTimeFloatDriver(part, intType, frames);
	}
}