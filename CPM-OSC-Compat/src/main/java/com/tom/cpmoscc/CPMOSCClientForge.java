package com.tom.cpmoscc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CPMOSCClientForge {
	public static final CPMOSCClientForge INSTANCE = new CPMOSCClientForge();

	private static final String[] IS_PAUSED = new String[] {"isPaused", "func_147113_T", "m_91104_"};
	private static BooleanSupplier isPaused;

	static {
		Minecraft inst = null;
		try {
			for(Method method : Minecraft.class.getDeclaredMethods()) {
				if(Modifier.isStatic(method.getModifiers()) && method.getReturnType() == Minecraft.class) {
					inst = (Minecraft) method.invoke(null);
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		if(inst == null)throw new NoSuchMethodError("Failed to find Minecraft.getInstance()");

		try {
			Method isPausedM = null;
			for (String m : IS_PAUSED) {
				try {
					isPausedM = Minecraft.class.getDeclaredMethod(m);
				} catch (Throwable e) {
					continue;
				}
			}
			if(isPausedM == null)throw new RuntimeException("Failed to find Minecraft.isPaused()");

			final Method ip = isPausedM;
			final Minecraft i = inst;
			isPaused = () -> {
				try {
					return (boolean) ip.invoke(i);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					return false;
				}
			};
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		CPMOSC.LOGGER.info("CPM OSC initialized");
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent evt) {
		if (evt.phase == Phase.START || isPaused.getAsBoolean())
			return;

		CPMOSC.tick();
	}
}
