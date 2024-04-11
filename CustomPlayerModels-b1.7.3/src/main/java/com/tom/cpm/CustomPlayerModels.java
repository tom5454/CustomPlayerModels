package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.entity.player.PlayerEntity;

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
import com.tom.cpm.common.EntityTypeHandlerImpl;
import com.tom.cpm.common.ItemStackHandlerImpl;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.VersionCheck;

public class CustomPlayerModels implements MinecraftCommonAccess, ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("CPM");
	public static final ILogger log = new SLF4JLogger(LOGGER);
	public static CPMApiManager api;
	public static SidedHandler proxy;

	@Override
	public void onInitialize() {
		cfg = new ModConfigFile(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);

		api = new CPMApiManager();
		FabricLoader.getInstance().getEntrypointContainers("cpmapi", ICPMPlugin.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ICPMPlugin plugin = entrypoint.getEntrypoint();
				api.register(plugin);
			} catch (Throwable e) {
				log.error("Mod " + modId + " provides a broken implementation of CPM api", e);
			}
		});
		log.info("Customizable Player Models Initialized");
		log.info(api.getPluginStatus());
		api.buildCommon().player(PlayerEntity.class).init();
	}

	private ModConfigFile cfg;

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
		return "b1.7.3";
	}

	@Override
	public String getMCBrand() {
		String lVer = FabricLoader.getInstance().getModContainer("fabricloader").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
		return "(babric/" + lVer + ")";
	}

	@Override
	public String getModVersion() {
		return FabricLoader.getInstance().getModContainer("cpm").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?UNKNOWN?");
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

	@Override
	public IVersionCheck getVersionCheck() {
		return VersionCheck.get(getMCVersion() + "-fabric", getModVersion());
	}
}
