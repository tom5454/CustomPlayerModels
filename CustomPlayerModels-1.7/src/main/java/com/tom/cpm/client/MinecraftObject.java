package com.tom.cpm.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.gui.IKeybind;
import com.tom.cpm.shared.util.DynamicTexture.ITexture;

public class MinecraftObject implements MinecraftClientAccess {
	/** The default skin for the Steve model. */
	private static final ResourceLocation TEXTURE_STEVE = new ResourceLocation("textures/entity/steve.png");
	/** The default skin for the Alex model. */
	private static final ResourceLocation TEXTURE_ALEX = new ResourceLocation("textures/entity/alex.png");

	private final Minecraft mc;
	private final PlayerRenderManager prm;
	private final ModelDefinitionLoader loader;
	public MinecraftObject(Minecraft mc, ModelDefinitionLoader loader) {
		this.mc = mc;
		prm = new PlayerRenderManager(loader);
		this.loader = loader;
	}

	@Override
	public InputStream loadResource(String path) throws IOException {
		IResource r = mc.getResourceManager().getResource(new ResourceLocation(CustomPlayerModels.ID, path));
		return r.getInputStream();
	}

	@Override
	public BufferedImage getVanillaSkin(int skinType) {
		try {
			IResource r = mc.getResourceManager().getResource(skinType == 1 ? TEXTURE_STEVE : TEXTURE_ALEX);
			try (InputStream is = r.getInputStream()) {
				return ImageIO.read(is);
			}
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public PlayerRenderManager getPlayerRenderManager() {
		return prm;
	}

	@Override
	public ITexture createTexture() {
		return new DynTexture();
	}

	private static class DynTexture extends AbstractTexture implements ITexture {

		@Override
		public void bind() {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, getGlTextureId());
		}

		@Override
		public void load(BufferedImage image) {
			this.deleteGlTexture();
			TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image, false, false);
		}

		@Override
		public void free() {
			this.deleteGlTexture();
		}

		@Override
		public void loadTexture(IResourceManager resourceManager) throws IOException {}
	}

	@Override
	public void executeLater(Runnable r) {
		mc.func_152344_a(r);
	}

	@Override
	public ModelDefinitionLoader getDefinitionLoader() {
		return loader;
	}

	@Override
	public int getSkinType() {
		return 1;
	}

	@Override
	public void setEncodedGesture(int value) {
	}

	@Override
	public boolean isInGame() {
		return mc.thePlayer != null;
	}

	@Override
	public Object getPlayerIDObject() {
		return mc.getSession().func_148256_e();
	}

	@Override
	public List<IKeybind> getKeybinds() {
		return KeyBindings.kbs;
	}

	@Override
	public ServerStatus getServerSideStatus() {
		return mc.thePlayer != null ? ServerStatus.UNAVAILABLE : ServerStatus.OFFLINE;
	}
}
