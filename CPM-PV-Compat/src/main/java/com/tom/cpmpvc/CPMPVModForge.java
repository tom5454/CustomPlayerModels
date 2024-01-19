package com.tom.cpmpvc;

import java.util.function.Supplier;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CPMPVC.MOD_ID)
public class CPMPVModForge {

	public CPMPVModForge() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::sendIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
	}

	public void clientSetup(FMLClientSetupEvent e) {
		CPMAddon.init();
	}

	public void sendIMC(InterModEnqueueEvent e) {
		InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) CPMPVPlugin::new);
	}
}