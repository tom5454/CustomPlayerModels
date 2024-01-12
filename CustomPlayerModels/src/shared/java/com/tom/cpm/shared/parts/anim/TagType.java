package com.tom.cpm.shared.parts.anim;

import java.io.IOException;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.IOHelper.ObjectReader;

public enum TagType {
	END((a, b) -> {}),
	NEW_ANIM(SerializedAnimation::newAnimation),
	NEW_TRIGGER(SerializedTrigger::newTrigger),
	INIT_BUILTIN_TRIGGER(SerializedTrigger::initBuiltin),
	INIT_NAMED_TRIGGER(SerializedTrigger::initNamed),
	INIT_ANIM_STAGED_TRIGGER(SerializedTrigger::initAnimStaged),
	INIT_TRIGGER_STAGED_TRIGGER(SerializedTrigger::initTriggerStaged),
	CONSTANT_FRAME_TIME_FLOAT(ConstantTimeFloat::parse),
	CONSTANT_FRAME_TIME_BOOLEAN(ConstantTimeBool::parse),
	CUBES_TO_CHANNELS(SerializedAnimation::addCubesToChannels),
	CONTROL_INFO(AnimLoaderState::parseInfo),
	;
	public static final TagType[] VALUES = values();
	private final Handler handler;

	private TagType(Handler handler) {
		this.handler = handler;
	}

	@FunctionalInterface
	public static interface Handler {
		void load(IOHelper din, AnimLoaderState state) throws IOException;
	}

	public static ObjectReader<TagType, TagType> read(AnimLoaderState state) throws IOException {
		return (t, din) -> {
			t.handler.load(din, state);
			return t;
		};
	}
}