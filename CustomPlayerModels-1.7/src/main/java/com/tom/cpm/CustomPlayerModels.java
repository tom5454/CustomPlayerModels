package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.common.CommandCPM;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = CustomPlayerModels.ID, acceptableRemoteVersions = "*")
public class CustomPlayerModels implements MinecraftCommonAccess {
	public static final String ID = "customplayermodels";

	@SidedProxy(clientSide = "com.tom.cpm.client.ClientProxy", serverSide = "com.tom.cpm.CommonProxy")
	public static CommonProxy proxy;

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		proxy.init();
		ServerHandler sh = new ServerHandler();
		MinecraftForge.EVENT_BUS.register(sh);
		FMLCommonHandler.instance().bus().register(sh);
		LOG.info("Customizable Player Models Initialized");
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent evt) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(evt.getServer()));
		evt.registerServerCommand(new CommandCPM());
	}

	@EventHandler
	public void serverStop(FMLServerStoppingEvent evt) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}

	private ModConfigFile cfg;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
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
}
