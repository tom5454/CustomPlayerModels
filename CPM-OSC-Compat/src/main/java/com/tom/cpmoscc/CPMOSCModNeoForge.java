package com.tom.cpmoscc;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@Mod(CPMOSC.MOD_ID)
public class CPMOSCModNeoForge {

	public CPMOSCModNeoForge(IEventBus bus) {
		bus.addListener(this::sendIMC);
		bus.addListener(this::doClientStuff);
	}

	public void sendIMC(InterModEnqueueEvent e) {
		InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) CPMOSCPlugin::new);
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		CPMOSCClientNeoForge.INSTANCE.init();
	}
}