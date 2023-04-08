package com.tom.cpm.common;

import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayerMP;

import com.artemis.artemislib.util.attributes.ArtemisLibAttributes;

import com.tom.cpm.shared.network.NetHandler.ScalerInterface;
import com.tom.cpm.shared.util.ScalingOptions;

public class ArtemisScaler implements ScalerInterface<EntityPlayerMP, ScalingOptions> {
	private static final UUID CPM_ATTR_UUID = UUID.fromString("24bba381-9615-4530-8fcf-4fc42393a4b5");

	@Override
	public void setScale(ScalingOptions key, EntityPlayerMP player, float value) {
		if(key == ScalingOptions.WIDTH) {
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_WIDTH).removeModifier(CPM_ATTR_UUID);
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_WIDTH).applyModifier(new AttributeModifier(CPM_ATTR_UUID, "cpm", value - 1, 1).setSaved(false));
		} else if(key == ScalingOptions.HEIGHT) {
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_HEIGHT).removeModifier(CPM_ATTR_UUID);
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_HEIGHT).applyModifier(new AttributeModifier(CPM_ATTR_UUID, "cpm", value - 1, 1).setSaved(false));
		} else if(key == ScalingOptions.ENTITY) {
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_WIDTH).removeModifier(CPM_ATTR_UUID);
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_HEIGHT).removeModifier(CPM_ATTR_UUID);
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_WIDTH).applyModifier(new AttributeModifier(CPM_ATTR_UUID, "cpm", value - 1, 1).setSaved(false));
			player.getAttributeMap().getAttributeInstance(ArtemisLibAttributes.ENTITY_HEIGHT).applyModifier(new AttributeModifier(CPM_ATTR_UUID, "cpm", value - 1, 1).setSaved(false));
		}
	}

	@Override
	public ScalingOptions toKey(ScalingOptions opt) { return opt; }
}
