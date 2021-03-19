package com.tom.cpm;

import java.io.File;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;

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

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		proxy.init();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent evt) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(evt.getServer()));
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
}
