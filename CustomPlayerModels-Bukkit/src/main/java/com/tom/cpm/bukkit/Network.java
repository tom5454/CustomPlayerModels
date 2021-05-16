package com.tom.cpm.bukkit;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.tom.cpl.util.Util;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.NetHandler;
import com.tom.cpm.shared.network.NetHandler.NBTGetter;
import com.tom.cpm.shared.network.NetHandler.NBTSetter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Network implements PluginMessageListener, Listener {
	public static final String PLAYER_DATA = MinecraftObjectHolder.NETWORK_ID + ":data";
	private final CPMBukkitPlugin plugin;
	private NetHandler<String, Object, Player, ByteBuf, Meta> netHandler;

	private Class<?> NBTTagCompound, PacketDataSerializer;
	private Method setBoolean, setByteArray, setFloat, writeCompound, readCompound, getBoolean, getByteArray, getFloat, hasKey;
	private Function<ByteBuf, ByteBuf> newPB;

	@SuppressWarnings("unchecked")
	public Network(CPMBukkitPlugin plugin) {
		this.plugin = plugin;
		try {
			String pckg;
			if(Bukkit.getServer() != null) {
				Field console = Bukkit.getServer().getClass().getDeclaredField("console");
				console.setAccessible(true);
				Object dedicatedServer = console.get(Bukkit.getServer());
				String dedicatedServerClazz = dedicatedServer.getClass().getName();
				int ind = dedicatedServerClazz.lastIndexOf('.');
				pckg = dedicatedServerClazz.substring(0, ind + 1);
			} else {
				pckg = "net.minecraft.server.v1_16_R3.";
			}
			NBTTagCompound = Class.forName(pckg + "NBTTagCompound");

			PacketDataSerializer = Class.forName(pckg + "PacketDataSerializer");

			setBoolean = NBTTagCompound.getDeclaredMethod("setBoolean", String.class, boolean.class);
			setByteArray = NBTTagCompound.getDeclaredMethod("setByteArray", String.class, byte[].class);
			setFloat = NBTTagCompound.getDeclaredMethod("setFloat", String.class, float.class);
			getBoolean = NBTTagCompound.getDeclaredMethod("getBoolean", String.class);
			getByteArray = NBTTagCompound.getDeclaredMethod("getByteArray", String.class);
			getFloat = NBTTagCompound.getDeclaredMethod("getFloat", String.class);
			hasKey = NBTTagCompound.getDeclaredMethod("hasKey", String.class);

			for(Method m : PacketDataSerializer.getDeclaredMethods()) {
				if(m.getParameterCount() == 1) {
					if(m.getParameters()[0].getType() == NBTTagCompound) {
						writeCompound = m;
					}
				} else if(m.getParameterCount() == 0 && m.getReturnType() == NBTTagCompound) {
					readCompound = m;
				}
			}

			if(writeCompound == null)throw new NoSuchMethodError("PacketDataSerializer.writeCompound");
			if(readCompound == null)throw new NoSuchMethodError("PacketDataSerializer.readCompound");
			netHandler = new NetHandler<>((k, v) -> k + ":" + v);
			netHandler.setNewNbt((Supplier<Object>) Util.constructor(NBTTagCompound));
			newPB = (Function<ByteBuf, ByteBuf>) Util.constructor(PacketDataSerializer, ByteBuf.class);
			netHandler.setNewPacketBuffer(() -> newPB.apply(Unpooled.buffer()));
			netHandler.setIsDedicatedServer(p -> true);
			netHandler.setGetPlayerUUID(Player::getUniqueId);
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodHandle writeC = lookup.unreflect(writeCompound);
			MethodHandle readC = lookup.unreflect(readCompound);
			MethodHandle setBool = lookup.unreflect(setBoolean);
			MethodHandle setByteArr = lookup.unreflect(setByteArray);
			MethodHandle setFlt = lookup.unreflect(setFloat);
			MethodHandle getBool = lookup.unreflect(getBoolean);
			MethodHandle getByteArr = lookup.unreflect(getByteArray);
			MethodHandle getFlt = lookup.unreflect(getFloat);
			MethodHandle cont = lookup.unreflect(hasKey);
			BiFunction<ByteBuf, Object, Object> writeComp =
					(BiFunction<ByteBuf, Object, Object>) LambdaMetafactory.metafactory(lookup, "apply",
							MethodType.methodType(BiFunction.class),
							MethodType.methodType(Object.class, Object.class, Object.class), writeC, writeC.type()).getTarget().invoke();
			netHandler.setWriteCompound(writeComp::apply,
					(Function<ByteBuf, Object>) LambdaMetafactory.metafactory(lookup, "apply",
							MethodType.methodType(Function.class),
							MethodType.methodType(Object.class, Object.class), readC, readC.type()).getTarget().invoke());
			netHandler.setSendPacket((pl, pck, dt) -> pl.owner.sendPluginMessage(plugin, pck, dt.array()), this::sendToAllTrackingAndSelf);
			netHandler.setWritePlayerId((b, pl) -> writeVarInt(b, pl.getEntityId()));
			netHandler.setNBTSetters(
					(n, k, v) -> invoke(setBoolean, n, k, v),
					(NBTSetter<Object, byte[]>) LambdaMetafactory.metafactory(lookup, "set",
							MethodType.methodType(NBTSetter.class),
							MethodType.methodType(void.class, Object.class, String.class, Object.class), setByteArr, setByteArr.type()).getTarget().invoke(),
					(n, k, v) -> invoke(setFloat, n, k, v));
			netHandler.setNBTGetters(
					(NBTGetter<Object, Boolean>) LambdaMetafactory.metafactory(lookup, "get",
							MethodType.methodType(NBTGetter.class),
							MethodType.methodType(Object.class, Object.class, String.class), getBool, getBool.type()).getTarget().invoke(),
					(NBTGetter<Object, byte[]>) LambdaMetafactory.metafactory(lookup, "get",
							MethodType.methodType(NBTGetter.class),
							MethodType.methodType(Object.class, Object.class, String.class), getByteArr, getByteArr.type()).getTarget().invoke(),
					(NBTGetter<Object, Float>) LambdaMetafactory.metafactory(lookup, "get",
							MethodType.methodType(NBTGetter.class),
							MethodType.methodType(Object.class, Object.class, String.class), getFlt, getFlt.type()).getTarget().invoke());
			netHandler.setContains((BiPredicate<Object, String>) LambdaMetafactory.metafactory(lookup, "test",
					MethodType.methodType(BiPredicate.class),
					MethodType.methodType(boolean.class, Object.class, Object.class), cont, cont.type()).getTarget().invoke());
			netHandler.setFindTracking((p, c) -> getPlayersWithin(p, 64, c));
			netHandler.setSendChat((pl, msg) -> pl.sendMessage(plugin.i18n.format(msg)));
			netHandler.setExecutor(() -> Runnable::run);
			netHandler.setGetNet(this::getMetadata);
			netHandler.setGetPlayer(n -> n.owner);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private Object invoke(Method m, Object obj, Object... args) {
		try {
			return m.invoke(obj, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
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

	private void sendToAllTrackingAndSelf(Player player, String packet, ByteBuf dataIn) {
		byte[] data = dataIn.array();
		getPlayersWithin(player, 64, pl -> {
			if(getMetadata(pl).cpm$hasMod()) {
				pl.sendPluginMessage(plugin, packet, data);
			}
		});
	}

	@Override
	public void onPluginMessageReceived(String paramString, Player paramPlayer, byte[] paramArrayOfByte) {
		netHandler.receiveServer(paramString, newPB.apply(Unpooled.wrappedBuffer(paramArrayOfByte)), getMetadata(paramPlayer));
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

	public static void writeVarInt(ByteBuf bb, int toWrite) {
		while ((toWrite & -128) != 0) {
			bb.writeByte(toWrite & 127 | 128);
			toWrite >>>= 7;
		}

		bb.writeByte(toWrite);
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
		System.out.println("Created player data for player");
		Meta mt = new Meta(player);
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

	public static void main(String[] args) {
		new Network(null);
	}
}
