package com.tom.cpmsvcc;

import java.util.function.Supplier;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CPMSVCC.MOD_ID)
public class CPMSVCModForge {

	public CPMSVCModForge() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::sendIMC);
	}

	public void sendIMC(InterModEnqueueEvent e) {
		InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) CPMSVCPlugin::new);
	}
}