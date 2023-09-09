package com.tom.cpl.block;

import java.util.List;

import com.tom.cpl.block.BiomeHandler.RainType;

public class Biome {
	private final BiomeHandler<Object> handler;
	private final Object biome;

	@SuppressWarnings("unchecked")
	protected <T> Biome(BiomeHandler<T> handler, T biome) {
		this.biome = biome;
		this.handler = (BiomeHandler<Object>) handler;
	}

	public Object getBiome() {
		return biome;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Biome other = (Biome) obj;
		return handler == other.handler && handler.equals(biome, other.biome);
	}

	public boolean isInTag(String tag) {
		return handler.isInTag(tag, biome);
	}

	public List<String> listTags() {
		return handler.listTags(biome);
	}

	public String getBiomeId() {
		return handler.getBiomeId(biome);
	}

	public float getTemperature() {
		return handler.getTemperature(biome);
	}

	public float getHumidity() {
		return handler.getHumidity(biome);
	}

	public RainType getRainType() {
		return handler.getRainType(biome);
	}
}
