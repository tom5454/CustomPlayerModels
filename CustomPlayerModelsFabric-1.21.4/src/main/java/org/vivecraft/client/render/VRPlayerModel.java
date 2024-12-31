package org.vivecraft.client.render;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public class VRPlayerModel extends PlayerModel {
	public ModelPart vrHMD;

	public VRPlayerModel(ModelPart modelPart, boolean bl) {
		super(modelPart, bl);
	}

}
