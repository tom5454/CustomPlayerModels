package com.tom.cpm;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.VersionCheck;

@Mod("cpm")
public class CustomPlayerModels extends CommonBase {

	public CustomPlayerModels() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

		if (FMLEnvironment.dist == Dist.CLIENT) CustomPlayerModelsClient.preInit();

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new ServerHandler());
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		CustomPlayerModelsClient.INSTANCE.init();
	}

	public void setup(FMLCommonSetupEvent evt) {
		cfg = new ModConfigFile(new File(FMLPaths.CONFIGDIR.get().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);
		LOG.info("Customizable Player Models Initialized");
	}

	@SuppressWarnings("unchecked")
	private void processIMC(final InterModProcessEvent event) {
		event.getIMCStream().forEach(m -> {
			try {
				if(m.method().equals("api")) {
					ICPMPlugin plugin = ((Supplier<ICPMPlugin>) m.messageSupplier().get()).get();
					api.register(plugin);
				}
			} catch (Throwable e) {
				LOG.error("Mod {} provides a broken implementation of CPM api", m.senderModId(), e);
			}
		});
		apiInit();
		if (FMLEnvironment.dist == Dist.CLIENT) CustomPlayerModelsClient.apiInit();
	}

	@SubscribeEvent
	public void onStart(ServerStartingEvent e) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(e.getServer()));
	}

	@SubscribeEvent
	public void onStop(ServerStoppingEvent e) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
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
		return "(forge/" + ForgeVersion.getVersion() + ")";
	}

	@Override
	public String getModVersion() {
		return ModList.get().getModContainerById("cpm").map(m -> m.getModInfo().getVersion().toString()).orElse("?UNKNOWN?");
	}

	@Override
	public IVersionCheck getVersionCheck() {
		return VersionCheck.get(() -> ModList.get().getModContainerById("cpm").map(c -> VersionChecker.getResult(c.getModInfo()).changes()).orElse(Collections.emptyMap()));
	}
}
