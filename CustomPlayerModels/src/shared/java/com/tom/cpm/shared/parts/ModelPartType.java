package com.tom.cpm.shared.parts;

public enum ModelPartType {
	END(ModelPartEnd::new),
	PLAYER(ModelPartPlayer::new),
	TEMPLATE(ModelPartTemplate::new),
	DEFINITION(ModelPartDefinition::new),
	DEFINITION_LINK(ModelPartDefinitionLink::new),
	SKIN(ModelPartSkin::new),
	SKIN_LINK(ModelPartSkinLink::new),
	PLAYER_PARTPOS(ModelPartPlayerPos::new),
	RENDER_EFFECT(ModelPartRenderEffect::new),
	UUID_LOCK(ModelPartUUIDLockout::new),
	ANIMATION_DATA(ModelPartAnimation::new),
	SKIN_TYPE(ModelPartSkinType::new),
	MODEL_ROOT(ModelPartRoot::new),
	LIST_ICON(ModelPartListIcon::new),
	DUP_ROOT(ModelPartDupRoot::new),
	CLONEABLE(ModelPartCloneable::new),
	SCALE(ModelPartScale::new),
	;
	public static final ModelPartType[] VALUES = values();
	private final IModelPart.Factory factory;
	private ModelPartType(IModelPart.Factory factory) {
		this.factory = factory;
	}

	public IModelPart.Factory getFactory() {
		return factory;
	}
}
