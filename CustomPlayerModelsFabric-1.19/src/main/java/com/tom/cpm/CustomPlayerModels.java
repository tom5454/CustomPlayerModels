package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;

public class CustomPlayerModels implements MinecraftCommonAccess, ModInitializer {
	private ModConfigFile config;

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);
	public static CPMApiManager api;

	@Override
	public void onInitialize() {
		api = new CPMApiManager();
		config = new ModConfigFile(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);

		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			MinecraftObjectHolder.setServerObject(new MinecraftServerObject(s));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		});
		ServerTickEvents.END_SERVER_TICK.register(s -> ServerHandler.netHandler.tick());
		CommandRegistrationCallback.EVENT.register((d, a, e) -> {
			new Command(d);
		});
		EntityTrackingEvents.START_TRACKING.register(ServerHandler::onTrackingStart);
		FabricLoader.getInstance().getEntrypointContainers("cpmapi", ICPMPlugin.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ICPMPlugin plugin = entrypoint.getEntrypoint();
				api.register(plugin);
			} catch (Throwable e) {
				LOG.error("Mod {} provides a broken implementation of CPM api", modId, e);
			}
		});
		LOG.info("Customizable Player Models Initialized");
		LOG.info(api.getPluginStatus());
		api.buildCommon().player(PlayerEntity.class).init();
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
			PlatformFeature.EDITOR_SUPPORTED
			);

	@Override
	public EnumSet<PlatformFeature> getSupportedFeatures() {
		return features;
	}

	@Override
	public String getPlatformVersionString() {
		String modVer = FabricLoader.getInstance().getModContainer("cpm").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
		String fVer = FabricLoader.getInstance().getModContainer("fabric").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
		String lVer = FabricLoader.getInstance().getModContainer("fabricloader").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
		String s = "Minecraft " + SharedConstants.getGameVersion().getName() + " (fabric/" + lVer + "/" + fVer + ") " + modVer;
		return s;
	}

	@Override
	public TextRemapper<MutableText> getTextRemapper() {
		return new TextRemapper<>(Text::translatable, Text::literal, MutableText::append, Text::keybind);
	}

	@Override
	public CPMApiManager getApi() {
		return api;
	}
}
