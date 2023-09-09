package com.tom.cpl.block;

public interface World {
	BlockState getBlock(int x, int y, int z);
	boolean isCovered();
	int getYHeight();
	int getMaxHeight();
	int getMinHeight();
	WeatherType getWeather();
	Biome getBiome();
	String getDimension();

	public static enum WeatherType {
		CLEAR,
		RAIN,
		THUNDER
	}
}
