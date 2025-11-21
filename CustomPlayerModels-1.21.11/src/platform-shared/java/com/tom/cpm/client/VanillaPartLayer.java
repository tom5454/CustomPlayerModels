package com.tom.cpm.client;

import java.util.function.Function;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class VanillaPartLayer extends Model<PartPose> {

	public VanillaPartLayer(ModelPart p_368583_, Function<Identifier, RenderType> p_103110_) {
		super(p_368583_, p_103110_);
	}

	@Override
	public void setupAnim(PartPose p_435637_) {
		super.setupAnim(p_435637_);
		root().loadPose(p_435637_);
	}
}
