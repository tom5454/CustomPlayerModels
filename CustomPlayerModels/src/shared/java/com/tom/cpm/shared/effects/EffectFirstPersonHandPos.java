package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.PartPosition;

public class EffectFirstPersonHandPos implements IRenderEffect {
	private PartPosition leftHand, rightHand;

	public EffectFirstPersonHandPos() {
	}

	public EffectFirstPersonHandPos(PartPosition leftHand, PartPosition rightHand) {
		this.leftHand = leftHand;
		this.rightHand = rightHand;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		leftHand = new PartPosition();
		leftHand.setRenderScale(in.readVec6b(), in.readAngle(), in.readVec6b());

		rightHand = new PartPosition();
		rightHand.setRenderScale(in.readVec6b(), in.readAngle(), in.readVec6b());
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVec6b(leftHand.getRPos());
		out.writeAngle(leftHand.getRRotation());
		out.writeVec6b(leftHand.getRScale());

		out.writeVec6b(rightHand.getRPos());
		out.writeAngle(rightHand.getRRotation());
		out.writeVec6b(rightHand.getRScale());
	}

	@Override
	public void apply(ModelDefinition def) {
		def.fpLeftHand = leftHand;
		def.fpRightHand = rightHand;
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.FIRST_PERSON_HAND;
	}
}
