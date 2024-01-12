package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpl.function.FloatUnaryOperator;
import com.tom.cpm.shared.animation.InterpolationInfo;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.SerializedAnimation.AnimFrame;

public class ConstantTimeBool implements AnimationFrameData {
	private boolean[] frames;

	public ConstantTimeBool(boolean[] frames) {
		this.frames = frames;
	}

	public static void parse(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedAnimation cA = state.getAnim();
		int frames = block.readVarInt();
		int compCount = block.readVarInt();
		for (int i = 0; i < compCount; i++) {
			int channelID = block.readVarInt();
			AnimatorChannel ch = cA.animatorChannels.get(channelID);
			boolean[] f = block.readBoolArray(frames);
			ch.frameData = new ConstantTimeBool(f);
		}
	}

	public static void write(List<AnimFrame<ConstantTimeBool>> frames, IOHelper h) throws IOException {
		Map<Integer, List<AnimFrame<ConstantTimeBool>>> groups = new HashMap<>();
		frames.forEach(f -> groups.computeIfAbsent(f.data.frames.length, __ -> new ArrayList<>()).add(f));
		for (Entry<Integer, List<AnimFrame<ConstantTimeBool>>> e : groups.entrySet()) {
			try (IOHelper d = h.writeNextObjectBlock(TagType.CONSTANT_FRAME_TIME_BOOLEAN)) {
				int fs = e.getKey();
				d.writeVarInt(fs);
				d.writeVarInt(e.getValue().size());
				for (AnimFrame<ConstantTimeBool> f : e.getValue()) {
					d.writeVarInt(f.channel);
					d.writeBoolArray(fs, f.data.frames);
				}
			}
		}
	}

	@Override
	public AnimationFrameDataType getType() {
		return AnimationFrameDataType.CONSTANT_FRAME_BOOL;
	}

	public static class ConstantTimeBoolDriver implements FloatUnaryOperator {
		private final boolean[] frames;

		public ConstantTimeBoolDriver(boolean[] frames) {
			this.frames = frames;
		}

		@Override
		public float apply(float value) {
			return frames[(int) (value * frames.length)] ? 1f : 0f;
		}
	}

	@Override
	public FloatUnaryOperator createDriver(InterpolationInfo part) {
		return new ConstantTimeBoolDriver(frames);
	}
}