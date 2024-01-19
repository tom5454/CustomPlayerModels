package com.tom.cpmpvc;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(CPMPVC.MOD_ID)
public class CPMPVModNeoForge {

	public CPMPVModNeoForge(IEventBus bus) {
		bus.addListener(this::sendIMC);
		bus.addListener(this::clientSetup);
	}
	
	public void clientSetup(FMLClientSetupEvent e) {
		CPMAddon.init();
	}

	public void sendIMC(InterModEnqueueEvent e) {
		InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) CPMPVPlugin::new);
	}
}