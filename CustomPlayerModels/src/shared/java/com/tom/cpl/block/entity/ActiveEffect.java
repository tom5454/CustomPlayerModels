package com.tom.cpl.block.entity;

public class ActiveEffect {
	public final String effectId;
	public final int amplifier, duration;
	public final boolean hidden;

	public ActiveEffect(String effectId, int amplifier, int duration, boolean hidden) {
		this.effectId = effectId;
		this.amplifier = amplifier;
		this.duration = duration;
		this.hidden = hidden;
	}
}
