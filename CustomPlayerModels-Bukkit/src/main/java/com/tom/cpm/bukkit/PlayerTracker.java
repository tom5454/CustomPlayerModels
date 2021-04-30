package com.tom.cpm.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.tom.cpm.bukkit.Network.Meta;

public class PlayerTracker implements Runnable {
	private Network net;
	public PlayerTracker(Network net) {
		this.net = net;
	}

	@Override
	public void run() {
		for(Player pl : Bukkit.getOnlinePlayers()) {
			Meta mt = net.getMetadata(pl);
			if(mt.cpm$hasMod()) {
				List<Player> nearbyPlayers = new ArrayList<>();
				Network.getPlayersWithin(pl, 64, nearbyPlayers::add);
				for (Player player : nearbyPlayers) {
					if(!mt.trackingPlayers.contains(player)) {
						net.onTrackingStart(pl, player);
					}
				}
				mt.trackingPlayers.clear();
				mt.trackingPlayers.addAll(nearbyPlayers);
			}
		}
	}
}
