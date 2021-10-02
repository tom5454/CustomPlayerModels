package com.tom.cpm.client;

import static org.lwjgl.opengl.GL11.glColor3f;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.MatrixStack;
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
	private Minecraft mc;

	public Panel3dImpl(Panel3d panel) {
		super(panel);
		mc = Minecraft.getMinecraft();
	}

	@Override
	public void render(float partialTicks) {
		ViewportCamera cam = panel.getCamera();
		float pitch = (float) Math.asin(cam.look.y);
		float yaw = cam.look.getYaw();

		GlStateManager.enableAlpha();
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		try {
			Box bounds = getBounds();
			Vec2i off = panel.getGui().getOffset();
			GlStateManager.translate(off.x + bounds.w / 2, off.y + bounds.h / 2, 50);
			float scale = cam.camDist;
			GlStateManager.scale((-scale), scale, 0.1f);
			GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate((float) Math.toDegrees(pitch), 1, 0, 0);
			GlStateManager.rotate((float) Math.toDegrees(yaw), 0, 1, 0);
			GlStateManager.translate(-cam.position.x, -cam.position.y, -cam.position.z);
			float f = 1.0f;
			glColor3f(f, f, f);

			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			GlStateManager.enableDepth();
			GlStateManager.disableCull();

			panel.render(new MatrixStack(), new VBuffers(RetroGL::buffer), partialTicks);
		} finally {
			GlStateManager.disableDepth();
			GlStateManager.popMatrix();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableRescaleNormal();
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.disableTexture2D();
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		}
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes() {
		return getRenderTypes(DynTexture.getBoundLoc());
	}

	@Override
	public RenderTypes<RenderMode> getRenderTypes(String tex) {
		return getRenderTypes(new ResourceLocation("cpm", "textures/gui/" + tex + ".png"));
	}

	public RenderTypes<RenderMode> getRenderTypes(ResourceLocation tex) {
		RenderTypes<RenderMode> renderTypes = new RenderTypes<>(RenderMode.class);
		renderTypes.put(RenderMode.NORMAL, new NativeRenderType(RetroGL.texture(tex), 0));
		renderTypes.put(RenderMode.DEFAULT, new NativeRenderType(RetroGL.texture(tex), 0));
		renderTypes.put(RenderMode.GLOW, new NativeRenderType(RetroGL.eyes(tex), 1));
		renderTypes.put(RenderMode.OUTLINE, new NativeRenderType(RetroGL.linesNoDepth(), 2));
		renderTypes.put(RenderMode.COLOR, new NativeRenderType(RetroGL.color(), 0));
		renderTypes.put(RenderMode.PAINT, new NativeRenderType(RetroGL.paint(tex), 0));
		return renderTypes;
	}

	@Override
	public int getColorUnderMouse() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
		int colorUnderMouse = (((int)(buffer.get(0) * 255)) << 16) | (((int)(buffer.get(1) * 255)) << 8) | ((int)(buffer.get(2) * 255));
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		return colorUnderMouse;
	}

	@Override
	public Image takeScreenshot(Vec2i size) {
		GuiImpl gui = (GuiImpl) panel.getGui();
		float multiplierX = mc.displayWidth / (float)gui.width;
		float multiplierY = mc.displayHeight / (float)gui.height;
		int width = (int) (multiplierX * size.x);
		int height = (int) (multiplierY * size.y);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 3);
		GL11.glReadPixels((int) (multiplierX * renderPos.x), mc.displayHeight - height - ((int) (multiplierY * renderPos.y)), width, height, GL11.GL_RGB, GL11.GL_FLOAT, buffer);
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
	public void renderItem(MatrixStack stack, ItemSlot hand, DisplayItem item) {
		renderItem(stack, getHandStack(item), hand);
	}

	private void renderItem(MatrixStack stack, ItemStack itemstack, ItemSlot hand) {
		if (!itemstack.isEmpty()) {
			GlStateManager.pushMatrix();

			PlayerRenderManager.multiplyStacks(stack.getLast());
			ItemCameraTransforms.TransformType view = ItemCameraTransforms.TransformType.FIXED;
			boolean flag = false;
			if(hand == ItemSlot.LEFT_HAND || hand == ItemSlot.RIGHT_HAND) {
				GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				flag = hand == ItemSlot.LEFT_HAND;
				view = flag ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
				GlStateManager.translate((flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
			} else if(hand == ItemSlot.HEAD) {
				Item item = itemstack.getItem();
				if (item == Items.SKULL) {
					GlStateManager.scale(1.1875F, -1.1875F, -1.1875F);
					TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F, itemstack.getMetadata(), null, -1, 0);
					GlStateManager.popMatrix();
					return;
				} else {
					GlStateManager.translate(0.0F, -0.25F, 0.0F);
					GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
					GlStateManager.scale(0.625F, -0.625F, -0.625F);
					view = ItemCameraTransforms.TransformType.HEAD;
				}
			}
			IBakedModel ibakedmodel = mc.getRenderItem().getItemModelWithOverrides(itemstack, (World)null, (EntityLivingBase)null);
			renderItemModel(itemstack, ibakedmodel, view, flag);
			GlStateManager.popMatrix();
		}
	}

	/** Copy of {@link net.minecraft.client.renderer.RenderItem#renderItemModel}*/
	private void renderItemModel(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
		if (!stack.isEmpty()) {
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableRescaleNormal();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.pushMatrix();
			// TODO: check if negative scale is a thing
			bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, transform, leftHanded);

			mc.getRenderItem().renderItem(stack, bakedmodel);
			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
	}

	private ItemStack getHandStack(DisplayItem item) {
		switch (item) {
		case BLOCK:
			return new ItemStack(Blocks.STONE);
		case NONE:
			break;
		case SWORD:
			return new ItemStack(Items.DIAMOND_SWORD);
		case SKULL:
			return new ItemStack(Items.SKULL, 2);
		default:
			break;
		}
		return ItemStack.EMPTY;
	}
}
