package com.tom.cpm.shared.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.function.ToFloatFunction;
import com.tom.cpl.function.TriFunction;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.nbt.NBTTag;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.nbt.NBTTagList;
import com.tom.cpl.nbt.NBTTagString;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.KeybindText;
import com.tom.cpl.text.LiteralText;
import com.tom.cpl.util.Pair;
import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpl.util.TriConsumer;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.BuiltInSafetyProfiles;
import com.tom.cpm.shared.config.ConfigChangeRequest;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey;
import com.tom.cpm.shared.config.PlayerSpecificConfigKey.KeyGroup;
import com.tom.cpm.shared.config.SocialConfig;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.ScalingOptions;

@SuppressWarnings("resource")
public class NetHandler<RL, P, NET> {
	public static final String GET_SKIN = "get_skin";
	public static final String SET_SKIN = "set_skin";
	public static final String HELLO = "hello";
	public static final String SKIN_LAYERS = "layer";
	public static final String SET_SCALE = "set_scl";
	public static final String RECOMMEND_SAFETY = "rec_sfy";
	public static final String SUBSCRIBE_EVENT = "sub_evt";
	public static final String RECEIVE_EVENT = "rec_evt";

	public static final String FORCED_TAG = "forced";
	public static final String DATA_TAG = "data";
	public static final String PROFILE_TAG = "profile";
	public static final String PROFILE_DATA = "data";
	public static final String SERVER_CAPS = "caps";
	public static final String EVENT_LIST = "eventList";
	public static final String KICK_TIME = "kickTime";
	public static final String SCALING = "scaling";

	public static final FormatText FORCED_CHAT_MSG = new FormatText("chat.cpm.skinForced");

	protected Function<P, UUID> getPlayerUUID;
	protected TriConsumer<NET, RL, byte[]> sendPacket;
	protected TriConsumer<P, RL, byte[]> sendToAllTracking;
	protected IntFunction<P> getPlayerById;
	protected BiConsumer<P, IText> sendChat;
	protected BiConsumer<P, Consumer<P>> findTracking;
	protected Function<NET, Executor> executor;
	protected Function<P, Object> playerToLoader;
	protected Supplier<P> getClient;
	protected Function<P, NET> getNet;
	protected Function<NET, P> getPlayer;
	protected BiConsumer<P, IText> kickPlayer;
	protected ToIntFunction<P> getPlayerId;
	protected Consumer<IText> displayText;
	protected Supplier<Collection<? extends P>> getOnlinePlayers;
	protected ToFloatFunction<P> getFallDistance;
	protected Predicate<P> getIsCreativeFlying;
	protected Map<ScalingOptions, BiConsumer<P, Float>> scaleSetters = new EnumMap<>(ScalingOptions.class);

	public final RL helloPacket;
	public final RL setSkin;
	public final RL getSkin;
	public final RL setLayer;
	public final RL setScale;
	public final RL recommendSafety;
	public final RL subEvent;
	public final RL receiveEvent;

	private List<ConfigChangeRequest<?, ?>> recommendedSettingChanges = new ArrayList<>();
	private EnumSet<ServerCaps> serverCaps = EnumSet.noneOf(ServerCaps.class);

	public NetHandler(BiFunction<String, String, RL> keyFactory) {
		helloPacket = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, HELLO);
		setSkin = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, SET_SKIN);
		getSkin = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, GET_SKIN);
		setLayer = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, SKIN_LAYERS);
		setScale = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, SET_SCALE);
		recommendSafety = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, RECOMMEND_SAFETY);
		subEvent = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, SUBSCRIBE_EVENT);
		receiveEvent = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, RECEIVE_EVENT);
	}

	public void onJoin(P player) {
		try {
			IOHelper pb = new IOHelper();
			NBTTagCompound data = new NBTTagCompound();
			int kickTimer = ModConfig.getWorldConfig().getInt(ConfigKeys.KICK_PLAYERS_WITHOUT_MOD, 0);
			data.setInteger(KICK_TIME, kickTimer);
			data.setTag(SERVER_CAPS, writeCaps());

			PlayerData pd = newData();
			getSNetH(player).cpm$setEncodedModelData(pd);
			pd.load(getID(player));

			NBTTagCompound scaling = new NBTTagCompound();
			data.setTag(SCALING, scaling);
			for(ScalingOptions o : scaleSetters.keySet()) {
				float v = pd.scale.getOrDefault(o, 1F);
				if(v != 1) {
					scaling.setFloat(o.getNetKey(), v);
				}
			}

			pb.writeNBT(data);
			sendPacket.accept(getNet.apply(player), helloPacket, pb.toBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private NBTTag writeCaps() {
		NBTTagCompound data = new NBTTagCompound();
		scaleSetters.keySet().stream().map(ScalingOptions::getCaps).filter(e -> e != null).distinct().forEach(c -> setCap(data, c));
		setCap(data, ServerCaps.MODEL_EVENT_SUBS);
		return data;
	}

	private void setCap(NBTTagCompound tag, ServerCaps caps) {
		tag.setBoolean(caps.name().toLowerCase(), true);
	}

	protected PlayerData newData() {
		return new PlayerData();
	}

	@SuppressWarnings("unchecked")
	public void receiveServer(RL key, InputStream data, ServerNetH net) {
		try {
			NET from = (NET) net;
			P pl = getPlayer.apply(from);
			if(key.equals(helloPacket)) {
				executor.apply(from).execute(() -> {
					net.cpm$setHasMod(true);
					findTracking.accept(pl, p -> sendPlayerData(p, pl));
					PlayerData pd = net.cpm$getEncodedModelData();
					if(pd.canChangeModel()) {
						sendPacket.accept(from, getSkin, new byte[0]);
					} else {
						sendPacket.accept(from, setSkin, writeSkinData(pd, pl));
					}
					if(ModConfig.getWorldConfig().getBoolean(ConfigKeys.RECOMMEND_SAFETY_SETTINGS, false)) {
						sendSafetySettings(from);
					}
				});
			} else if(key.equals(setSkin)) {
				PlayerData pd = net.cpm$getEncodedModelData();
				if(pd.canChangeModel()) {
					IOHelper pb = new IOHelper(data);
					NBTTagCompound tag = pb.readNBT();
					executor.apply(from).execute(() -> {
						pd.setModel(tag.hasKey(DATA_TAG) ? tag.getByteArray(DATA_TAG) : null, false, false);
						sendToAllTracking.accept(pl, setSkin, writeSkinData(pd, pl));
						pd.save(getID(pl));
					});
				} else {
					sendChat.accept(pl, FORCED_CHAT_MSG);
				}
			} else if(key.equals(setScale)) {
				IOHelper pb = new IOHelper(data);
				NBTTagCompound tag = pb.readNBT();
				executor.apply(from).execute(() -> {
					PlayerData pd = net.cpm$getEncodedModelData();
					for(Entry<ScalingOptions, BiConsumer<P, Float>> e : scaleSetters.entrySet()) {
						float oldV = pd.scale.getOrDefault(e.getKey(), 1F);
						float newV = tag.getFloat(e.getKey().getNetKey());
						Pair<Float, Float> l = getScalingLimits(e.getKey(), getID(pl));
						newV = newV == 0 || l == null ? 1F : MathHelper.clamp(newV, l.getKey(), l.getValue());
						Log.info("Scaling " + e.getKey() + " " + oldV + " -> " + newV);
						if(newV != oldV) {
							e.getValue().accept(pl, newV);
							pd.scale.put(e.getKey(), newV);
						}
					}
					pd.save(getID(pl));
				});
			} else if(key.equals(subEvent)) {
				IOHelper pb = new IOHelper(data);
				NBTTagCompound tag = pb.readNBT();
				PlayerData pd = net.cpm$getEncodedModelData();
				pd.eventSubs.clear();
				NBTTagList list = tag.getTagList(EVENT_LIST, NBTTag.TAG_STRING);
				for (int i = 0;i<list.tagCount();i++) {
					ModelEventType type = ModelEventType.of(list.getStringTagAt(i));
					if(type != null)pd.eventSubs.add(type);
				}
			}
		} catch (Throwable e) {
			Log.error("Exception while processing cpm packet", e);
		}
	}

	private void sendSafetySettings(NET to) {
		BuiltInSafetyProfiles profile = BuiltInSafetyProfiles.get(ModConfig.getWorldConfig().getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase()));
		if(profile != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString(PROFILE_TAG, profile.name().toLowerCase());
			if(profile == BuiltInSafetyProfiles.CUSTOM) {
				Map<String, Object> map = new HashMap<>();
				ConfigEntry main = ModConfig.getWorldConfig().getEntry(ConfigKeys.SAFETY_SETTINGS);
				ConfigEntry ce = new ConfigEntry(map, () -> {});
				for(PlayerSpecificConfigKey<?> key : ConfigKeys.SAFETY_KEYS) {
					Object v = key.getValue(main, KeyGroup.GLOBAL);
					sendSafetySettings$setValue(ce, key, v);
				}
				tag.setString(PROFILE_DATA, MinecraftObjectHolder.gson.toJson(map));
			}
			try {
				IOHelper pb = new IOHelper();
				pb.writeNBT(tag);
				sendPacket.accept(to, recommendSafety, pb.toBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void sendSafetySettings$setValue(ConfigEntry ce, PlayerSpecificConfigKey<T> key, Object value) {
		key.setValue(ce, (T) value);
	}

	@SuppressWarnings("unchecked")
	public void receiveClient(RL key, InputStream data, NetH net) {
		try {
			NET from = (NET) net;
			if(key.equals(helloPacket)) {
				IOHelper pb = new IOHelper(data);
				NBTTagCompound tag = pb.readNBT();
				NBTTagCompound scaling = tag.getCompoundTag(SCALING);
				Map<ScalingOptions, Float> scalingMap = new EnumMap<>(ScalingOptions.class);
				for(ScalingOptions o : ScalingOptions.VALUES) {
					float v = scaling.getFloat(o.getNetKey());
					scalingMap.put(o, v);
				}
				executor.apply(from).execute(() -> {
					handleServerCaps(tag.getCompoundTag(SERVER_CAPS));
					String server = MinecraftClientAccess.get().getConnectedServer();
					ConfigEntry cc = ModConfig.getCommonConfig();
					ConfigEntry ss = cc.getEntry(ConfigKeys.SERVER_SETTINGS);
					if(server != null && ss.hasEntry(server)) {
						if(ss.getEntry(server).getBoolean(ConfigKeys.DISABLE_NETWORK, false)) {
							int kickTime = tag.getInteger(KICK_TIME);
							if(kickTime > 0) {
								recommendedSettingChanges.clear();
								recommendedSettingChanges.add(new ConfigChangeRequest<>(ConfigKeys.DISABLE_NETWORK, true, false));
								displayText.accept(new FormatText("chat.cpm.serverRequiresCPM", new KeybindText("key.cpm.gestureMenu", "gestureMenu")));
							}
							return;
						}
					}
					net.cpm$setHasMod(true);
					MinecraftClientAccess.get().getDefinitionLoader().clearServerData();
					sendPacket.accept(from, helloPacket, new byte[0]);
					MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine().setServerScaling(scalingMap);
				});
			} else if(net.cpm$hasMod()) {
				if(key.equals(setSkin)) {
					IOHelper pb = new IOHelper(data);
					int plid = pb.readVarInt();
					NBTTagCompound tag = pb.readNBT();
					executor.apply(from).execute(() -> {
						P player = getPlayerById.apply(plid);
						if(player != null) {
							MinecraftClientAccess.get().getDefinitionLoader().setModel(playerToLoader.apply(player), tag.hasKey(DATA_TAG) ? tag.getByteArray(DATA_TAG) : null, tag.getBoolean(FORCED_TAG));
						}
					});
				} else if(key.equals(getSkin)) {
					sendSkinData(from);
				} else if(key.equals(recommendSafety)) {
					IOHelper pb = new IOHelper(data);
					NBTTagCompound tag = pb.readNBT();
					executor.apply(from).execute(() -> {
						String server = MinecraftClientAccess.get().getConnectedServer();
						ConfigEntry cc = ModConfig.getCommonConfig();
						ConfigEntry ss = cc.getEntry(ConfigKeys.SERVER_SETTINGS);
						if(server != null && ss.hasEntry(server)) {
							if(ss.getEntry(server).getBoolean(ConfigKeys.IGNORE_SAFETY_RECOMMENDATIONS, false))return;
						}

						BuiltInSafetyProfiles netProfile = BuiltInSafetyProfiles.get(tag.getString(PROFILE_TAG));

						ConfigEntry ce = null;
						if(netProfile == BuiltInSafetyProfiles.CUSTOM) {
							try {
								Map<String, Object> map = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(tag.getString(PROFILE_DATA), Object.class);
								ce = new ConfigEntry(map, () -> {});
							} catch (Exception e) {
								return;
							}
						}

						recommendedSettingChanges.clear();
						for(PlayerSpecificConfigKey<?> k : ConfigKeys.SAFETY_KEYS) {
							Object rv = ce != null ? k.getValue(ce, KeyGroup.GLOBAL) : k.getValue(netProfile);
							Object sv = k.getValueFor(server, null, cc);
							if(!rv.equals(sv)) {
								recommendedSettingChanges.add(new ConfigChangeRequest<>(k, sv, rv));
							}
						}
						if(!recommendedSettingChanges.isEmpty()) {
							ConfigEntry gs = cc.getEntry(ConfigKeys.GLOBAL_SETTINGS);
							String[] spf = gs.getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase()).split(":", 2);
							BuiltInSafetyProfiles profile = SocialConfig.getProfile(spf);

							if(server != null && ss.hasEntry(server)) {
								ConfigEntry e = ss.getEntry(server);
								if(e.hasEntry(ConfigKeys.SAFETY_PROFILE)) {
									spf = e.getString(ConfigKeys.SAFETY_PROFILE, BuiltInSafetyProfiles.MEDIUM.name().toLowerCase()).split(":", 2);
									profile = SocialConfig.getProfile(spf);
								}
							}

							String old;
							if(profile == BuiltInSafetyProfiles.CUSTOM) {
								old = "custom:" + spf[1];
							} else {
								old = profile.name().toLowerCase();
							}

							if(netProfile == BuiltInSafetyProfiles.CUSTOM) {
								recommendedSettingChanges.add(new ConfigChangeRequest<>(ConfigKeys.SAFETY_PROFILE, old, "custom:import-" + server));
							} else {
								recommendedSettingChanges.add(new ConfigChangeRequest<>(ConfigKeys.SAFETY_PROFILE, old, netProfile.name().toLowerCase()));
							}

							displayText.accept(new FormatText("chat.cpm.serverSafetySettings", new KeybindText("key.cpm.gestureMenu", "gestureMenu")));
						}
					});
				} else if(key.equals(receiveEvent)) {
					IOHelper pb = new IOHelper(data);
					int plid = pb.readVarInt();
					NBTTagCompound tag = pb.readNBT();
					executor.apply(from).execute(() -> {
						P player = getPlayerById.apply(plid);
						if(player != null) {
							Player<?, ?> pl = MinecraftClientAccess.get().getDefinitionLoader().loadPlayer(playerToLoader.apply(player), null);
							pl.animState.receiveEvent(tag, pl.isClientPlayer());
						}
					});
				}
			}
		} catch (Throwable e) {
			Log.error("Exception while processing cpm packet", e);
		}
	}

	private void handleServerCaps(NBTTagCompound tag) {
		serverCaps.clear();
		for(ServerCaps c : ServerCaps.VALUES) {
			if(tag.getBoolean(c.name().toLowerCase())) {
				serverCaps.add(c);
			}
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
				IOHelper pb = new IOHelper();
				NBTTagCompound data = new NBTTagCompound();
				data.setByteArray(DATA_TAG, file.getDataBlock());
				pb.writeNBT(data);
				file.registerLocalCache(MinecraftClientAccess.get().getDefinitionLoader());
				sendPacket.accept(pl, setSkin, pb.toBytes());
			} catch (IOException e) {
			}
		} else {
			try {
				IOHelper pb = new IOHelper();
				NBTTagCompound data = new NBTTagCompound();
				pb.writeNBT(data);
				sendPacket.accept(pl, setSkin, pb.toBytes());
			} catch (IOException e) {
			}
		}
	}

	public void sendPlayerData(P target, P to) {
		PlayerData dt = getSNetH(target).cpm$getEncodedModelData();
		if(dt == null)return;
		sendPacket.accept(getNet.apply(to), setSkin, writeSkinData(dt, target));
	}

	private byte[] writeSkinData(PlayerData dt, P target) {
		try {
			IOHelper pb = new IOHelper();
			pb.writeVarInt(getPlayerId.applyAsInt(target));
			NBTTagCompound data = new NBTTagCompound();
			if(dt.data != null) {
				data.setBoolean(FORCED_TAG, dt.forced);
				data.setByteArray(DATA_TAG, dt.data);
			}
			pb.writeNBT(data);
			return pb.toBytes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSkin(P pl, String skin, boolean force, boolean save) {
		PlayerData pd = getSNetH(pl).cpm$getEncodedModelData();
		pd.setModel(skin, force, save);
		if(skin == null) {
			sendPacket.accept(getNet.apply(pl), getSkin, new byte[0]);
		}
		sendToAllTracking.accept(pl, setSkin, writeSkinData(pd, pl));
		pd.save(getID(pl));
	}

	public void setScale(ScaleData scl) {
		if(hasModClient() && serverCaps.contains(ServerCaps.SCALING)) {
			try {
				if(scl == null)scl = ScaleData.NULL;
				NBTTagCompound nbt = new NBTTagCompound();
				for(Entry<ScalingOptions, Float> e : scl.getScaling().entrySet()) {
					if(serverCaps.contains(e.getKey().getCaps())) {
						nbt.setFloat(e.getKey().getNetKey(), e.getValue());
					}
				}
				IOHelper pb = new IOHelper();
				pb.writeNBT(nbt);
				sendPacket.accept(getClientNet(), setScale, pb.toBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void onRespawn(P pl) {
		PlayerData pd = getSNetH(pl).cpm$getEncodedModelData();
		for(Entry<ScalingOptions, BiConsumer<P, Float>> e : scaleSetters.entrySet()) {
			float sc = pd.scale.getOrDefault(e.getKey(), 1F);
			if(sc != 1 || sc != 0) {
				e.getValue().accept(pl, sc);
			}
		}
	}

	public void tick() {
		int kickTimer = ModConfig.getWorldConfig().getInt(ConfigKeys.KICK_PLAYERS_WITHOUT_MOD, 0);
		for(P p : new ArrayList<>(getOnlinePlayers.get())) {
			ServerNetH net = getSNetH(p);
			PlayerData dt = net.cpm$getEncodedModelData();
			if(dt != null) {
				if(!net.cpm$hasMod()) {
					dt.ticksSinceLogin++;
					if(kickTimer > 0 && dt.ticksSinceLogin > kickTimer) {
						kickPlayer.accept(p, new LiteralText(ModConfig.getWorldConfig().getString(ConfigKeys.KICK_MESSAGE, ConfigKeys.DEFAULT_KICK_MESSAGE)));
					}
				}
				NBTTagCompound evt = new NBTTagCompound();
				if(dt.eventSubs.contains(ModelEventType.FALLING))evt.setFloat(ModelEventType.FALLING.getName(), getFallDistance.apply(p));
				if(dt.eventSubs.contains(ModelEventType.CREATIVE_FLYING))evt.setBoolean(ModelEventType.CREATIVE_FLYING.getName(), getIsCreativeFlying.test(p));
				if(evt.tagCount() > 0) {
					IOHelper h = new IOHelper();
					try {
						h.writeVarInt(getPlayerId.applyAsInt(p));
						h.writeNBT(evt);
						sendToAllTracking.accept(p, receiveEvent, h.toBytes());
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public boolean hasModClient() {
		NET n = getClientNet();
		return n instanceof NetH && ((NetH) n).cpm$hasMod();
	}

	public void onLogOut() {
		recommendedSettingChanges.clear();
		serverCaps.clear();
	}

	public void sendEventSubs(ModelDefinition def) {
		if(serverCaps.contains(ServerCaps.MODEL_EVENT_SUBS)) {
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			tag.setTag(EVENT_LIST, list);
			def.getAnimations().getAnimations().keySet().stream().filter(e -> e instanceof VanillaPose).
			map(ModelEventType::getType).filter(e -> e != null).distinct().map(ModelEventType::getName).
			map(NBTTagString::new).forEach(list::appendTag);
			IOHelper h = new IOHelper();
			try {
				h.writeNBT(tag);
				sendPacket.accept(getClientNet(), subEvent, h.toBytes());
			} catch (IOException e) {
			}
		}
	}

	public void onJump(P p) {
		ServerNetH net = getSNetH(p);
		PlayerData dt = net.cpm$getEncodedModelData();
		if(dt != null && dt.eventSubs.contains(ModelEventType.JUMPING)) {
			NBTTagCompound evt = new NBTTagCompound();
			evt.setBoolean(ModelEventType.JUMPING.getName(), true);
			IOHelper h = new IOHelper();
			try {
				h.writeVarInt(getPlayerId.applyAsInt(p));
				h.writeNBT(evt);
				sendToAllTracking.accept(p, receiveEvent, h.toBytes());
			} catch (IOException e) {
			}
		}
	}

	public String getID(P pl) {
		return getPlayerUUID.apply(pl).toString();
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

	public List<ConfigChangeRequest<?, ?>> getRecommendedSettingChanges() {
		return recommendedSettingChanges;
	}

	public void setGetPlayerUUID(Function<P, UUID> getPlayerUUID) {
		this.getPlayerUUID = getPlayerUUID;
	}

	public <PB> void setSendPacket(Function<byte[], PB> wrapper, TriConsumer<NET, RL, PB> sendPacket, TriConsumer<P, RL, PB> sendToAllTracking) {
		this.sendPacket = (a, b, c) -> sendPacket.accept(a, b, wrapper.apply(c));
		this.sendToAllTracking = (a, b, c) -> sendToAllTracking.accept(a, b, wrapper.apply(c));
	}

	public void setSendPacket(TriConsumer<NET, RL, byte[]> sendPacket, TriConsumer<P, RL, byte[]> sendToAllTracking) {
		this.sendPacket = sendPacket;
		this.sendToAllTracking = sendToAllTracking;
	}

	public void setFindTracking(BiConsumer<P, Consumer<P>> findTracking) {
		this.findTracking = findTracking;
	}

	public void setSendChat(BiConsumer<P, IText> sendChat) {
		this.sendChat = sendChat;
	}

	public void setExecutor(Supplier<Executor> executor) {
		this.executor = v -> executor.get();
	}

	public void setExecutor(Function<NET, Executor> executor) {
		this.executor = executor;
	}

	public void setPlayerToLoader(Function<P, Object> playerToloader) {
		this.playerToLoader = playerToloader;
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

	public void setKickPlayer(BiConsumer<P, IText> kickPlayer) {
		this.kickPlayer = kickPlayer;
	}

	public void setGetPlayerById(IntFunction<P> getPlayerById) {
		this.getPlayerById = getPlayerById;
	}

	public void setGetPlayerId(ToIntFunction<P> getPlayerId) {
		this.getPlayerId = getPlayerId;
	}

	public void setDisplayText(Consumer<IText> displayText) {
		this.displayText = displayText;
	}

	public void setGetOnlinePlayers(Supplier<Collection<? extends P>> getOnlinePlayers) {
		this.getOnlinePlayers = getOnlinePlayers;
	}

	public void setGetPlayerAnimGetters(ToFloatFunction<P> getFallDistance, Predicate<P> getIsCreativeFlying) {
		this.getIsCreativeFlying = getIsCreativeFlying;
		this.getFallDistance = getFallDistance;
	}

	public <E extends Throwable> void registerOut(ThrowingConsumer<RL, E> reg) throws E {
		reg.accept(helloPacket);
		reg.accept(setSkin);
		reg.accept(getSkin);
		reg.accept(recommendSafety);
		reg.accept(receiveEvent);
	}

	public <E extends Throwable> void registerIn(ThrowingConsumer<RL, E> reg) throws E {
		reg.accept(helloPacket);
		reg.accept(setSkin);
		reg.accept(subEvent);
	}

	public <K> void setScaler(ScalerInterface<P, K> intf) {
		for(ScalingOptions opt : ScalingOptions.VALUES) {
			K key = intf.toKey(opt);
			if(key != null)
				scaleSetters.put(opt, (p, v) -> intf.setScale(key, p, v));
		}
	}

	public BiConsumer<P, Float> setScaler(ScalingOptions key, BiConsumer<P, Float> value) {
		return scaleSetters.put(key, value);
	}

	public static interface ScalerInterface<P, K> {
		void setScale(K key, P player, float value);
		K toKey(ScalingOptions opt);
	}

	public boolean isSupported(ScalingOptions o) {
		return scaleSetters.containsKey(o);
	}

	public Pair<Float, Float> getScalingLimits(ScalingOptions o, String id) {
		ConfigEntry e = ModConfig.getWorldConfig();
		ConfigEntry pl = e.getEntry(ConfigKeys.PLAYER_SCALING_SETTINGS);
		ConfigEntry g = e.getEntry(ConfigKeys.SCALING_SETTINGS);
		if(!getValue(pl, g, id, o.name().toLowerCase(), ConfigKeys.ENABLED, ConfigEntry::getBoolean, o.getDefualtEnabled()))
			return null;
		float min = getValue(pl, g, id, o.name().toLowerCase(), ConfigKeys.MIN, ConfigEntry::getFloat, o.getMin());
		float max = getValue(pl, g, id, o.name().toLowerCase(), ConfigKeys.MAX, ConfigEntry::getFloat, o.getMax());
		return Pair.of(min, max);
	}

	private <T> T getValue(ConfigEntry pl, ConfigEntry g, String id, String opt, String key, TriFunction<ConfigEntry, String, T, T> getter, T def) {
		if(pl.hasEntry(id)) {
			pl = pl.getEntry(id);
			if(pl.hasEntry(opt)) {
				pl = pl.getEntry(opt);
				if(pl.hasEntry(key))
					return getter.apply(g, key, def);
			}
		}
		g = g.getEntry(opt);
		return getter.apply(g, key, def);
	}
}
