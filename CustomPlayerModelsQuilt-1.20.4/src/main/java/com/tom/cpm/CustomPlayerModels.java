package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

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
	public void onInitialize(ModContainer mod) {
		cfg = new ModConfigFile(new File(QuiltLoader.getConfigDir().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);

		ServerLifecycleEvents.STARTING.register(s -> {
			MinecraftObjectHolder.setServerObject(new MinecraftServerObject(s));
		});
		ServerLifecycleEvents.STOPPING.register(s -> {
			ModConfig.getWorldConfig().save();
			MinecraftObjectHolder.setServerObject(null);
		});
		ServerTickEvents.END.register(s -> ServerHandler.netHandler.tick());
		CommandRegistrationCallback.EVENT.register((d, a, e) -> {
			new Command(d, false);
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((o, n, end) -> {
			if(!end)ServerHandler.netHandler.onRespawn(n);
		});
		QuiltLoader.getEntrypointContainers("cpmapi", ICPMPlugin.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().metadata();
			String modId = metadata.id();
			try {
				ICPMPlugin plugin = entrypoint.getEntrypoint();
				api.register(plugin);
			} catch (Throwable e) {
				LOG.error("Mod {} provides a broken implementation of CPM api", modId, e);
			}
		});
		LOG.info("Customizable Player Models Initialized");
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
		String fVer = QuiltLoader.getModContainer("quilt_base").map(m -> m.metadata().version().raw()).orElse("?UNKNOWN?");
		String lVer = QuiltLoader.getModContainer("quilt_loader").map(m -> m.metadata().version().raw()).orElse("?UNKNOWN?");
		return "(quilt/" + lVer + "/" + fVer + ")";
	}

	@Override
	public String getModVersion() {
		return QuiltLoader.getModContainer("cpm").map(m -> m.metadata().version().raw()).orElse("?UNKNOWN?");
	}

	@Override
	public IVersionCheck getVersionCheck() {
		return VersionCheck.get(getMCVersion() + "-quilt", getModVersion());
	}

	public static boolean isModLoaded(String string) {
		return QuiltLoader.isModLoaded(string);
	}
}
