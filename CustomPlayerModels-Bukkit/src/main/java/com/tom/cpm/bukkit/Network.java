package com.tom.cpm.bukkit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.FastByteArrayInputStream;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.util.Log;

public class Network implements PluginMessageListener, Listener {
	public static final String PLAYER_DATA = MinecraftObjectHolder.NETWORK_ID + ":data";
	private final CPMBukkitPlugin plugin;
	public NetHandler<String, Player, Meta> netHandler;

	public Network(CPMBukkitPlugin plugin) {
		this.plugin = plugin;
		try {
			netHandler = new NetHandler<>((k, v) -> k + ":" + v);
			netHandler.setSendPacketDirect((pl, pck, dt) -> pl.owner.sendPluginMessage(plugin, pck, dt), this::sendToAllTrackingAndSelf);
			netHandler.setGetPlayerUUID(Player::getUniqueId);
			netHandler.setFindTracking((p, c) -> getPlayersWithin(p, 64, c));
			netHandler.setSendChat((pl, msg) -> pl.sendMessage(msg.<String>remap()));
			netHandler.setExecutor(() -> Runnable::run);
			netHandler.setGetNet(this::getMetadata);
			netHandler.setGetPlayer(n -> n.owner);
			netHandler.setGetPlayerId(Player::getEntityId);
			netHandler.setGetOnlinePlayers(Bukkit::getOnlinePlayers);
			netHandler.setKickPlayer((p, m) -> p.kickPlayer(m.remap()));
			netHandler.setGetPlayerAnimGetters(Player::getFallDistance, Player::isFlying);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void register() {
		netHandler.registerOut(c -> Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, c));
		netHandler.registerIn(c -> Bukkit.getMessenger().registerIncomingPluginChannel(plugin, c, this));
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new BukkitRunnable(){

			@Override
			public void run(){
				netHandler.tick();
			}

		}.runTaskTimer(plugin, 0L, 1L);
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
		netHandler.receiveServer(name, new FastByteArrayInputStream(packet), getMetadata(player));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		try {
			Method addChn = evt.getPlayer().getClass().getDeclaredMethod("addChannel", String.class);
			netHandler.registerOut(c -> addChn.invoke(evt.getPlayer(), c));
		} catch (Exception e) {
			e.printStackTrace();
		}
		evt.getPlayer().setMetadata(PLAYER_DATA, new FixedMetadataValue(plugin, new Meta(evt.getPlayer())));
		netHandler.onJoin(evt.getPlayer());
	}

	@EventHandler
	public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event) {
		if (event.getStatistic() == Statistic.JUMP) {
			netHandler.onJump(event.getPlayer());
		}
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
