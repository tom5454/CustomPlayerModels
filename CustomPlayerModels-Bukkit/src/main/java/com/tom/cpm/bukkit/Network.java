package com.tom.cpm.bukkit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.Log;

public class Network implements PluginMessageListener, Listener {
	public static final String PLAYER_DATA = MinecraftObjectHolder.NETWORK_ID + ":data";
	private final CPMBukkitPlugin plugin;
	private NetHandler<String, NBTTagCompound, Player, IOHelper, Meta> netHandler;

	public Network(CPMBukkitPlugin plugin) {
		this.plugin = plugin;
		try {
			netHandler = new NetHandler<>((k, v) -> k + ":" + v);
			NetHandler.initBuiltin(netHandler, Player::getEntityId, (pl, pck, dt) -> pl.owner.sendPluginMessage(plugin, pck, dt), this::sendToAllTrackingAndSelf);
			netHandler.setGetPlayerUUID(Player::getUniqueId);
			netHandler.setFindTracking((p, c) -> getPlayersWithin(p, 64, c));
			netHandler.setSendChat((pl, msg) -> pl.sendMessage(plugin.i18n.format(msg)));
			netHandler.setExecutor(() -> Runnable::run);
			netHandler.setGetNet(this::getMetadata);
			netHandler.setGetPlayer(n -> n.owner);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void register() {
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, netHandler.helloPacket, this);
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, netHandler.setSkin, this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, netHandler.helloPacket);
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, netHandler.setSkin);
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, netHandler.getSkin);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void onCommand(Player pl, String skin, boolean force, boolean save) {
		netHandler.onCommand(pl, skin, force, save);
	}

	public static void getPlayersWithin(Player player, int distance, Consumer<Player> cons) {
		int d2 = distance * distance;
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getWorld() == player.getWorld() && p.getLocation().distanceSquared(player.getLocation()) <= d2) {
				cons.accept(p);
			}
		}
	}

	private void sendToAllTrackingAndSelf(Player player, String packet, byte[] data) {
		getPlayersWithin(player, 64, pl -> {
			if(getMetadata(pl).cpm$hasMod()) {
				pl.sendPluginMessage(plugin, packet, data);
			}
		});
	}

	@Override
	public void onPluginMessageReceived(String name, Player player, byte[] packet) {
		netHandler.receiveServer(name, new IOHelper(packet), getMetadata(player));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		try {
			Method addChn = evt.getPlayer().getClass().getDeclaredMethod("addChannel", String.class);
			addChn.invoke(evt.getPlayer(), netHandler.helloPacket);
			addChn.invoke(evt.getPlayer(), netHandler.setSkin);
			addChn.invoke(evt.getPlayer(), netHandler.getSkin);
		} catch (Exception e) {
			e.printStackTrace();
		}
		evt.getPlayer().setMetadata(PLAYER_DATA, new FixedMetadataValue(plugin, new Meta(evt.getPlayer())));
		netHandler.onJoin(evt.getPlayer());
	}

	public Meta getMetadata(Player player) {
		List<MetadataValue> m = player.getMetadata(PLAYER_DATA);
		if(m != null) {
			for (MetadataValue mv : m) {
				if(mv.getOwningPlugin() == plugin && mv instanceof FixedMetadataValue) {
					return (Meta) ((FixedMetadataValue)mv).value();
				}
			}
		}
		Log.info("Created player data for player");
		Meta mt = new Meta(player);
		mt.cpm$setEncodedModelData(new PlayerData());
		player.setMetadata(PLAYER_DATA, new FixedMetadataValue(plugin, mt));
		return mt;
	}

	public static class Meta implements ServerNetH {
		private final Player owner;
		private boolean hasMod;
		private PlayerData data;
		public List<Player> trackingPlayers = new ArrayList<>();

		public Meta(Player owner) {
			this.owner = owner;
		}

		@Override
		public boolean cpm$hasMod() {
			return hasMod;
		}

		@Override
		public void cpm$setHasMod(boolean v) {
			hasMod = v;
		}

		@Override
		public PlayerData cpm$getEncodedModelData() {
			return data;
		}

		@Override
		public void cpm$setEncodedModelData(PlayerData data) {
			this.data = data;
		}
	}

	public void onTrackingStart(Player to, Player player) {
		netHandler.sendPlayerData(player, to);
	}
}
