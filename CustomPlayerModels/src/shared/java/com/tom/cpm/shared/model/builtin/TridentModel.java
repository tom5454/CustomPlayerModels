package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;

public class TridentModel extends SimpleModel implements IItemModel {
	private final SimplePartRenderer pole;

	public TridentModel() {
		textureWidth = 32;
		textureHeight = 32;
		pole = new SimplePartRenderer(this, 0, 6);
		this.pole.addBox(-0.5F, 2.0F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F);
		SimplePartRenderer modelrenderer = new SimplePartRenderer(this, 4, 0);
		modelrenderer.addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F);
		this.pole.addChild(modelrenderer);
		SimplePartRenderer modelrenderer1 = new SimplePartRenderer(this, 4, 3);
		modelrenderer1.addBox(-2.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
		this.pole.addChild(modelrenderer1);
		SimplePartRenderer modelrenderer2 = new SimplePartRenderer(this, 0, 0);
		modelrenderer2.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F);
		this.pole.addChild(modelrenderer2);
		SimplePartRenderer modelrenderer3 = new SimplePartRenderer(this, 4, 3);
		modelrenderer3.mirror = true;
		modelrenderer3.addBox(1.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
		this.pole.addChild(modelrenderer3);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		pole.render(stack, buf);
	}

	@Override
	public String getTexture() {
		return "trident";
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf, ItemSlot inSlot) {
		stack.push();
		stack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(-90F));
		stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90F));
		float z = inSlot == ItemSlot.LEFT_HAND ? -0.05F : 0.05F;
		stack.translate(0.5F, -1F, z);
		render(stack, buf);
		stack.pop();
	}
}
