package com.tom.cpm.client;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.render.RenderTypes;
import com.tom.cpl.render.VBuffers;
import com.tom.cpl.render.VBuffers.NativeRenderType;
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
	private Minecraft mc;

	public Panel3dImpl(Panel3d panel) {
		super(panel);
		mc = Minecraft.getInstance();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void render(float partialTicks) {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();
		Box bounds = getBounds();
		Vec2i off = panel.getGui().getOffset();
		float size = cam.camDist;

		RenderSystem.pushMatrix();
		RenderSystem.translatef(off.x + bounds.w / 2, off.y + bounds.h / 2, 600);
		RenderSystem.scalef(1.0F, 1.0F, -0.1F);
		matrixstack = new MatrixStack();
		matrixstack.translate(0.0D, 0.0D, 1000.0D);
		matrixstack.scale(size, size, size);
		Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
		Quaternion quaternion1 = Vector3f.XP.rotation(-pitch);
		quaternion.mul(quaternion1);
		matrixstack.mulPose(quaternion);

		matrixstack.mulPose(Vector3f.YP.rotation((float) (yaw + Math.PI)));
		matrixstack.translate(-cam.position.x, -cam.position.y, -cam.position.z);
		RenderSystem.enableDepthTest();

		IRenderTypeBuffer bufs = mc.renderBuffers().bufferSource();
		int light = LightTexture.pack(15, 15);
		panel.render(new com.tom.cpl.math.MatrixStack(), new VBuffers(rt -> new VBuffer(bufs.getBuffer(rt.getNativeType()), light, OverlayTexture.NO_OVERLAY, matrixstack)), partialTicks);
		mc.renderBuffers().bufferSource().endBatch();

		RenderSystem.disableDepthTest();
		RenderSystem.popMatrix();
		matrixstack = null;
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes(DynTexture.getBoundLoc());
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String texture) {
		return getRenderTypes(new ResourceLocation("cpm", "textures/gui/" + texture + ".png"));
	}

	public RenderTypes<RenderMode> getRenderTypes(ResourceLocation rl) {
		RenderTypes<RenderMode> renderTypes = new RenderTypes<>(RenderMode.class);
		renderTypes.put(RenderMode.NORMAL, new NativeRenderType(0));
		renderTypes.put(RenderMode.DEFAULT, new NativeRenderType(RenderType.entityTranslucent(rl), 0));
		renderTypes.put(RenderMode.PAINT, new NativeRenderType(CustomRenderTypes.getEntityTranslucentCullNoLight(rl), 0));
		renderTypes.put(RenderMode.GLOW, new NativeRenderType(RenderType.eyes(rl), 1));
		renderTypes.put(RenderMode.OUTLINE, new NativeRenderType(CustomRenderTypes.getLinesNoDepth(), 2));
		renderTypes.put(RenderMode.COLOR, new NativeRenderType(CustomRenderTypes.getEntityColorTranslucentCull(), 0));
		return renderTypes;
	}

	@Override
	public int getColorUnderMouse() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		GL11.glReadPixels((int) mc.mouseHandler.xpos(), mc.getWindow().getHeight() - (int) mc.mouseHandler.ypos(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		int colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		return colorUnderMouse;
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
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.getWindow().getHeight() - height - (int) (multiplierY * renderPos.y), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
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
			matrixstack.pushPose();
			PlayerRenderManager.multiplyStacks(stack.getLast(), matrixstack);
			boolean flag = false;
			ItemCameraTransforms.TransformType view = ItemCameraTransforms.TransformType.FIXED;
			if(hand == ItemSlot.LEFT_HAND || hand == ItemSlot.RIGHT_HAND) {
				matrixstack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
				matrixstack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
				flag = hand == ItemSlot.LEFT_HAND;
				view = flag ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
				matrixstack.translate((flag ? -1 : 1) / 16.0F, 0.125D, -0.625D);
			} else if(hand == ItemSlot.HEAD) {
				Item item = itemstack.getItem();
				if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
					matrixstack.scale(1.1875F, -1.1875F, -1.1875F);
					matrixstack.translate(-0.5D, 0.0D, -0.5D);
					SkullTileEntityRenderer.renderSkull(null, 180.0F, ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType(), null, 0, matrixstack, mc.renderBuffers().bufferSource(), LightTexture.pack(15, 15));
					matrixstack.popPose();
					return;
				} else {
					matrixstack.translate(0.0D, -0.25D, 0.0D);
					matrixstack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
					matrixstack.scale(0.625F, -0.625F, -0.625F);
					view = ItemCameraTransforms.TransformType.HEAD;
				}
			}
			mc.getItemRenderer().renderStatic(null, itemstack, view, flag, matrixstack, mc.renderBuffers().bufferSource(), null, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
			matrixstack.popPose();
		}
	}
}
