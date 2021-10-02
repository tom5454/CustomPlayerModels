package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.animation.AnimatedTexture;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.anim.AnimatedTex;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.TextureSheetType;

public class ModelPartAnimatedTexture implements IModelPart, IResolvedModelPart {
	private TextureSheetType sheet;
	private Vec2i uvStart, uvSize;
	private Vec2i animStart;
	private int frameTime;
	private int frameCount;
	private boolean anX, interpolate;

	public ModelPartAnimatedTexture(IOHelper in, ModelDefinition def) throws IOException {
		sheet = in.readEnum(TextureSheetType.VALUES);
		uvStart = in.read2v();
		uvSize = in.read2v();
		animStart = in.read2v();
		frameCount = in.readVarInt();
		frameTime = in.readVarInt();
		int flags = in.readByte();
		anX = (flags & 1) != 0;
		interpolate = (flags & 2) != 0;
	}

	public ModelPartAnimatedTexture(TextureSheetType sheet, AnimatedTex tex) {
		this.sheet = sheet;
		uvStart = tex.uvStart;
		uvSize = tex.uvSize;
		animStart = tex.animStart;
		frameCount = tex.frameCount;
		frameTime = tex.frameTime;
		anX = tex.anX;
		interpolate = tex.interpolate;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeEnum(sheet);
		dout.write2v(uvStart);
		dout.write2v(uvSize);
		dout.write2v(animStart);
		dout.writeVarInt(frameCount);
		dout.writeVarInt(frameTime);
		int flags = 0;
		if(anX)flags |= 1;
		if(interpolate)flags |= 2;
		dout.writeByte(flags);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.ANIMATED_TEX;
	}

	@Override
	public void apply(ModelDefinition def) {
		if(def.getPlayerObj().isClientPlayer() || ConfigKeys.ENABLE_ANIMATED_TEXTURES.getValueFor(def.getPlayerObj())) {
			def.getAnimations().addAnimatedTexture(new AnimatedTexture(def, sheet, uvStart, uvSize, animStart, frameCount, frameTime, anX, interpolate));
		}
	}
}
