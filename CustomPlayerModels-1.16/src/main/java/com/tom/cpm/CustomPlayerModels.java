package com.tom.cpm;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import com.tom.cpm.client.ClientProxy;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigEntry.ModConfig;

@Mod("cpm")
public class CustomPlayerModels implements MinecraftCommonAccess {

	public CustomPlayerModels() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(this);
	}

	public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

	private void doClientStuff(final FMLClientSetupEvent event) {
		proxy.init();
	}

	private ModConfig cfg;

	public void setup(FMLCommonSetupEvent evt) {
		cfg = new ModConfig(new File(FMLPaths.CONFIGDIR.get().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);
	}

	@Override
	public ModConfig getConfig() {
		return cfg;
	}

	@SubscribeEvent
	public void onStart(FMLServerStartingEvent e) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(e.getServer()));
	}

	@SubscribeEvent
	public void onStop(FMLServerStoppingEvent e) {
		MinecraftObjectHolder.setServerObject(null);
	}
}
