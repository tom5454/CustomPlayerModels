package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.tom.cpl.block.BiomeHandler;
import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.shared.MinecraftServerAccess;

public class BiomeHandlerImpl extends BiomeHandler<Holder<Biome>> {
	public static final BiomeHandlerImpl clientImpl = new BiomeHandlerImpl(() -> Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME));
	public static final BiomeHandlerImpl serverImpl = new BiomeHandlerImpl(() -> ((MinecraftServerObject) MinecraftServerAccess.get()).getServer().registryAccess().registryOrThrow(Registries.BIOME));

	public static BiomeHandlerImpl getImpl(Level level) {
		return level.isClientSide ? clientImpl : serverImpl;
	}

	private final Supplier<Registry<Biome>> registry;

	public BiomeHandlerImpl(Supplier<Registry<Biome>> registry) {
		this.registry = registry;
	}

	@Override
	public List<com.tom.cpl.block.Biome> listNativeEntries(String tag) {
		ResourceLocation rl = ResourceLocation.tryParse(tag);
		if (rl == null)return Collections.emptyList();
		Holder<Biome> b = registry.get().getHolder(ResourceKey.create(Registries.BIOME, rl)).orElse(null);
		if (b != null)return Collections.singletonList(wrap(b));
		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return registry.get().getTagNames().map(k -> k.location().toString()).toList();
	}

	@Override
	public com.tom.cpl.block.Biome emptyObject() {
		return wrap(null);
	}

	@Override
	public boolean isInTag(String tag, Holder<Biome> state) {
		if (tag.charAt(0) == '#') {
			ResourceLocation rl = ResourceLocation.tryParse(tag.substring(1));
			if (rl != null) {
				TagKey<Biome> i = TagKey.create(Registries.BIOME, rl);
				return state.is(i);
			}
		} else {
			return getBiomeId(state).equals(tag);
		}
		return false;
	}

	@Override
	public List<String> listTags(Holder<Biome> state) {
		return state.tags().map(k -> "#" + k.location()).toList();
	}

	@Override
	public List<com.tom.cpl.block.Biome> getAllElements() {
		return StreamSupport.stream(registry.get().asHolderIdMap().spliterator(), false).map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(Holder<Biome> a, Holder<Biome> b) {
		return a == b;
	}

	@Override
	public String getBiomeId(Holder<Biome> state) {
		return registry.get().getKey(state.value()).toString();
	}

	@Override
	public float getTemperature(Holder<Biome> state) {
		return state.value().getBaseTemperature();
	}

	@Override
	public float getHumidity(Holder<Biome> state) {
		return state.value().getDownfall();
	}

	@Override
	public RainType getRainType(Holder<Biome> state) {
		return RainType.get(state.value().getPrecipitation().name());
	}

	@Override
	public boolean isAvailable() {
		try {
			return registry.get() != null;
		} catch (Exception e) {
			return false;
		}
	}
}
