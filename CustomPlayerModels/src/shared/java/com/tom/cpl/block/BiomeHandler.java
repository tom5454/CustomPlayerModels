package com.tom.cpl.block;

import java.util.List;

import com.tom.cpl.tag.NativeTagManager;

public abstract class BiomeHandler<B> implements NativeTagManager<Biome> {

	public Biome wrap(B stack) {
		return new Biome(this, stack);
	}

	@SuppressWarnings("unchecked")
	public B unwrap(Biome state) {
		return (B) state.getBiome();
	}

	@Override
	public List<String> listNativeTags(Biome state) {
		return listTags(unwrap(state));
	}

	@Override
	public boolean isInNativeTag(String tag, Biome state) {
		return isInTag(tag, unwrap(state));
	}

	public abstract boolean isInTag(String tag, B state);
	public abstract List<String> listTags(B state);
	public abstract List<Biome> getAllBiomes();
	public abstract boolean equals(B a, B b);
	public abstract String getBiomeId(B state);
	public abstract float getTemperature(B state);
	public abstract float getHumidity(B state);
	public abstract RainType getRainType(B state);

	public static enum RainType {
		NONE,
		RAIN,
		SNOW,
		;
		public static final RainType[] VALUES = values();

		public static RainType get(String name) {
			if(name == null)return NONE;
			for (RainType pl : VALUES) {
				if(name.equalsIgnoreCase(pl.name()))
					return pl;
			}
			return NONE;
		}
	}
}
