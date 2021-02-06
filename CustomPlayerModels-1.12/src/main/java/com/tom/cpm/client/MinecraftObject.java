package com.tom.cpm.client;

import java.awt.image.BufferedImage;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
		return new FilterInputStream(r.getInputStream()) {
			@Override
			public void close() throws IOException {
				try {
					super.close();
				} finally {
					r.close();
				}
			}
		};
	}

	@Override
	public BufferedImage getVanillaSkin(int skinType) {
		try(IResource r = mc.getResourceManager().getResource(skinType == 1 ? TEXTURE_STEVE : TEXTURE_ALEX)) {
			return ImageIO.read(r.getInputStream());
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
			GlStateManager.bindTexture(getGlTextureId());
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
		mc.addScheduledTask(r);
	}

	@Override
	public ModelDefinitionLoader getDefinitionLoader() {
		return loader;
	}

	@Override
	public int getSkinType() {
		return DefaultPlayerSkin.getSkinType(mc.getSession().getProfile().getId()).equals("default") ? 1 : 0;
	}

	@Override
	public void setEncodedGesture(int value) {
		Set<EnumPlayerModelParts> s = ObfuscationReflectionHelper.getPrivateValue(GameSettings.class, mc.gameSettings, "field_178882_aU");
		setEncPart(s, value, 0, EnumPlayerModelParts.HAT);
		setEncPart(s, value, 1, EnumPlayerModelParts.JACKET);
		setEncPart(s, value, 2, EnumPlayerModelParts.LEFT_PANTS_LEG);
		setEncPart(s, value, 3, EnumPlayerModelParts.RIGHT_PANTS_LEG);
		setEncPart(s, value, 4, EnumPlayerModelParts.LEFT_SLEEVE);
		setEncPart(s, value, 5, EnumPlayerModelParts.RIGHT_SLEEVE);
		mc.gameSettings.sendSettingsToServer();
	}

	private static void setEncPart(Set<EnumPlayerModelParts> s, int value, int off, EnumPlayerModelParts part) {
		if((value & (1 << off)) != 0)s.add(part);
		else s.remove(part);
	}

	@Override
	public boolean isInGame() {
		return mc.player != null;
	}

	@Override
	public Object getPlayerIDObject() {
		return mc.getSession().getProfile();
	}

	@Override
	public List<IKeybind> getKeybinds() {
		return KeyBindings.kbs;
	}

	@Override
	public ServerStatus getServerSideStatus() {
		return mc.player != null ? ServerStatus.SKIN_LAYERS_ONLY : ServerStatus.OFFLINE;
	}
}
