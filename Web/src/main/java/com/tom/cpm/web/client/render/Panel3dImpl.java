package com.tom.cpm.web.client.render;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Quaternion;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.gui.panel.Panel3d.Panel3dNative;
import com.tom.cpm.shared.model.render.RenderMode;
import com.tom.cpm.web.client.WebMC.Texture;

public class Panel3dImpl extends Panel3dNative {

	public Panel3dImpl(Panel3d panel) {
		super(panel);
	}

	@Override
	public void render(float partialTicks) {
		RenderSystem.setProj3d(true);
		MatrixStack stack = new MatrixStack();
		stack.translate(0, 0, -2);

		float f = 0.02f;
		stack.scale(f, -f, -0.001f);

		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();
		float size = cam.camDist;

		stack.scale(size, size, size);
		Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
		Quaternion quaternion1 = Vec3f.POSITIVE_X.getRadialQuaternion(-pitch);
		quaternion.mul(quaternion1);
		stack.rotate(quaternion);

		stack.rotate(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (yaw + Math.PI)));
		stack.translate(-cam.position.x, -cam.position.y, -cam.position.z);

		stack.push();

		panel.render(stack, RenderSystem.buffers, partialTicks);
		RenderSystem.setProj3d(false);
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes0(Texture.bound.getId());
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String tex) {
		return getRenderTypes0("assets/cpm/textures/gui/" + tex + ".png");
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		Image img = RenderSystem.screenshot(new Box(renderPos.x, renderPos.y, size.x, size.y));
		Image rImg = new Image(size.x, size.y);
		rImg.draw(img, 0, 0, size.x, size.y);
		return rImg;
	}

	@Override
	public void renderItem(MatrixStack stack, ItemSlot hand, DisplayItem item) {}

	@Override
	public Mat4f getView() {
		float f = 0.25f;
		return Mat4f.makeScale(f, f, f);
	}

	@Override
	public Mat4f getProjection() {
		return RenderSystem.proj;
	}
}
