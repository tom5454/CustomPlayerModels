package com.tom.cpm.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.EntityTracker;
import net.minecraft.src.EntityTrackerEntry;
import net.minecraft.src.ICommandListener;
import net.minecraft.src.WorldServer;

import com.tom.cpl.text.IText;
import com.tom.cpm.CommonProxy;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.ServerNetworkImpl;
import com.tom.cpm.shared.util.Log;

import cpw.mods.fml.server.FMLServerHandler;

public class ServerProxy extends CommonProxy {

	public static CommonProxy makeProxy() {
		return new ServerProxy();
	}

	@Override
	public void getTracking(EntityPlayer player, Consumer<EntityPlayer> f) {
		for (EntityTrackerEntry tr : (Set<EntityTrackerEntry>) ((WorldServer) player.worldObj).entityTracker.trackedEntitySet) {
			if (tr.trackedEntity instanceof EntityPlayer && tr.trackedPlayers.contains(player)) {
				f.accept((EntityPlayerMP) tr.trackedEntity);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<EntityPlayer> getTrackingPlayers(Entity entity) {
		EntityTracker et = ((WorldServer) entity.worldObj).entityTracker;
		EntityTrackerEntry entry = (EntityTrackerEntry) et.trackedEntityHashTable.lookup(entity.entityId);
		if (entry == null)
			return Collections.emptySet();
		else
			return entry.trackedPlayers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EntityPlayer> getPlayersOnline() {
		return FMLServerHandler.instance().getServer().configManager.playerEntities;
	}

	@Override
	public ServerNetworkImpl getServer(EntityPlayer pl) {
		return (ServerNetworkImpl) ((EntityPlayerMP) pl).playerNetServerHandler;
	}

	private static final Command.CommandHandlerBase<ICommandListener> cpm = new Command.CommandHandlerBase<ICommandListener>() {

		@Override
		protected void sendMessage(ICommandListener sender, String string) {
			sender.log(string);
		}

		@Override
		public void sendSuccess(ICommandListener sender, IText text) {
			String t = text.remap();
			sender.log(t);
			String r = sender.getUsername();
			t = "[" + r + ": " + t + "]";
			Log.info(t);
			FMLServerHandler.instance().getServer().configManager.sendChatMessageToAllOps("§7§o" + t);
		}
	};

	@Override
	public boolean runCommand(String command, String sender, Object listener) {
		return cpm.onCommand((ICommandListener) listener, command);
	}
}
