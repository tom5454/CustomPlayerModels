package com.tom.cpm.shared.model.builtin;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.model.render.BoxRender;
import com.tom.cpm.shared.model.render.Mesh;

public class ItemModel extends SimpleModel implements IItemModel {
	private final String texture;
	private Mesh mesh;
	private ItemRenderTransform[] renderTransform;

	public ItemModel(String texture, ItemRenderTransform[] renderTransform) {
		this.texture = texture;
		this.renderTransform = renderTransform;

		mesh = BoxRender.createTexturedExtruded(new Vec3f(), new Vec3f(16, 16, 1), new Vec3f(1, 1, 1), 0, 0, 0, 1, 16, 16);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf) {
		mesh.draw(stack, buf, 1, 1, 1, 1);
	}

	@Override
	public void render(MatrixStack stack, VertexBuffer buf, ItemSlot inSlot) {
		stack.push();
		boolean flag = inSlot == ItemSlot.LEFT_HAND;
		stack.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
		stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
		stack.translate((flag ? -1 : 1) / 16.0F, 0.125D, -0.625D);
		if(renderTransform.length == 1) {
			renderTransform[0].apply(stack);
		} else {
			renderTransform[flag ? 1 : 0].apply(stack);
			if(flag) {
				stack.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
			}
		}
		stack.translate(-0.5D, -0.5D, 0);
		render(stack, buf);
		stack.pop();
	}

	@Override
	public String getTexture() {
		return texture;
	}
}
