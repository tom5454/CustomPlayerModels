package com.tom.cpm.shared.network;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.tom.cpl.function.ToFloatFunction;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.ServerAnimationState;
import com.tom.cpm.shared.animation.VanillaPose;

public enum ModelEventType {
	FALLING(VanillaPose.FALLING, floatType(a -> a.falling, (a, b) -> a.falling = b)),
	CREATIVE_FLYING(VanillaPose.CREATIVE_FLYING, booleanType(a -> a.creativeFlying, (a, b) -> a.creativeFlying = b)),
	JUMPING(VanillaPose.JUMPING, eventType((a, b) -> a.jumping = b)),
	HEALTH(VanillaPose.HEALTH, floatType(a -> a.health, (a, b) -> a.health = b)),
	HUNGER(VanillaPose.HUNGER, floatType(a -> a.hunger, (a, b) -> a.hunger = b)),
	AIR(VanillaPose.AIR, floatType(a -> a.air, (a, b) -> a.air = b)),
	IN_MENU(VanillaPose.IN_MENU, booleanType(a -> a.inMenu, (a, b) -> a.inMenu = b)),
	;
	public static final ModelEventType[] VALUES = values();
	public static final ModelEventType[] SYNC_TYPES = Arrays.stream(VALUES).filter(ModelEventType::autoSync).toArray(ModelEventType[]::new);
	private final String name;
	private final VanillaPose pose;
	private final ValueSync sync;

	private static ValueSync floatType(ToFloatFunction<ServerAnimationState> get, BiConsumer<ServerAnimationState, Float> set) {
		return new ValueSync() {

			@Override
			public void write(ModelEventType type, ServerAnimationState state, NBTTagCompound tag) {
				tag.setFloat(type.getName(), get.apply(state));
			}

			@Override
			public void read(ModelEventType type, AnimationState state, NBTTagCompound tag) {
				set.accept(state.serverState, tag.getFloat(type.getName()));
			}
		};
	}

	private static ValueSync booleanType(Predicate<ServerAnimationState> get, BiConsumer<ServerAnimationState, Boolean> set) {
		return new ValueSync() {

			@Override
			public void write(ModelEventType type, ServerAnimationState state, NBTTagCompound tag) {
				tag.setBoolean(type.getName(), get.test(state));
			}

			@Override
			public void read(ModelEventType type, AnimationState state, NBTTagCompound tag) {
				set.accept(state.serverState, tag.getBoolean(type.getName()));
			}
		};
	}

	private static ValueSync eventType(BiConsumer<AnimationState, Long> set) {
		return new ValueSync() {

			@Override
			public void write(ModelEventType type, ServerAnimationState state, NBTTagCompound tag) {
				tag.setBoolean(type.getName(), true);
			}

			@Override
			public void read(ModelEventType type, AnimationState state, NBTTagCompound tag) {
				set.accept(state, tag.getBoolean(type.getName()) ? MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime() : 0);
			}

			@Override
			public void trigger(AnimationState state) {
				set.accept(state, MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().getTime());
			}

			@Override
			public boolean autoSync() {
				return false;
			}
		};
	}

	private ModelEventType(VanillaPose pose, ValueSync sync) {
		name = name().toLowerCase(Locale.ROOT);
		this.pose = pose;
		this.sync = sync;
	}

	public static <T extends Enum<T>> ModelEventType of(String value) {
		for (int i = 0; i < VALUES.length; i++) {
			ModelEventType type = VALUES[i];
			if(type.name.equalsIgnoreCase(value))
				return type;
		}
		return null;
	}

	public static ModelEventType getType(IPose value) {
		for (int i = 0; i < VALUES.length; i++) {
			ModelEventType type = VALUES[i];
			if(type.pose == value)
				return type;
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void write(ServerAnimationState state, NBTTagCompound tag) {
		sync.write(this, state, tag);
	}

	public void read(AnimationState state, NBTTagCompound tag) {
		sync.read(this, state, tag);
	}

	public boolean autoSync() {
		return sync.autoSync();
	}

	public void trigger(AnimationState state) {
		sync.trigger(state);
	}

	public interface ValueSync {
		void write(ModelEventType type, ServerAnimationState state, NBTTagCompound tag);
		void read(ModelEventType type, AnimationState state, NBTTagCompound tag);

		default boolean autoSync() {
			return true;
		}

		default void trigger(AnimationState state) {}
	}
}
