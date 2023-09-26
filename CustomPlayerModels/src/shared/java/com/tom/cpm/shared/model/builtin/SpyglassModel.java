package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;

public class SpyglassModel extends SimpleModel implements IItemModel {
	private SimplePartRenderer base;

	public SpyglassModel() {
		textureWidth = 16;
		textureHeight = 16;
		base = new SimplePartRenderer(this, 0, 0);
		base.setPos(0.0F, 24.0F, 0.0F);
		base.addBox(-1.0F, -11.0F, -1.0F, 2, 5, 2, 0.0F);

		SimplePartRenderer top = new SimplePartRenderer(this, 0, 7);
		base.addChild(top);
		top.addBox(-1.0F, -6.0F, -1.0F, 2, 6, 2, 0.1F);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		base.render(stack, buf);
	}

	@Override
	public String getTexture() {
		return "spyglass";
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf, ItemSlot inSlot) {
		stack.push();
		stack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(-90f));
		float x = inSlot == ItemSlot.LEFT_HAND ? 0.1F : -0.1F;
		stack.translate(x, -0.56F, -0.2F);
		render(stack, buf);
		stack.pop();
	}
}
