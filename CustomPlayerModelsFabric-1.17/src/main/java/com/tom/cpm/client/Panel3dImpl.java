package com.tom.cpm.client;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.util.Image;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.client.MinecraftObject.DynTexture;
import com.tom.cpm.shared.editor.DisplayItem;
import com.tom.cpm.shared.gui.ViewportCamera;
import com.tom.cpm.shared.gui.panel.Panel3d;
import com.tom.cpm.shared.gui.panel.Panel3d.Panel3dNative;
import com.tom.cpm.shared.model.render.RenderMode;

public class Panel3dImpl extends Panel3dNative {
	private MatrixStack matrixstack;
	private MinecraftClient mc;

	public Panel3dImpl(Panel3d panel) {
		super(panel);
		mc = MinecraftClient.getInstance();
	}

	@Override
	public void render(float partialTicks) {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();
		Box bounds = getBounds();
		Vec2i off = panel.getGui().getOffset();
		float size = cam.camDist;

		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		try {
			matrixStack.translate(off.x + bounds.w / 2, off.y + bounds.h / 2, 600.0D);//600
			matrixStack.scale(1.0F, 1.0F, -0.1F);
			RenderSystem.applyModelViewMatrix();
			matrixstack = new MatrixStack();
			matrixstack.translate(0.0D, 0.0D, 1000.0D);
			matrixstack.scale(size, size, size);
			Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
			Quaternion quaternion1 = Vec3f.POSITIVE_X.getRadialQuaternion(-pitch);
			quaternion.hamiltonProduct(quaternion1);
			matrixstack.multiply(quaternion);

			matrixstack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (yaw + Math.PI)));
			matrixstack.translate(-cam.position.x, -cam.position.y, -cam.position.z);
			RenderSystem.enableDepthTest();

			VertexConsumerProvider bufs = mc.getBufferBuilders().getEntityVertexConsumers();
			int light = LightmapTextureManager.pack(15, 15);
			panel.render(new com.tom.cpl.math.MatrixStack(), new VBuffers(rt -> new VBuffer(bufs.getBuffer(rt.getNativeType()), light, OverlayTexture.DEFAULT_UV, matrixstack)), partialTicks);
			mc.getBufferBuilders().getEntityVertexConsumers().draw();
		} finally {
			RenderSystem.disableDepthTest();
			matrixStack = RenderSystem.getModelViewStack();
			matrixStack.pop();
			RenderSystem.applyModelViewMatrix();
			matrixstack = null;
		}
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes0(DynTexture.getBoundLoc());
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String texture) {
		return getRenderTypes0(new Identifier("cpm", "textures/gui/" + texture + ".png"));
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		GuiImpl gui = (GuiImpl) panel.getGui();
		int dw = mc.getWindow().getWidth();
		int dh = mc.getWindow().getHeight();
		float multiplierX = dw / (float)gui.width;
		float multiplierY = dh / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.getWindow().getFramebufferHeight() - height - (int) (multiplierY * renderPos.y), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		Image img = new Image(width, height);
		for(int y = 0;y<height;y++) {
			for(int x = 0;x<width;x++) {
				float r = buffer.get((x + y * width) * 3);
				float g = buffer.get((x + y * width) * 3 + 1);
				float b = buffer.get((x + y * width) * 3 + 2);
				int color = 0xff000000 | (((int)(r * 255)) << 16) | (((int)(g * 255)) << 8) | ((int)(b * 255));
				img.setRGB(x, height - y - 1, color);
			}
		}
		Image rImg = new Image(size.x, size.y);
		rImg.draw(img, 0, 0, size.x, size.y);
		return rImg;
	}

	@Override
	public void renderItem(com.tom.cpl.math.MatrixStack stack, ItemSlot hand, DisplayItem item) {
		this.renderItem(stack, getHandStack(item), hand);
	}

	private ItemStack getHandStack(DisplayItem item) {
		switch (item) {
		case BLOCK:
			return new ItemStack(Blocks.STONE);
		case NONE:
			break;
		case SWORD:
			return new ItemStack(Items.NETHERITE_SWORD);
		case SKULL:
			return new ItemStack(Items.CREEPER_HEAD);
		default:
			break;
		}
		return ItemStack.EMPTY;
	}

	private void renderItem(com.tom.cpl.math.MatrixStack stack, ItemStack itemstack, ItemSlot hand) {
		if (!itemstack.isEmpty()) {
			matrixstack.push();
			PlayerRenderManager.multiplyStacks(stack.getLast(), matrixstack);
			boolean flag = false;
			ModelTransformation.Mode view = ModelTransformation.Mode.FIXED;
			if(hand == ItemSlot.LEFT_HAND || hand == ItemSlot.RIGHT_HAND) {
				matrixstack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
				matrixstack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
				flag = hand == ItemSlot.LEFT_HAND;
				view = flag ? ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND : ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND;
				matrixstack.translate((flag ? -1 : 1) / 16.0F, 0.125D, -0.625D);
			} else if(hand == ItemSlot.HEAD) {
				Item item = itemstack.getItem();
				if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
					/*matrixstack.scale(1.1875F, -1.1875F, -1.1875F);
					matrixstack.translate(-0.5D, 0.0D, -0.5D);

					SkullBlock.Type var20 = ((AbstractSkullBlock) ((BlockItem) item).getBlock()).getType();
					SkullModelBase var17 = (SkullModelBase) this.skullModels.get(var20);
					RenderType var18 = SkullBlockRenderer.getRenderType(var20, null);
					SkullBlockRenderer.renderSkull((Direction) null, 180.0F, 0, matrixstack, mc.renderBuffers().bufferSource(), LightTexture.pack(15, 15),
							var17, var18);*///TODO render skull
					matrixstack.pop();
					return;
				} else {
					matrixstack.translate(0.0D, -0.25D, 0.0D);
					matrixstack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
					matrixstack.scale(0.625F, -0.625F, -0.625F);
					view = ModelTransformation.Mode.HEAD;
				}
			}
			mc.getItemRenderer().renderItem(null, itemstack, view, flag, matrixstack, mc.getBufferBuilders().getEntityVertexConsumers(), null, LightmapTextureManager.pack(15, 15), OverlayTexture.DEFAULT_UV, 0);
			matrixstack.pop();
		}
	}

	@Override
	public Mat4f getView() {
		return Mat4f.map(RenderSystem.getModelViewMatrix(), matrixstack.peek().getModel(), Matrix4f::writeColumnMajor);
	}

	@Override
	public Mat4f getProjection() {
		return Mat4f.map(RenderSystem.getProjectionMatrix(), Matrix4f::writeColumnMajor);
	}
}
