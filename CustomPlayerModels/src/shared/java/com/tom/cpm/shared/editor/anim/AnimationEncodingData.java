package com.tom.cpm.shared.editor.anim;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import com.tom.cpm.shared.editor.util.PlayerSkinLayer;

public class AnimationEncodingData {
	public Set<PlayerSkinLayer> freeLayers;
	public EnumMap<PlayerSkinLayer, Boolean> defaultLayerValue;

	public AnimationEncodingData() {
		freeLayers = new HashSet<>();
		defaultLayerValue = new EnumMap<>(PlayerSkinLayer.class);
	}

	public AnimationEncodingData(AnimationEncodingData cpy) {
		freeLayers = new HashSet<>(cpy.freeLayers);
		defaultLayerValue = new EnumMap<>(cpy.defaultLayerValue);
	}
}