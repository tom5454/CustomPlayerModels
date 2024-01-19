package com.tom.cpmsvcc;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CPMSVCC.MOD_ID)
public class CPMSVCModNeoForge {

	public CPMSVCModNeoForge() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::sendIMC);
	}

	public CPMSVCModNeoForge(IEventBus bus) {
		bus.addListener(this::sendIMC);
	}

	public void sendIMC(InterModEnqueueEvent e) {
		InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) CPMSVCPlugin::new);
	}
}