package com.tom.cpm.shared.parts.anim;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tom.cpl.function.FloatUnaryOperator;
import com.tom.cpm.shared.animation.AnimationNew.AnimationDriver;
import com.tom.cpm.shared.animation.AnimationNew.PartAnimationDriver;
import com.tom.cpm.shared.animation.InterpolationInfo;
import com.tom.cpm.shared.definition.ModelDefinition;

public class Float3Driver implements AnimationDriver {
	private FloatUnaryOperator x, y, z;
	private Float3Consumer setter;

	public Float3Driver(Float3Consumer f3, AnimatorChannel dx, AnimatorChannel dy, AnimatorChannel dz) {
		this.setter = f3;
		x = dx.frameData != null ? dx.frameData.createDriver(f3.getXInfo()) : __ -> f3.getX();
		y = dy.frameData != null ? dy.frameData.createDriver(f3.getYInfo()) : __ -> f3.getY();
		z = dz.frameData != null ? dz.frameData.createDriver(f3.getZInfo()) : __ -> f3.getZ();
	}

	@Override
	public void set(float value) {
		setter.set(x.apply(value), y.apply(value), z.apply(value));
	}

	public static interface Float3Consumer {
		void set(float x, float y, float z);
		float getX();
		float getY();
		float getZ();
		InterpolationInfo getXInfo();
		InterpolationInfo getYInfo();
		InterpolationInfo getZInfo();
		void init(ModelDefinition def);
	}

	private static class PartDriver implements PartAnimationDriver {
		public Float3Consumer f3;
		public Float3Driver fd;
		public Consumer<ModelDefinition> init;
		public String chn;

		public PartDriver(Float3Consumer f3, String chn) {
			this.f3 = f3;
			this.chn = chn;
		}

		@Override
		public void set(float value) {
		}

		@Override
		public InterpolationInfo getInterpolationInfo() {
			return InterpolationInfo.NULL;
		}

		@Override
		public void init(ModelDefinition def) {
			if (init != null) {
				f3.init(def);
				init.accept(def);
				init = null;
			}
		}

		@Override
		public AnimationDriver makeDriver(FloatUnaryOperator frameDriver) {
			return fd;
		}

		@Override
		public String toString() {
			return "F3: " + chn + " " + f3.toString();
		}
	}

	public static <T> void make(Float3Consumer f3, T x, T y, T z, BiConsumer<T, AnimatorChannel> registerChn) {
		PartDriver d = new PartDriver(f3, "X");
		AnimatorChannel chX = new AnimatorChannel(d);
		AnimatorChannel chY = new AnimatorChannel(new PartDriver(f3, "Y"));
		AnimatorChannel chZ = new AnimatorChannel(new PartDriver(f3, "Z"));
		d.init = def -> {
			if (chX.frameData != null || chY.frameData != null || chZ.frameData != null)
				d.fd = new Float3Driver(f3, chX, chY, chZ);
		};
		registerChn.accept(x, chX);
		registerChn.accept(y, chY);
		registerChn.accept(z, chZ);
	}

	public static Float3Consumer getFromPart(PartAnimationDriver p) {
		if (p instanceof PartDriver) {
			PartDriver pd = (PartDriver) p;
			return pd.f3;
		}
		return null;
	}
}
