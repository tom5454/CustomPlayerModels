package com.tom.cpm.shared.network.packet;

import java.io.IOException;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

public class PluginMessageS2C extends NBTEntityS2C {
	private String id;

	public PluginMessageS2C() {
		super();
	}

	public PluginMessageS2C(String msgID, int entityId, NBTTagCompound data) {
		super(entityId, data);
		this.id = msgID;
	}

	@Override
	public void write(IOHelper pb) throws IOException {
		pb.writeUTF(id);
		super.write(pb);
	}

	@Override
	public void read(IOHelper pb) throws IOException {
		id = pb.readUTF();
		super.read(pb);
	}

	@Override
	protected <P> void handle(NetHandler<?, P, ?> handler, NetH from, P player) {
		MinecraftCommonAccess.get().getApi().clientApi().handlePacket(id, tag, player);
	}
}
