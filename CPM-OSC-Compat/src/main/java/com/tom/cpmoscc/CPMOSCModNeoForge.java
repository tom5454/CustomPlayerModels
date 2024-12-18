package com.tom.cpmoscc;

import java.util.function.Supplier;

import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
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
		try {
			String clazz = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES) >= 34 ? "CPMOSCClientNeoForgeNew" : "CPMOSCClientNeoForge";
			var clz = Class.forName("com.tom.cpmoscc." + clazz);
			var inst = clz.getDeclaredField("INSTANCE").get(null);
			clz.getDeclaredMethod("init").invoke(inst);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load CPMOSC compat", e);
		}
	}
}