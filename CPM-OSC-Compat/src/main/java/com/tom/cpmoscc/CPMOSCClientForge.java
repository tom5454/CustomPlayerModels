package com.tom.cpmoscc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CPMOSCClientForge {
	public static final CPMOSCClientForge INSTANCE = new CPMOSCClientForge();

	private static final String[] IS_PAUSED = new String[] {"isPaused", "func_147113_T", "m_91104_"};
	private static final String[] PLAYER = new String[] {"player", "field_71439_g", "f_91074_"};
	private static BooleanSupplier isPaused;
	private static Supplier<Object> getPlayer;

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

			Field playerF = null;
			for (String m : PLAYER) {
				try {
					playerF = Minecraft.class.getDeclaredField(m);
				} catch (Throwable e) {
					continue;
				}
			}
			if(playerF == null)throw new RuntimeException("Failed to find Minecraft.player");

			final Method ip = isPausedM;
			final Field pf = playerF;
			final Minecraft i = inst;
			isPaused = () -> {
				try {
					return (boolean) ip.invoke(i);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					return false;
				}
			};
			getPlayer = () -> {
				try {
					return pf.get(i);
				} catch (IllegalAccessException | IllegalArgumentException e) {
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

		CPMOSC.tick(getPlayer.get());
	}
}
