package com.tom.cpm.shared.network;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.util.TriConsumer;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.util.Log;

public class NetHandler<RL, NBT, P, PB, NET> {
	public static final String GET_SKIN = "get_skin";
	public static final String SET_SKIN = "set_skin";
	public static final String HELLO = "hello";
	public static final String SKIN_LAYERS = "layer";
	public static final String SET_SCALE = "set_scl";

	public static final String FORCED_TAG = "forced";
	public static final String DATA_TAG = "data";
	public static final String SCALE_TAG = "scale";

	public static final String FORCED_CHAT_MSG = "chat.cpm.skinForced";

	protected Supplier<PB> newPacketBuffer;
	protected Supplier<NBT> newNbt;
	protected Function<P, UUID> getPlayerUUID;
	protected BiConsumer<PB, NBT> writeCompound;
	protected Function<PB, NBT> readCompound;
	protected TriConsumer<NET, RL, PB> sendPacket;
	protected TriConsumer<P, RL, PB> sendToAllTracking;
	protected BiConsumer<PB, P> writePlayerId;
	protected Function<PB, Object> readPlayerId;
	protected Function<Object, P> getPlayerById;
	protected NBTSetter<NBT, Boolean> setBoolean;
	protected NBTSetter<NBT, byte[]> setByteArray;
	protected NBTSetter<NBT, Float> setFloat;
	protected NBTGetter<NBT, Boolean> getBoolean;
	protected NBTGetter<NBT,  byte[]> getByteArray;
	protected NBTGetter<NBT,  Float> getFloat;
	protected BiPredicate<NBT, String> contains;
	protected BiConsumer<P, String> sendChat;
	protected BiConsumer<P, Consumer<P>> findTracking;
	protected Function<NET, Executor> executor;
	protected Function<P, Object> playerToLoader;
	protected BiConsumer<P, Float> scaleSetter;
	protected Supplier<P> getClient;
	protected Function<P, NET> getNet;
	protected Function<NET, P> getPlayer;

	public final RL helloPacket;
	public final RL setSkin;
	public final RL getSkin;
	public final RL setLayer;
	public final RL setScale;

	public NetHandler(BiFunction<String, String, RL> keyFactory) {
		helloPacket = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, HELLO);
		setSkin = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, SET_SKIN);
		getSkin = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, GET_SKIN);
		setLayer = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, SKIN_LAYERS);
		setScale = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, SET_SCALE);
	}

	public void onJoin(P player) {
		PB pb = newPacketBuffer.get();
		NBT data = newNbt.get();
		writeCompound.accept(pb, data);
		PlayerData pd = newData();
		getSNetH(player).cpm$setEncodedModelData(pd);
		sendPacket.accept(getNet.apply(player), helloPacket, pb);
		pd.load(getPlayerUUID.apply(player).toString());
	}

	protected PlayerData newData() {
		return new PlayerData();
	}

	@SuppressWarnings("unchecked")
	public void receiveServer(RL key, PB data, ServerNetH net) {
		try {
			NET from = (NET) net;
			P pl = getPlayer.apply(from);
			if(key.equals(helloPacket)) {
				executor.apply(from).execute(() -> {
					net.cpm$setHasMod(true);
					findTracking.accept(pl, p -> sendPlayerData(p, pl));
					PlayerData pd = net.cpm$getEncodedModelData();
					if(pd.canChangeModel()) {
						sendPacket.accept(from, getSkin, newPacketBuffer.get());
					} else {
						sendPacket.accept(from, setSkin, writeSkinData(pd, pl));
					}
				});
			} else if(key.equals(setSkin)) {
				PlayerData pd = net.cpm$getEncodedModelData();
				if(pd.canChangeModel()) {
					NBT tag = readCompound.apply(data);
					executor.apply(from).execute(() -> {
						pd.setModel(contains.test(tag, DATA_TAG) ? getByteArray.get(tag, DATA_TAG) : null, false, false);
						sendToAllTracking.accept(pl, setSkin, writeSkinData(pd, pl));
						pd.save(getPlayerUUID.apply(pl).toString());
					});
				} else {
					sendChat.accept(pl, FORCED_CHAT_MSG);
				}
			} else if(key.equals(setScale)) {
				NBT tag = readCompound.apply(data);
				float scale = getFloat.get(tag, SCALE_TAG);
				if(scaleSetter != null) {
					scaleSetter.accept(pl, scale);
					net.cpm$getEncodedModelData().scale = scale;
				}
			}
		} catch (Throwable e) {
			Log.error("Exception while processing cpm packet", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void receiveClient(RL key, PB data, NetH net) {
		try {
			NET from = (NET) net;
			if(key.equals(helloPacket)) {
				NBT nbt = readCompound.apply(data);
				executor.apply(from).execute(() -> {
					net.cpm$setHasMod(true);
					MinecraftClientAccess.get().getDefinitionLoader().clearServerData();
					sendPacket.accept(from, helloPacket, newPacketBuffer.get());
				});
			} else if(key.equals(setSkin)) {
				Object pl = readPlayerId.apply(data);
				NBT tag = readCompound.apply(data);
				executor.apply(from).execute(() -> {
					P player = getPlayerById.apply(pl);
					if(player != null) {
						MinecraftClientAccess.get().getDefinitionLoader().setModel(playerToLoader.apply(player), contains.test(tag, DATA_TAG) ? getByteArray.get(tag, DATA_TAG) : null, getBoolean.get(tag, FORCED_TAG));
					}
				});
			} else if(key.equals(getSkin)) {
				sendSkinData(from);
			}
		} catch (Throwable e) {
			Log.error("Exception while processing cpm packet", e);
		}
	}

	public void sendSkinData() {
		if(hasModClient())
			sendSkinData(getClientNet());
	}

	private void sendSkinData(NET pl) {
		String model = ModConfig.getCommonConfig().getString(ConfigKeys.SELECTED_MODEL, null);
		if(model != null) {
			File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
			try {
				ModelFile file = ModelFile.load(new File(modelsDir, model));
				PB pb = newPacketBuffer.get();
				NBT data = newNbt.get();
				setByteArray.set(data, DATA_TAG, file.getDataBlock());
				writeCompound.accept(pb, data);
				sendPacket.accept(pl, setSkin, pb);
				file.registerLocalCache(MinecraftClientAccess.get().getDefinitionLoader());
			} catch (IOException e) {
			}
		} else {
			PB pb = newPacketBuffer.get();
			NBT data = newNbt.get();
			writeCompound.accept(pb, data);
			sendPacket.accept(pl, setSkin, pb);
		}
	}

	public void sendPlayerData(P target, P to) {
		PlayerData dt = getSNetH(target).cpm$getEncodedModelData();
		if(dt == null)return;
		sendPacket.accept(getNet.apply(to), setSkin, writeSkinData(dt, target));
	}

	private PB writeSkinData(PlayerData dt, P target) {
		PB pb = newPacketBuffer.get();
		writePlayerId.accept(pb, target);
		NBT data = newNbt.get();
		if(dt.data != null) {
			setBoolean.set(data, FORCED_TAG, dt.forced);
			setByteArray.set(data, DATA_TAG, dt.data);
		}
		writeCompound.accept(pb, data);
		return pb;
	}

	public void onCommand(P pl, String skin, boolean force, boolean save) {
		PlayerData pd = getSNetH(pl).cpm$getEncodedModelData();
		pd.setModel(skin, force, save);
		if(skin == null) {
			sendPacket.accept(getNet.apply(pl), getSkin, newPacketBuffer.get());
		}
		sendToAllTracking.accept(pl, setSkin, writeSkinData(pd, pl));
		pd.save(getPlayerUUID.apply(pl).toString());
	}

	public void setScale(float scl) {
		if(hasModClient()) {
			PB pb = newPacketBuffer.get();
			NBT nbt = newNbt.get();
			setFloat.set(nbt, SCALE_TAG, scl);
			writeCompound.accept(pb, nbt);
			sendPacket.accept(getClientNet(), setScale, pb);
		}
	}

	public void onRespawn(P pl) {
		if(scaleSetter != null) {
			float sc = getSNetH(pl).cpm$getEncodedModelData().scale;
			if(sc != 1 || sc != 0) {
				scaleSetter.accept(pl, sc);
			}
		}
	}

	public boolean hasModClient() {
		NET n = getClientNet();
		return n instanceof NetH && ((NetH) n).cpm$hasMod();
	}

	public P getClient() {
		return getClient.get();
	}

	public NET getClientNet() {
		return getNet.apply(getClient());
	}

	private ServerNetH getSNetH(P player) {
		return (ServerNetH) getNet.apply(player);
	}

	public void setNewPacketBuffer(Supplier<PB> newPacketBuffer) {
		this.newPacketBuffer = newPacketBuffer;
	}

	public void setNewNbt(Supplier<NBT> newNbt) {
		this.newNbt = newNbt;
	}

	public void setGetPlayerUUID(Function<P, UUID> getPlayerUUID) {
		this.getPlayerUUID = getPlayerUUID;
	}

	public void setWriteCompound(BiConsumer<PB, NBT> writeCompound, Function<PB, NBT> readCompound) {
		this.writeCompound = writeCompound;
		this.readCompound = readCompound;
	}

	public void setSendPacket(TriConsumer<NET, RL, PB> sendPacket, TriConsumer<P, RL, PB> sendToAllTracking) {
		this.sendPacket = sendPacket;
		this.sendToAllTracking = sendToAllTracking;
	}

	public void setWritePlayerId(BiConsumer<PB, P> writePlayerId) {
		this.writePlayerId = writePlayerId;
	}

	public void setNBTSetters(NBTSetter<NBT, Boolean> setBoolean, NBTSetter<NBT, byte[]> setByteArray, NBTSetter<NBT, Float> setFloat) {
		this.setBoolean = setBoolean;
		this.setByteArray = setByteArray;
		this.setFloat = setFloat;
	}

	public void setNBTGetters(NBTGetter<NBT, Boolean> getBoolean, NBTGetter<NBT, byte[]> getByteArray, NBTGetter<NBT, Float> getFloat) {
		this.getBoolean = getBoolean;
		this.getByteArray = getByteArray;
		this.getFloat = getFloat;
	}

	public void setContains(BiPredicate<NBT, String> contains) {
		this.contains = contains;
	}

	public void setFindTracking(BiConsumer<P, Consumer<P>> findTracking) {
		this.findTracking = findTracking;
	}

	public void setSendChat(BiConsumer<P, String> sendChat) {
		this.sendChat = sendChat;
	}

	public void setExecutor(Supplier<Executor> executor) {
		this.executor = v -> executor.get();
	}

	public void setExecutor(Function<NET, Executor> executor) {
		this.executor = executor;
	}

	@SuppressWarnings("unchecked")
	public <T> void setReadPlayerId(Function<PB, T> readPlayerId, Function<T, P> getPlayerById) {
		this.readPlayerId = (Function<PB, Object>) readPlayerId;
		this.getPlayerById = (Function<Object, P>) getPlayerById;
	}

	public void setPlayerToLoader(Function<P, Object> playerToloader) {
		this.playerToLoader = playerToloader;
	}

	public void setScaleSetter(BiConsumer<P, Float> scaleSetter) {
		this.scaleSetter = scaleSetter;
	}

	public void setGetClient(Supplier<P> getClient) {
		this.getClient = getClient;
	}

	public void setGetNet(Function<P, NET> getNet) {
		this.getNet = getNet;
	}

	public void setGetPlayer(Function<NET, P> getPlayer) {
		this.getPlayer = getPlayer;
	}

	@FunctionalInterface
	public interface NBTSetter<NBT, T> {
		void set(NBT nbt, String key, T value);
	}

	@FunctionalInterface
	public interface NBTGetter<NBT, T> {
		T get(NBT nbt, String key);
	}

	public static <RL, P, NET> void initBuiltin(NetHandler<RL, NBTTagCompound, P, IOHelper, NET> netHandler,
			ToIntFunction<P> getId, TriConsumer<NET, RL, byte[]> sendPacket,
			TriConsumer<P, RL, byte[]> sendToAllTracking) {
		netHandler.setNewNbt(NBTTagCompound::new);
		netHandler.setNewPacketBuffer(IOHelper::new);
		netHandler.setWritePlayerId((b, pl) -> {
			try {
				b.writeVarInt(getId.applyAsInt(pl));
			} catch (IOException e) {
				Log.error("Error writing packet", e);
			}
		});
		netHandler.setNBTSetters(NBTTagCompound::setBoolean, NBTTagCompound::setByteArray, NBTTagCompound::setFloat);
		netHandler.setNBTGetters(NBTTagCompound::getBoolean, NBTTagCompound::getByteArray, NBTTagCompound::getFloat);
		netHandler.setContains(NBTTagCompound::hasKey);
		netHandler.setSendPacket((pl, pck, dt) -> {
			try {
				sendPacket.accept(pl, pck, dt.toBytes());
			} catch (IOException e) {
				Log.error("Error writing packet", e);
			}
		}, (pl, pck, dt) -> {
			try {
				sendToAllTracking.accept(pl, pck, dt.toBytes());
			} catch (IOException e) {
				Log.error("Error writing packet", e);
			}
		});
		netHandler.setWriteCompound((t, u) -> {
			try {
				t.writeNBT(u);
			} catch (IOException e) {
				Log.error("Error writing packet", e);
			}
		}, t -> {
			try {
				return t.readNBT();
			} catch (IOException e) {
				Log.error("Error reading packet", e);
				return new NBTTagCompound();
			}
		});
	}
}
