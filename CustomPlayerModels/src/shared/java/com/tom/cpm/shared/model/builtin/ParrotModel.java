package com.tom.cpm.shared.model.builtin;


import com.google.common.collect.ImmutableList;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.render.VertexBuffer;

public class ParrotModel extends SimpleModel {
	private final SimplePartRenderer body;
	private final SimplePartRenderer tail;
	private final SimplePartRenderer wingLeft;
	private final SimplePartRenderer wingRight;
	private final SimplePartRenderer head;
	private final SimplePartRenderer head2;
	private final SimplePartRenderer beak1;
	private final SimplePartRenderer beak2;
	private final SimplePartRenderer feather;
	private final SimplePartRenderer legLeft;
	private final SimplePartRenderer legRight;

	public ParrotModel() {
		this.textureWidth = 32;
		this.textureHeight = 32;
		this.body = new SimplePartRenderer(this, 2, 8);
		this.body.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F);
		this.body.setPos(0.0F, 16.5F, -3.0F);
		this.tail = new SimplePartRenderer(this, 22, 1);
		this.tail.addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 1.0F);
		this.tail.setPos(0.0F, 21.07F, 1.16F);
		this.wingLeft = new SimplePartRenderer(this, 19, 8);
		this.wingLeft.addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F);
		this.wingLeft.setPos(1.5F, 16.94F, -2.76F);
		this.wingRight = new SimplePartRenderer(this, 19, 8);
		this.wingRight.addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F);
		this.wingRight.setPos(-1.5F, 16.94F, -2.76F);
		this.head = new SimplePartRenderer(this, 2, 2);
		this.head.addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F);
		this.head.setPos(0.0F, 15.69F, -2.76F);
		this.head2 = new SimplePartRenderer(this, 10, 0);
		this.head2.addBox(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F);
		this.head2.setPos(0.0F, -2.0F, -1.0F);
		this.head.addChild(this.head2);
		this.beak1 = new SimplePartRenderer(this, 11, 7);
		this.beak1.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F);
		this.beak1.setPos(0.0F, -0.5F, -1.5F);
		this.head.addChild(this.beak1);
		this.beak2 = new SimplePartRenderer(this, 16, 7);
		this.beak2.addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
		this.beak2.setPos(0.0F, -1.75F, -2.45F);
		this.head.addChild(this.beak2);
		this.feather = new SimplePartRenderer(this, 2, 18);
		this.feather.addBox(0.0F, -4.0F, -2.0F, 0.0F, 5.0F, 4.0F);
		this.feather.setPos(0.0F, -2.15F, 0.15F);
		this.head.addChild(this.feather);
		this.legLeft = new SimplePartRenderer(this, 14, 18);
		this.legLeft.addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
		this.legLeft.setPos(1.0F, 22.0F, -1.05F);
		this.legRight = new SimplePartRenderer(this, 14, 18);
		this.legRight.addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
		this.legRight.setPos(-1.0F, 22.0F, -1.05F);

		this.feather.xRot = -0.2214F;
		this.body.xRot = 0.4937F;
		this.wingLeft.xRot = -0.6981F;
		this.wingLeft.yRot = -(float)Math.PI;
		this.wingRight.xRot = -0.6981F;
		this.wingRight.yRot = -(float)Math.PI;
		this.legLeft.xRot = -0.0299F;
		this.legRight.xRot = -0.0299F;
		this.legLeft.y = 22.0F;
		this.legRight.y = 22.0F;
		this.legLeft.zRot = 0.0F;
		this.legRight.zRot = 0.0F;
		this.head.zRot = 0.0F;
		this.head.x = 0.0F;
		this.body.x = 0.0F;
		this.tail.x = 0.0F;
		this.wingRight.x = -1.5F;
		this.wingLeft.x = 1.5F;
		this.head.y = 15.69F;
		this.tail.xRot = 1.015F;
		this.tail.y = 21.07F;
		this.body.y = 16.5F;
		this.wingLeft.zRot = -0.0873F;
		this.wingLeft.y = 16.94F;
		this.wingRight.zRot = 0.0873F;
		this.wingRight.y = 16.94F;
	}

	public Iterable<VanillaPartRenderer> parts() {
		return ImmutableList.of(this.body, this.wingLeft, this.wingRight, this.tail, this.head, this.legLeft, this.legRight);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		parts().forEach(p -> p.render(stack, buf));
	}

	@Override
	public String getTexture() {
		return "parrot";
	}
}
