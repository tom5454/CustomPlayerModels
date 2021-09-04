package com.tom.cpm.shared.effects;

import java.io.IOException;

import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.render.ItemRenderer;

public class EffectRenderItem implements IRenderEffect {
	private int id;
	private ItemSlot slot;
	private int slotID;

	public EffectRenderItem() {
	}

	public EffectRenderItem(int id, ItemSlot slot, int slotID) {
		this.id = id;
		this.slot = slot;
		this.slotID = slotID;
	}

	@Override
	public void load(IOHelper in) throws IOException {
		id = in.readVarInt();
		slot = in.readEnum(ItemSlot.VALUES);
		slotID = in.readByte();
	}

	@Override
	public void write(IOHelper out) throws IOException {
		out.writeVarInt(id);
		out.writeEnum(slot);
		out.writeByte(slotID);
	}

	@Override
	public void apply(ModelDefinition def) {
		RenderedCube cube = def.getElementById(id);
		if(cube != null) {
			cube.getCube().size = new Vec3f(0, 0, 0);
			cube.getCube().mcScale = 0;
			cube.itemRenderer = new ItemRenderer(slot, slotID);
		}
	}

	@Override
	public RenderEffects getEffect() {
		return RenderEffects.ITEM;
	}
}
