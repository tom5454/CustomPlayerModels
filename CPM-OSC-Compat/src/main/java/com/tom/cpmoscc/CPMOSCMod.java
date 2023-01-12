package com.tom.cpmoscc;

import java.util.function.Supplier;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CPMOSC.MOD_ID)
public class CPMOSCMod {

	public CPMOSCMod() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::sendIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
	}

	public void sendIMC(InterModEnqueueEvent e) {
		InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) CPMOSCPlugin::new);
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		CPMOSCClientForge.INSTANCE.init();
	}
}