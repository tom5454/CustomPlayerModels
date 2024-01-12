package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.AnimatorChannel.CubeColorDriver;
import com.tom.cpm.shared.parts.anim.AnimatorChannel.CubePosDriver;
import com.tom.cpm.shared.parts.anim.AnimatorChannel.CubeRotDriver;
import com.tom.cpm.shared.parts.anim.AnimatorChannel.CubeScaleDriver;
import com.tom.cpm.shared.parts.anim.AnimatorChannel.CubeVisDriver;
import com.tom.cpm.shared.parts.anim.Float3Driver.Float3Consumer;

public class SerializedAnimation {
	public static final int ADDITIVE = 1 << 0;

	public int triggerID, priority, duration;
	public Map<Integer, AnimatorChannel> animatorChannels = new HashMap<>();
	private int chID = 0;

	public int addChannel(AnimatorChannel c) {
		int i = chID++;
		animatorChannels.put(i, c);
		return i;
	}

	public static void newAnimation(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedAnimation cA = new SerializedAnimation();
		cA.triggerID = block.readVarInt();
		cA.priority = block.readSignedVarInt();
		cA.duration = block.readVarInt();
		state.newAnimation(cA);
	}

	public static void addCubesToChannels(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedAnimation cA = state.getAnim();
		int cubeCount = block.readVarInt();
		int intChCount = block.readVarInt();
		int flags = block.read();
		for (int i = 0; i < cubeCount; i++) {
			int id = block.readVarInt();
			boolean additive = (flags & ADDITIVE) != 0;
			if (intChCount == 12) {
				Float3Driver.make(new CubePosDriver(id, additive), InterpolatorChannel.POS_X, InterpolatorChannel.POS_Y, InterpolatorChannel.POS_Z, (ic, ac) -> cA.addChannel(ac));
				Float3Driver.make(new CubeRotDriver(id, additive), InterpolatorChannel.ROT_X, InterpolatorChannel.ROT_Y, InterpolatorChannel.ROT_Z, (ic, ac) -> cA.addChannel(ac));
				Float3Driver.make(new CubeColorDriver(id, additive), InterpolatorChannel.COLOR_R, InterpolatorChannel.COLOR_G, InterpolatorChannel.COLOR_B, (ic, ac) -> cA.addChannel(ac));
				Float3Driver.make(new CubeScaleDriver(id, additive), InterpolatorChannel.SCALE_X, InterpolatorChannel.SCALE_Y, InterpolatorChannel.SCALE_Z, (ic, ac) -> cA.addChannel(ac));
			}
			cA.animatorChannels.put(cA.chID++, new AnimatorChannel(new CubeVisDriver(id)));
		}
	}

	private static void writeCubeMaps(List<Integer> cubes, IOHelper dout, boolean add) throws IOException {
		if (cubes.isEmpty())return;
		try (IOHelper d = dout.writeNextObjectBlock(TagType.CUBES_TO_CHANNELS)) {
			d.writeVarInt(cubes.size());
			d.writeVarInt(InterpolatorChannel.VALUES.length);
			int flags = 0;
			if (add)flags |= ADDITIVE;
			d.writeVarInt(flags);
			for (int i = 0; i < cubes.size(); i++) {
				d.writeVarInt(cubes.get(i));
			}
		}
	}

	public void write(IOHelper dout) throws IOException {
		try (IOHelper d = dout.writeNextObjectBlock(TagType.NEW_ANIM)) {
			d.writeVarInt(this.triggerID);
			d.writeSignedVarInt(this.priority);
			d.writeVarInt(duration);
		}
		List<Integer> cubes = new ArrayList<>();
		boolean add = false;
		for (int j = 0;j<this.animatorChannels.size();j++) {
			AnimatorChannel ac = this.animatorChannels.get(j);
			if (ac.isMappedCube()) {
				Float3Consumer f3c = Float3Driver.getFromPart(ac.part);
				if (f3c != null && f3c instanceof CubePosDriver) {
					CubePosDriver cfd = (CubePosDriver) f3c;
					j += InterpolatorChannel.VALUES.length;
					cubes.add(cfd.cubeId);
					add = cfd.additive;
				} else {
					throw new IOException("Misaligned cube info");
				}
			} else {
				//TODO
				throw new RuntimeException("Can't export non cube mapped channels");
			}
		}
		writeCubeMaps(cubes, dout, add);
		Map<AnimationFrameDataType, List<AnimFrame<?>>> framesByType = new HashMap<>();
		for (int j = 0;j<this.animatorChannels.size();j++) {
			AnimationFrameData frm = this.animatorChannels.get(j).frameData;
			if (frm != null)
				framesByType.computeIfAbsent(frm.getType(), __ -> new ArrayList<>()).add(new AnimFrame<>(j, frm));
		}
		for (Entry<AnimationFrameDataType, List<AnimFrame<?>>> e : framesByType.entrySet()) {
			e.getKey().write(e.getValue(), dout);
		}
	}

	public static class AnimFrame<T extends AnimationFrameData> {
		public final int channel;
		public final T data;

		public AnimFrame(int channel, T data) {
			this.channel = channel;
			this.data = data;
		}
	}

	@Override
	public String toString() {
		/*StringBuilder sb = new StringBuilder("Animation: " + triggerID);
		animatorChannels.forEach((i, c) -> {
			sb.append("\n\t");
			sb.append(i);
			sb.append(": ");
			sb.append(c.toString().replace("\n", "\n\t"));
		});
		return sb.toString();*/
		return "Animation: " + triggerID;
	}
}