package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.world.biome.Biome;

import com.tom.cpl.block.BiomeHandler;

public class BiomeHandlerImpl extends BiomeHandler<Biome> {
	public static final BiomeHandlerImpl impl = new BiomeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.Biome> listNativeEntries(String tag) {
		Biome b = biomes().filter(e -> e.translationKey.equals(tag)).findFirst().orElse(null);
		if (b != null)return Collections.singletonList(wrap(b));
		return Collections.emptyList();
	}

	private Stream<Biome> biomes() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(Registries.BIOMES.iterator(), Spliterator.DISTINCT | Spliterator.ORDERED), false);
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
		return biomes().map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(Biome a, Biome b) {
		return a == b;
	}

	@Override
	public String getBiomeId(Biome state) {
		return state.translationKey;
	}

	@Override
	public float getTemperature(Biome state) {
		return 0;//state.temperature;
	}

	@Override
	public float getHumidity(Biome state) {
		return 0;//state.rainfall;
	}

	@Override
	public RainType getRainType(Biome state) {
		//return state.field_896 ? RainType.SNOW : state.field_897 ? RainType.RAIN : RainType.NONE;
		return RainType.NONE;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
