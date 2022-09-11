package com.tom.cpmsvcc;

import java.util.function.Supplier;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.versions.forge.ForgeVersion;

@Mod(CPMSVCC.MOD_ID)
public class CPMSVCMod {

	public CPMSVCMod() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::sendIMC);
	}

	public void sendIMC(InterModEnqueueEvent e) {
		if (Integer.parseInt(ForgeVersion.getSpec().substring(0, 2)) < 37)
			InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) () -> CPMSVCC.make("forge116"));
		else if (Integer.parseInt(ForgeVersion.getSpec().substring(0, 2)) < 41)
			InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) () -> CPMSVCC.make("forge118"));
		else
			InterModComms.sendTo("cpm", "api", () -> (Supplier<?>) () -> CPMSVCC.make("forge119"));
	}
}