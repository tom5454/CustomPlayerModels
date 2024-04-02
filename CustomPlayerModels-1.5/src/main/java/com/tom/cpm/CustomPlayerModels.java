package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.MinecraftForge;

import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.block.entity.EntityTypeHandler;
import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.client.Lang;
import com.tom.cpm.common.BlockStateHandlerImpl;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.EntityTypeHandlerImpl;
import com.tom.cpm.common.GiveSkullCommand;
import com.tom.cpm.common.ItemStackHandlerImpl;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.retro.JavaLogger;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpmcore.CPMLoadingPlugin;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = CustomPlayerModels.ID, useMetadata = true)
public class CustomPlayerModels implements MinecraftCommonAccess {
	public static final String ID = "customplayermodels";

	@SidedProxy(clientSide = "com.tom.cpm.client.ClientProxy", serverSide = "com.tom.cpm.CommonProxy")
	public static CommonProxy proxy;

	public static final ILogger log = new JavaLogger(FMLLog.getLogger(), "CPM");
	public static CPMApiManager api;

	@Mod.Init
	public void init(FMLInitializationEvent evt) {
		api = new CPMApiManager();
		proxy.init();
		ServerHandler sh = new ServerHandler();
		MinecraftForge.EVENT_BUS.register(sh);
		log.info("Customizable Player Models Initialized");
	}

	@Mod.IMCCallback
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
	}

	@Mod.ServerStarting
	public void serverStart(FMLServerStartingEvent evt) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(evt.getServer()));
		new Command(evt::registerServerCommand, false);
		evt.registerServerCommand(new GiveSkullCommand());
	}

	@Mod.ServerStopping
	public void serverStop(FMLServerStoppingEvent evt) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}

	private ModConfigFile cfg;

	@Mod.PreInit
	public void preInit(FMLPreInitializationEvent evt) {
		if (!CPMLoadingPlugin.isLoaded) {
			log.error("###########################################");
			log.error("CPM is a Core mod!");
			log.error("Please place the CustomPlayerModels-" + getMCVersion() + "-" + getModVersion() + ".jar");
			log.error("into the coremods folder in your Minecraft installation.");
			log.error("###########################################");
			throw new RuntimeException("CPM is a Core mod! Please move into the coremods folder.");
		}
		cfg = new ModConfigFile(new File(evt.getModConfigurationDirectory(), "cpm.json"));
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
		return "1.5.2";
	}

	@Override
	public String getMCBrand() {
		return "(" + FMLCommonHandler.instance().getModName() + ")";
	}

	@Override
	public String getModVersion() {
		return Loader.instance().getIndexedModList().get(CustomPlayerModels.ID).getDisplayVersion();
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
