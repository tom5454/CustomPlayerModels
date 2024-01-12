package com.tom.cpm;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

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

	public CustomPlayerModels(IEventBus bus) {
		bus.addListener(this::doClientStuff);
		bus.addListener(this::setup);
		bus.addListener(this::processIMC);
		MinecraftObjectHolder.setCommonObject(this);

		if (FMLEnvironment.dist == Dist.CLIENT) CustomPlayerModelsClient.preInit();

		NeoForge.EVENT_BUS.register(this);
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		CustomPlayerModelsClient.INSTANCE.init();
	}

	public void setup(FMLCommonSetupEvent evt) {
		cfg = new ModConfigFile(new File(FMLPaths.CONFIGDIR.get().toFile(), "cpm.json"));
		NeoForge.EVENT_BUS.register(new ServerHandler());
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
		if(FMLEnvironment.dist == Dist.CLIENT)CustomPlayerModelsClient.apiInit();
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
		return "(neoforge/" + NeoForgeVersion.getVersion() + ")";
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
