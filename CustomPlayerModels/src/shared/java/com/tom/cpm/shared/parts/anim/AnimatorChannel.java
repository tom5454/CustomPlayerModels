package com.tom.cpm.shared.parts.anim;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.tom.cpm.shared.animation.AnimationNew;
import com.tom.cpm.shared.animation.AnimationNew.AnimationDriver;
import com.tom.cpm.shared.animation.AnimationNew.PartAnimationDriver;
import com.tom.cpm.shared.animation.InterpolationInfo;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.parts.anim.Float3Driver.Float3Consumer;

public class AnimatorChannel {
	public PartAnimationDriver part;
	public AnimationFrameData frameData;
	private boolean mappedCube;

	public AnimatorChannel(PartAnimationDriver part) {
		this.part = part;
	}

	public static Map<InterpolatorChannel, Integer> addCubeToChannels(SerializedAnimation an, int cubeId, boolean additive) {
		Map<InterpolatorChannel, Integer> map = new HashMap<>();
		BiConsumer<InterpolatorChannel, AnimatorChannel> reg = (ic, ac) -> {
			ac.mappedCube = true;
			map.put(ic, an.addChannel(ac));
		};
		Float3Driver.make(new CubePosDriver(cubeId, additive), InterpolatorChannel.POS_X, InterpolatorChannel.POS_Y, InterpolatorChannel.POS_Z, reg);
		Float3Driver.make(new CubeRotDriver(cubeId, additive), InterpolatorChannel.ROT_X, InterpolatorChannel.ROT_Y, InterpolatorChannel.ROT_Z, reg);
		Float3Driver.make(new CubeColorDriver(cubeId, additive), InterpolatorChannel.COLOR_R, InterpolatorChannel.COLOR_G, InterpolatorChannel.COLOR_B, reg);
		Float3Driver.make(new CubeScaleDriver(cubeId, additive), InterpolatorChannel.SCALE_X, InterpolatorChannel.SCALE_Y, InterpolatorChannel.SCALE_Z, reg);
		AnimatorChannel ac = new AnimatorChannel(new CubeVisDriver(cubeId));
		reg.accept(null, ac);
		return map;
	}

	public boolean isMappedCube() {
		return mappedCube;
	}

	public void addToAnim(AnimationNew a, ModelDefinition def) {
		part.init(def);
		AnimationDriver d;
		if (frameData != null)
			d = part.makeDriver(frameData.createDriver(part.getInterpolationInfo()));
		else
			d = part.makeDriver(null);
		if (d != null)
			a.add(d);
	}

	public static abstract class CubeFloatDriver implements Float3Consumer {
		public final int cubeId;
		public boolean additive;
		protected RenderedCube cube;

		public CubeFloatDriver(int cubeId, boolean additive) {
			this.cubeId = cubeId;
			this.additive = additive;
		}

		@Override
		public void init(ModelDefinition def) {
			cube = def.getElementById(cubeId);
		}
	}

	public static class CubePosDriver extends CubeFloatDriver {

		public CubePosDriver(int cubeId, boolean additive) {
			super(cubeId, additive);
		}

		@Override
		public void set(float x, float y, float z) {
			cube.setPosition(additive, x, y, z);
		}

		@Override
		public float getX() {
			return additive ? 0 : cube.getPosition().x;
		}

		@Override
		public float getY() {
			return additive ? 0 : cube.getPosition().y;
		}

		@Override
		public float getZ() {
			return additive ? 0 : cube.getPosition().z;
		}

		@Override
		public InterpolationInfo getXInfo() {
			return InterpolatorChannel.POS_X;
		}

		@Override
		public InterpolationInfo getYInfo() {
			return InterpolatorChannel.POS_Y;
		}

		@Override
		public InterpolationInfo getZInfo() {
			return InterpolatorChannel.POS_Z;
		}

		@Override
		public String toString() {
			return "Position: " + cubeId;
		}
	}

	public static class CubeRotDriver extends CubeFloatDriver {

		public CubeRotDriver(int cubeId, boolean additive) {
			super(cubeId, additive);
		}

		@Override
		public void set(float x, float y, float z) {
			cube.setRotation(additive, x, y, z);
		}

		@Override
		public float getX() {
			return additive ? 0 : cube.getRotation().x;
		}

		@Override
		public float getY() {
			return additive ? 0 : cube.getRotation().y;
		}

		@Override
		public float getZ() {
			return additive ? 0 : cube.getRotation().z;
		}

		@Override
		public InterpolationInfo getXInfo() {
			return InterpolatorChannel.ROT_X;
		}

		@Override
		public InterpolationInfo getYInfo() {
			return InterpolatorChannel.ROT_Y;
		}

		@Override
		public InterpolationInfo getZInfo() {
			return InterpolatorChannel.ROT_Z;
		}

		@Override
		public String toString() {
			return "Rotation: " + cubeId;
		}
	}

	public static class CubeColorDriver extends CubeFloatDriver {

		public CubeColorDriver(int cubeId, boolean additive) {
			super(cubeId, additive);
		}

		@Override
		public void set(float x, float y, float z) {
			cube.setColor(x, y, z);
		}

		@Override
		public float getX() {
			return (cube.getRGB() & 0xff0000) >> 16;
		}

		@Override
		public float getY() {
			return (cube.getRGB() & 0x00ff00) >> 8;
		}

		@Override
		public float getZ() {
			return cube.getRGB() & 0x0000ff;
		}

		@Override
		public InterpolationInfo getXInfo() {
			return InterpolatorChannel.COLOR_R;
		}

		@Override
		public InterpolationInfo getYInfo() {
			return InterpolatorChannel.COLOR_G;
		}

		@Override
		public InterpolationInfo getZInfo() {
			return InterpolatorChannel.COLOR_B;
		}

		@Override
		public String toString() {
			return "Color: " + cubeId;
		}
	}

	public static class CubeScaleDriver extends CubeFloatDriver {

		public CubeScaleDriver(int cubeId, boolean additive) {
			super(cubeId, additive);
		}

		@Override
		public void set(float x, float y, float z) {
			cube.setRenderScale(additive, x, y, z);
		}

		@Override
		public float getX() {
			return additive ? 0 : cube.getRenderScale().x;
		}

		@Override
		public float getY() {
			return additive ? 0 : cube.getRenderScale().y;
		}

		@Override
		public float getZ() {
			return additive ? 0 : cube.getRenderScale().z;
		}

		@Override
		public InterpolationInfo getXInfo() {
			return InterpolatorChannel.SCALE_X;
		}

		@Override
		public InterpolationInfo getYInfo() {
			return InterpolatorChannel.SCALE_Y;
		}

		@Override
		public InterpolationInfo getZInfo() {
			return InterpolatorChannel.SCALE_Z;
		}

		@Override
		public String toString() {
			return "Scale: " + cubeId;
		}
	}

	public static class CubeVisDriver implements PartAnimationDriver {
		private final int cubeId;
		private RenderedCube cube;

		public CubeVisDriver(int cubeId) {
			this.cubeId = cubeId;
		}

		@Override
		public void set(float value) {
			cube.setVisible(value > 0.5f);
		}

		@Override
		public InterpolationInfo getInterpolationInfo() {
			return InterpolationInfo.NULL;
		}

		@Override
		public void init(ModelDefinition def) {
			cube = def.getElementById(cubeId);
		}

		@Override
		public String toString() {
			return "Vis: " + cubeId;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Channel ");
		sb.append(part);
		return sb.toString();
	}
}