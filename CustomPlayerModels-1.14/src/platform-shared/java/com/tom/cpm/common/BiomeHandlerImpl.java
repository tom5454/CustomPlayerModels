package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import com.tom.cpl.block.BiomeHandler;

public class BiomeHandlerImpl extends BiomeHandler<Biome> {
	public static final BiomeHandlerImpl impl = new BiomeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.Biome> listNativeEntries(String tag) {
		ResourceLocation rl = ResourceLocation.tryParse(tag);
		Biome b = Registry.BIOME.get(rl);
		if (b != null)return Collections.singletonList(wrap(b));
		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return Collections.emptyList();
	}

	@Override
	public com.tom.cpl.block.Biome emptyObject() {
		return wrap(null);
	}

	@Override
	public boolean isInTag(String tag, Biome state) {
		return getBiomeId(state).equals(tag);
	}

	@Override
	public List<String> listTags(Biome state) {
		return Collections.emptyList();
	}

	@Override
	public List<com.tom.cpl.block.Biome> getAllElements() {
		return Registry.BIOME.stream().map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(Biome a, Biome b) {
		return a == b;
	}

	@Override
	public String getBiomeId(Biome state) {
		return Registry.BIOME.getKey(state).toString();
	}

	@Override
	public float getTemperature(Biome state) {
		return state.getTemperature();
	}

	@Override
	public float getHumidity(Biome state) {
		return state.getDownfall();
	}

	@Override
	public RainType getRainType(Biome state) {
		return RainType.get(state.getPrecipitation().name());
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
