package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationTrigger;
import com.tom.cpm.shared.animation.AnimationTrigger.GestureTrigger;
import com.tom.cpm.shared.animation.AnimationTrigger.LayerTrigger;
import com.tom.cpm.shared.animation.AnimationTrigger.ValueTrigger;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.io.IOHelper;

public class SerializedTrigger {
	public static final int LAYER_CTRL   = 1 << 0;
	public static final int LOOPING      = 1 << 1;
	public static final int BITMASK      = 1 << 2;
	public static final int PARAM_INTERPOLATE      = 1 << 3;
	public static final int MUST_FINISH      = 1 << 4;

	private boolean init;
	public StageType stage;
	public int stagingID = -1;
	public AnimationType anim;
	public VanillaPose pose;
	public boolean looping;
	public boolean layerCtrl;
	public int parameter = -1;
	public int value;
	public int gid = -1;
	public boolean bitMask;
	public boolean parameterInterpolate;
	public boolean mustFinish;

	public static void newTrigger(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger t = new SerializedTrigger();
		state.newTrigger(t);
	}

	public static void initBuiltin(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger t = state.getTrigger();
		t.pose = block.readEnum(VanillaPose.values());
		t.anim = AnimationType.POSE;
		int flags = block.read();
		t.mustFinish = (flags & MUST_FINISH) != 0;
		t.looping = true;
		t.init = true;
	}

	public static void initNamed(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger cT = state.getTrigger();
		cT.anim = block.readEnum(AnimationType.VALUES);
		if (cT.anim == AnimationType.GESTURE)cT.parameter = 1;
		else cT.parameter = 0;
		int flags = block.read();
		cT.layerCtrl = (flags & LAYER_CTRL) != 0;
		cT.looping = (flags & LOOPING) != 0;
		cT.mustFinish = (flags & MUST_FINISH) != 0;
		cT.value = block.readUnsignedByte();
		if (cT.layerCtrl)cT.gid = block.read();
		cT.init = true;
	}

	public static void initParameter(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger cT = state.getTrigger();
		cT.anim = block.readEnum(AnimationType.VALUES);
		int flags = block.read();
		cT.looping = (flags & LOOPING) != 0;
		cT.bitMask = (flags & BITMASK) != 0;
		cT.parameterInterpolate = (flags & PARAM_INTERPOLATE) != 0;
		cT.mustFinish = (flags & MUST_FINISH) != 0;
		cT.parameter = block.readVarInt();
		cT.value = block.readUnsignedByte();
		cT.init = true;
	}

	public static void initStaged(IOHelper block, AnimLoaderState state) throws IOException {
		SerializedTrigger cT = state.getTrigger();
		cT.anim = AnimationType.SETUP;
		cT.stage = block.readEnum(StageType.values());
		cT.stagingID = block.readVarInt();
		int flags = block.read();
		cT.mustFinish = (flags & MUST_FINISH) != 0;
		cT.init = true;
	}

	public void write(IOHelper dout) throws IOException {
		try (IOHelper d = dout.writeNextObjectBlock(TagType.NEW_TRIGGER)) {
		}
		if (this.pose != null) {
			try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_BUILTIN_TRIGGER)) {
				d.writeEnum(this.pose);
				int flags = 0;
				if (this.mustFinish)flags |= MUST_FINISH;
				d.write(flags);
			}
		} else if (anim == AnimationType.POSE || anim == AnimationType.GESTURE) {
			try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_NAMED_TRIGGER)) {
				d.writeEnum(this.anim);
				int flags = 0;
				if (this.bitMask)flags |= BITMASK;
				if (this.looping)flags |= LOOPING;
				if (this.layerCtrl)flags |= LAYER_CTRL;
				if (this.mustFinish)flags |= MUST_FINISH;
				d.write(flags);
				d.write(value);
				if (this.layerCtrl)
					d.write(gid);
			}
		} else if (parameter != -1) {
			try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_PARAMETER_TRIGGER)) {
				d.writeEnum(this.anim);
				int flags = 0;
				if (this.bitMask)flags |= BITMASK;
				if (this.looping)flags |= LOOPING;
				if (this.parameterInterpolate)flags |= PARAM_INTERPOLATE;
				if (this.mustFinish)flags |= MUST_FINISH;
				d.write(flags);
				d.writeVarInt(parameter);
				d.write(value);
			}
		} else if (stage != null) {
			try (IOHelper d = dout.writeNextObjectBlock(TagType.INIT_STAGED_TRIGGER)) {
				d.writeEnum(this.stage);
				d.writeVarInt(this.stagingID);
				int flags = 0;
				if (this.mustFinish)flags |= MUST_FINISH;
				d.write(flags);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anim == null) ? 0 : anim.hashCode());
		result = prime * result + stagingID;
		result = prime * result + parameter;
		result = prime * result + value;
		result = prime * result + (bitMask ? 1 : 0);
		result = prime * result + ((pose == null) ? 0 : pose.hashCode());
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
		result = prime * result + stagingID;
		result = prime * result + (mustFinish ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SerializedTrigger other = (SerializedTrigger) obj;
		if (anim != other.anim) return false;
		if (stagingID != other.stagingID) return false;
		if (stage != other.stage) return false;
		if (parameter != other.parameter) return false;
		if (value != other.value) return false;
		if (bitMask != other.bitMask) return false;
		if (mustFinish != other.mustFinish) return false;
		if (pose == null) {
			if (other.pose != null) return false;
		} else if (!pose.equals(other.pose)) return false;
		return true;
	}

	public AnimationTrigger compileStaging(List<IAnimation> animations) {
		return new AnimationTrigger(Collections.singleton(VanillaPose.GLOBAL), pose, animations, looping, mustFinish);
	}

	public AnimationTrigger compile(AnimationRegistry reg, List<IAnimation> animations) {
		IPose p;
		switch (anim) {
		case CUSTOM_POSE:
			p = reg.getPoseById(value, null);
			break;

		case POSE:
			p = pose;
			break;

		case SETUP:
		case FINISH:
			return null;

		case LAYER:
			return new LayerTrigger(Collections.singleton(VanillaPose.GLOBAL), animations, parameter, value, bitMask, mustFinish);

		case VALUE_LAYER:
			return new ValueTrigger(Collections.singleton(VanillaPose.GLOBAL), animations, parameter, parameterInterpolate);

		case GESTURE:
			return new GestureTrigger(Collections.singleton(VanillaPose.GLOBAL), animations, value, gid, looping, mustFinish);

		default:
			p = VanillaPose.GLOBAL;
			break;
		}

		if (p == null)return null;

		return new AnimationTrigger(Collections.singleton(p), pose, animations, looping, mustFinish);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Trigger");
		if (this.pose != null) {
			sb.append(" Pose ");
			sb.append(pose.name());
		} else if (parameter != -1) {
			sb.append(" ");
			sb.append(anim.name());
			sb.append("\n\tParameter: ");
			sb.append(parameter);
			if(bitMask)sb.append(" & ");
			else sb.append(" == ");
			sb.append(value);
			if(bitMask)sb.append(" == 0");
			sb.append(" ");
			sb.append(looping);
		} else if (stage != null) {
			sb.append(" ");
			sb.append(stage.name());
			sb.append(" ");
			sb.append(stagingID);
			sb.append(" Staged");
		}
		return sb.toString();
	}
}