package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.world.biome.BiomeGenBase;

import com.tom.cpl.block.BiomeHandler;

public class BiomeHandlerImpl extends BiomeHandler<BiomeGenBase> {
	public static final BiomeHandlerImpl impl = new BiomeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.Biome> listNativeEntries(String tag) {
		BiomeGenBase b = BiomeGenBase.BIOME_ID_MAP.get(tag);
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
	public boolean isInTag(String tag, BiomeGenBase state) {
		return getBiomeId(state).equals(tag);
	}

	@Override
	public List<String> listTags(BiomeGenBase state) {
		return Collections.emptyList();
	}

	@Override
	public List<com.tom.cpl.block.Biome> getAllElements() {
		return BiomeGenBase.BIOME_ID_MAP.values().stream().map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(BiomeGenBase a, BiomeGenBase b) {
		return a == b;
	}

	@Override
	public String getBiomeId(BiomeGenBase state) {
		return state.biomeName;
	}

	@Override
	public float getTemperature(BiomeGenBase state) {
		return state.temperature;
	}

	@Override
	public float getHumidity(BiomeGenBase state) {
		return state.rainfall;
	}

	@Override
	public RainType getRainType(BiomeGenBase state) {
		return state.isSnowyBiome() ? RainType.SNOW : state.canSpawnLightningBolt() ? RainType.RAIN : RainType.NONE;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
