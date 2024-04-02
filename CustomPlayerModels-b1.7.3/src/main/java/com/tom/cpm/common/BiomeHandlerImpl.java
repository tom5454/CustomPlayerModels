package com.tom.cpm.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.class_153;

import com.tom.cpl.block.BiomeHandler;

public class BiomeHandlerImpl extends BiomeHandler<class_153> {
	public static final BiomeHandlerImpl impl = new BiomeHandlerImpl();

	@Override
	public List<com.tom.cpl.block.Biome> listNativeEntries(String tag) {
		class_153 b = Arrays.stream(class_153.field_898).filter(e -> e.field_888.equals(tag)).findFirst().orElse(null);
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
	public boolean isInTag(String tag, class_153 state) {
		return getBiomeId(state).equals(tag);
	}

	@Override
	public List<String> listTags(class_153 state) {
		return Collections.emptyList();
	}

	@Override
	public List<com.tom.cpl.block.Biome> getAllElements() {
		return Arrays.stream(class_153.field_898).map(this::wrap).collect(Collectors.toList());
	}

	@Override
	public boolean equals(class_153 a, class_153 b) {
		return a == b;
	}

	@Override
	public String getBiomeId(class_153 state) {
		return state.field_888;
	}

	@Override
	public float getTemperature(class_153 state) {
		return 0;//state.temperature;
	}

	@Override
	public float getHumidity(class_153 state) {
		return 0;//state.rainfall;
	}

	@Override
	public RainType getRainType(class_153 state) {
		return state.field_896 ? RainType.SNOW : state.field_897 ? RainType.RAIN : RainType.NONE;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
