package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.VersionCheck;

public class CustomPlayerModels extends CommonBase implements ModInitializer {

	@Override
	public void onInitialize() {
		cfg = new ModConfigFile(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);

		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			MinecraftObjectHolder.setServerObject(new MinecraftServerObject(s));
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		});
		ServerTickEvents.END_SERVER_TICK.register(s -> ServerHandler.netHandler.tick());
		CommandRegistrationCallback.EVENT.register((d, isD) -> {
			new Command(d, false);
		});
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
		apiInit();
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
	public String getMCBrand() {
		String fVer = FabricLoader.getInstance().getModContainer("fabric").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
		String lVer = FabricLoader.getInstance().getModContainer("fabricloader").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
		return "(fabric/" + lVer + "/" + fVer + ")";
	}

	@Override
	public String getModVersion() {
		return FabricLoader.getInstance().getModContainer("cpm").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
	}

	@Override
	public IVersionCheck getVersionCheck() {
		return VersionCheck.get(getMCVersion() + "-fabric", getModVersion());
	}
}
