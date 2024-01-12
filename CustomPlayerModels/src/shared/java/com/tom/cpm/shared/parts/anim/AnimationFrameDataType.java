package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.List;

import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.SerializedAnimation.AnimFrame;

public enum AnimationFrameDataType {
	CONSTANT_FRAME_FLOAT(ConstantTimeFloat::write),
	CONSTANT_FRAME_BOOL(ConstantTimeBool::write),
	;

	private final Writer<?> writer;

	private AnimationFrameDataType(Writer writer) {
		this.writer = writer;
	}

	public static interface Writer<T extends AnimationFrameData> {
		void write(List<AnimFrame<T>> frames, IOHelper h) throws IOException;
	}

	public void write(List<AnimFrame<?>> value, IOHelper dout) throws IOException {
		writer.write((List) value, dout);
	}
}
