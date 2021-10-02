package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.SharedConstants;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;

@Mod("cpm")
public class CustomPlayerModels implements MinecraftCommonAccess {

	public CustomPlayerModels() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new ServerHandler());
	}

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);

	public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

	private void doClientStuff(final FMLClientSetupEvent event) {
		proxy.init();
	}

	private ModConfigFile cfg;

	public void setup(FMLCommonSetupEvent evt) {
		cfg = new ModConfigFile(new File(FMLPaths.CONFIGDIR.get().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);
		LOG.info("Customizable Player Models Initialized");
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@SubscribeEvent
	public void onStart(FMLServerStartingEvent e) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(e.getServer()));
	}

	@SubscribeEvent
	public void onStop(FMLServerStoppingEvent e) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
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
		String modVer = ModList.get().getModContainerById("cpm").map(m -> m.getModInfo().getVersion().toString()).orElse("?UNKNOWN?");
		return "Minecraft " + SharedConstants.getCurrentVersion().getName() + " (forge/" + ForgeVersion.getVersion() + ") " + modVer;
	}
}
