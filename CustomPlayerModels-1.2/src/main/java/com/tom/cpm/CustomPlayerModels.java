package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import net.minecraft.src.EntityPlayer;

import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.block.entity.EntityTypeHandler;
import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.client.Lang;
import com.tom.cpm.common.BlockStateHandlerImpl;
import com.tom.cpm.common.EntityTypeHandlerImpl;
import com.tom.cpm.common.ItemStackHandlerImpl;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.retro.JavaLogger;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;

public class CustomPlayerModels implements MinecraftCommonAccess {
	public static final String ID = "customplayermodels";

	public static CommonProxy proxy = makeProxy();

	public static final ILogger log = new JavaLogger(Loader.log, "CPM");
	public static CPMApiManager api;

	public void init() {
		api = new CPMApiManager();
		proxy.init();
		ServerHandler.init();

		api.buildCommon().player(EntityPlayer.class).init();
		proxy.apiInit();

		log.info("Customizable Player Models Initialized");
	}

	public static CommonProxy makeProxy() {
		throw new AbstractMethodError("Injector failed");
	}

	/*@Mod.IMCCallback
	public void processIMC(IMCEvent event) {
		event.getMessages().forEach(m -> {
			try {
				if(m.key.equals("api")) {
					ICPMPlugin plugin = (ICPMPlugin) Class.forName(m.getStringValue()).newInstance();
					api.register(plugin);
				}
			} catch (Throwable e) {
				log.error("Mod " + m.getSender() + " provides a broken implementation of CPM api", e);
			}
		});
		log.info("Customizable Player Models IMC processed: " + api.getPluginStatus());
		api.buildCommon().player(EntityPlayer.class).init();
		proxy.apiInit();
	}*/

	private ModConfigFile cfg;

	public CustomPlayerModels() {
		cfg = new ModConfigFile(new File(FMLCommonHandler.instance().getMinecraftRootDirectory(), "config/cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public ILogger getLogger() {
		return log;
	}

	private static final EnumSet<PlatformFeature> features = EnumSet.noneOf(PlatformFeature.class);

	@Override
	public EnumSet<PlatformFeature> getSupportedFeatures() {
		return features;
	}

	@Override
	public String getMCVersion() {
		return "1.2.5";
	}

	@Override
	public String getMCBrand() {
		return "(fml,forge)";
	}

	@Override
	public String getModVersion() {
		return CPMVersion.getVersion();
	}

	@Override
	public TextRemapper<String> getTextRemapper() {
		return TextRemapper.stringMapper(Lang::format);
	}

	@Override
	public CPMApiManager getApi() {
		return api;
	}

	@Override
	public BlockStateHandler<?> getBlockStateHandler() {
		return BlockStateHandlerImpl.impl;
	}

	@Override
	public ItemStackHandler<?> getItemStackHandler() {
		return ItemStackHandlerImpl.impl;
	}

	@Override
	public EntityTypeHandler<?> getEntityTypeHandler() {
		return EntityTypeHandlerImpl.impl;
	}
}
