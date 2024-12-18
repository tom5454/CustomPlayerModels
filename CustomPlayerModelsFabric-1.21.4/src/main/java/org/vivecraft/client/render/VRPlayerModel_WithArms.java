package org.vivecraft.client.render;

import net.minecraft.client.model.geom.ModelPart;

public class VRPlayerModel_WithArms extends VRPlayerModel {
	public ModelPart leftHand;
	public ModelPart rightHand;
	public ModelPart leftHandSleeve;
	public ModelPart rightHandSleeve;

	public VRPlayerModel_WithArms(ModelPart modelPart, boolean bl) {
		super(modelPart, bl);
	}

}
