package com.tom.cpm;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.common.CommandCPM;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;

@Mod(modid = CustomPlayerModels.ID, acceptableRemoteVersions = "*", clientSideOnly = true, updateJSON = "https://raw.githubusercontent.com/tom5454/CustomPlayerModels/master/version-check.json")
public class CustomPlayerModels implements MinecraftCommonAccess {
	public static final String ID = "customplayermodels";

	@SidedProxy(clientSide = "com.tom.cpm.client.ClientProxy", serverSide = "com.tom.cpm.CommonProxy")
	public static CommonProxy proxy;

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		proxy.init();
		MinecraftForge.EVENT_BUS.register(new ServerHandler());
		LOG.info("Customizable Player Models Initialized");
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent evt) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(evt.getServer()));
		evt.registerServerCommand(new CommandCPM());
	}

	@EventHandler
	public void serverStop(FMLServerStoppingEvent evt) {
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
}
