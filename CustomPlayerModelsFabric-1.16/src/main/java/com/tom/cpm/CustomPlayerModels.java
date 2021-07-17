package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.common.CommandCPM;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;

public class CustomPlayerModels implements MinecraftCommonAccess, ModInitializer {
	private ModConfigFile config;

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);

	@Override
	public void onInitialize() {
		config = new ModConfigFile(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);

		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			MinecraftObjectHolder.setServerObject(new MinecraftServerObject(s));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		});
		CommandRegistrationCallback.EVENT.register((d, isD) -> {
			CommandCPM.register(d);
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((o, n, end) -> {
			if(!end)ServerHandler.netHandler.onRespawn(n);
		});
		LOG.info("Customizable Player Models Initialized");
	}

	@Override
	public ModConfigFile getConfig() {
		return config;
	}

	@Override
	public ILogger getLogger() {
		return log;
	}

	private static final EnumSet<PlatformFeature> features = EnumSet.of(
			PlatformFeature.EDITOR_HELD_ITEM,
			PlatformFeature.RENDER_ARMOR,
			PlatformFeature.RENDER_ELYTRA,
			PlatformFeature.RENDER_CAPE
			);

	@Override
	public EnumSet<PlatformFeature> getSupportedFeatures() {
		return features;
	}
}
