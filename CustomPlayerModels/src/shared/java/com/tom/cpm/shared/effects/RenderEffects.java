package com.tom.cpm.shared.effects;

import java.util.function.Supplier;

public enum RenderEffects {
	GLOW(EffectGlow::new),
	SCALE(EffectScale::new),
	HIDE(EffectHide::new),
	COLOR(EffectColor::new),
	SINGLE_TEX(EffectSingleTexture::new),
	PER_FACE_UV(EffectPerFaceUV::new),
	UV_OVERFLOW(EffectUV::new),
	ITEM(EffectRenderItem::new),
	HIDE_SKULL(EffectHideSkull::new),
	REMOVE_ARMOR_OFFSET(EffectRemoveArmorOffset::new),
	EXTRUDE(EffectExtrude::new),
	@Deprecated PLAYER_SCALE(EffectPlayerScale::new),
	MODEL_SCALE(EffectModelScale::new),
	SCALING(EffectScaling::new),
	COPY_TRANSFORM(EffectCopyTransform::new),
	FIRST_PERSON_HAND(EffectFirstPersonHandPos::new),
	;
	public static final RenderEffects[] VALUES = values();
	private Supplier<IRenderEffect> factory;
	private RenderEffects(Supplier<IRenderEffect> factory) {
		this.factory = factory;
	}

	public IRenderEffect create() {
		return factory.get();
	}
}
