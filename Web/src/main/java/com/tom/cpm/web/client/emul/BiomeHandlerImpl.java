package com.tom.cpm.web.client.emul;

import java.util.List;

import com.tom.cpl.block.BiomeHandler;

public class BiomeHandlerImpl extends BiomeHandler<Biome> {
	public static final BiomeHandlerImpl impl = new BiomeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.Biome> listNativeEntries(String tag) {
		return null;
	}

	@Override
	public List<String> listNativeTags() {
		return null;
	}

	@Override
	public List<com.tom.cpl.block.Biome> getAllElements() {
		return null;
	}

	@Override
	public com.tom.cpl.block.Biome emptyObject() {
		return null;
	}

	@Override
	public boolean isInTag(String tag, Biome state) {
		return false;
	}

	@Override
	public List<String> listTags(Biome state) {
		return null;
	}

	@Override
	public boolean equals(Biome a, Biome b) {
		return false;
	}

	@Override
	public String getBiomeId(Biome state) {
		return null;
	}

	@Override
	public float getTemperature(Biome state) {
		return 0;
	}

	@Override
	public float getHumidity(Biome state) {
		return 0;
	}

	@Override
	public RainType getRainType(Biome state) {
		return null;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}
}
