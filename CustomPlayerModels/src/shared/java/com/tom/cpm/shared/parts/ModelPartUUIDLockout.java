package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.UUID;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.SafetyException;
import com.tom.cpm.shared.definition.SafetyException.BlockReason;
import com.tom.cpm.shared.io.IOHelper;

public class ModelPartUUIDLockout implements IModelPart, IResolvedModelPart {
	private UUID lockID;

	public ModelPartUUIDLockout(IOHelper in, ModelDefinition def) throws IOException {
		lockID = in.readUUID();
	}

	public ModelPartUUIDLockout(UUID uuid) {
		this.lockID = uuid;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeUUID(lockID);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.UUID_LOCK;
	}

	@Override
	public void apply(ModelDefinition def) {
		UUID uuid = def.getPlayerObj().getUUID();
		if(!lockID.equals(uuid) && !MinecraftClientAccess.get().getClientPlayer().getUUID().equals(uuid))
			throw new SafetyException(BlockReason.UUID_LOCK);
	}
}
