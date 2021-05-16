package com.tom.cpm.shared.model;

public enum SkinType {
	SLIM(0, "Slim"),
	DEFAULT(1, "Classic"),
	UNKNOWN(-1, "Classic"),
	;

	private final int channel;
	private final String lowerName, apiName;
	public static final SkinType[] VALUES = values();

	private SkinType(int channel, String apiName) {
		this.channel = channel;
		lowerName = name().toLowerCase();
		this.apiName = apiName;
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

	public String getApiName() {
		return apiName;
	}
}
