package com.tom.cpm.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.util.DynamicTexture.ITexture;
import com.tom.cpl.util.Image;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.NetworkHandler;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpmcore.CPMASMClientHooks;

public class MinecraftObject implements MinecraftClientAccess {
	/** The default skin for the Steve model. */
	private static final ResourceLocation TEXTURE_STEVE = new ResourceLocation("textures/entity/steve.png");
	/** The default skin for the Alex model. */
	private static final ResourceLocation TEXTURE_ALEX = new ResourceLocation(CustomPlayerModels.ID, "textures/entity/alex.png");

	private final Minecraft mc;
	private final PlayerRenderManager prm;
	private final ModelDefinitionLoader loader;
	public MinecraftObject(Minecraft mc, ModelDefinitionLoader loader) {
		this.mc = mc;
		prm = new PlayerRenderManager(loader);
		this.loader = loader;
	}

	@Override
	public Image getVanillaSkin(SkinType skinType) {
		ResourceLocation loc;
		switch (skinType) {
		case SLIM:
			loc = TEXTURE_ALEX;
			break;

		case DEFAULT:
		case UNKNOWN:
		default:
			loc = TEXTURE_STEVE;
			break;
		}
		try {
			IResource r = mc.getResourceManager().getResource(loc);
			try (InputStream is = r.getInputStream()) {
				return Image.loadFrom(is);
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
		public void load(Image image) {
			this.deleteGlTexture();
			TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image.toBufferedImage(), false, false);
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
	public SkinType getSkinType() {
		return SkinType.DEFAULT;
	}

	@Override
	public void setEncodedGesture(int value) {
		if(getServerSideStatus() == ServerStatus.INSTALLED) {
			mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload(NetworkHandler.setLayer.toString(), new byte[] {(byte) value}));
		}
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
		return mc.thePlayer != null ? CPMASMClientHooks.hasMod(mc.getNetHandler()) ? ServerStatus.INSTALLED : ServerStatus.UNAVAILABLE : ServerStatus.OFFLINE;
	}

	@Override
	public File getGameDir() {
		return mc.mcDataDir;
	}

	@Override
	public void sendSkinUpdate() {
		ClientProxy.INSTANCE.sendSkinData(mc.getNetHandler());
	}

	@Override
	public void openGui(Function<IGui, Frame> creator) {
		mc.displayGuiScreen(new GuiImpl(creator, mc.currentScreen));
	}
}
