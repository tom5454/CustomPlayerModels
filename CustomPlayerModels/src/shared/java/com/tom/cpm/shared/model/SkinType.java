package com.tom.cpm.shared.model;

public enum SkinType {
	SLIM(0),
	DEFAULT(1),
	UNKNOWN(-1),
	;

	private final int channel;
	private final String lowerName;
	public static final SkinType[] VALUES = values();

	private SkinType(int channel) {
		this.channel = channel;
		lowerName = name().toLowerCase();
	}

	public static SkinType get(String name) {
		if(name == null)return DEFAULT;
		for (SkinType pl : VALUES) {
			if(name.equals(pl.lowerName))
				return pl;
		}
		return UNKNOWN;
	}

	public int getChannel() {
		return channel;
	}

	public String getName() {
		return lowerName;
	}
}
