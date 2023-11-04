package com.tom.cpm.shared.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import com.tom.cpl.nbt.NBTTag;
import com.tom.cpl.nbt.NBTTagCompound;
import com.tom.cpl.nbt.NBTTagList;
import com.tom.cpl.nbt.NBTTagString;
import com.tom.cpl.text.IText;
import com.tom.cpl.text.LiteralText;
import com.tom.cpl.util.ThrowingConsumer;
import com.tom.cpl.util.TriConsumer;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.IManualGesture;
import com.tom.cpm.shared.animation.ServerAnimationState;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.ConfigChangeRequest;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.PlayerData;
import com.tom.cpm.shared.config.PlayerData.AnimationInfo;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.ScaleData;
import com.tom.cpm.shared.network.NetH.ServerNetH;
import com.tom.cpm.shared.network.packet.GestureC2S;
import com.tom.cpm.shared.network.packet.GetSkinS2C;
import com.tom.cpm.shared.network.packet.HelloC2S;
import com.tom.cpm.shared.network.packet.HelloS2C;
import com.tom.cpm.shared.network.packet.PluginMessageC2S;
import com.tom.cpm.shared.network.packet.PluginMessageS2C;
import com.tom.cpm.shared.network.packet.ReceiveEventS2C;
import com.tom.cpm.shared.network.packet.RecommendSafetyS2C;
import com.tom.cpm.shared.network.packet.RequestPlayerC2S;
import com.tom.cpm.shared.network.packet.ScaleInfoS2C;
import com.tom.cpm.shared.network.packet.ServerAnimationS2C;
import com.tom.cpm.shared.network.packet.SetScaleC2S;
import com.tom.cpm.shared.network.packet.SetSkinC2S;
import com.tom.cpm.shared.network.packet.SetSkinS2C;
import com.tom.cpm.shared.network.packet.SubEventC2S;
import com.tom.cpm.shared.util.Log;
import com.tom.cpm.shared.util.ScalingOptions;

public class NetHandler<RL, P, NET> {
	public static final String GET_SKIN = "get_skin";
	public static final String SET_SKIN = "set_skin";
	public static final String HELLO = "hello";
	public static final String SET_SCALE = "set_scl";
	public static final String RECOMMEND_SAFETY = "rec_sfy";
	public static final String SUBSCRIBE_EVENT = "sub_evt";
	public static final String RECEIVE_EVENT = "rec_evt";
	public static final String GESTURE = "gesture";
	public static final String SERVER_ANIMATION = "srv_anim";
	public static final String PLUGIN = "plugin";
	public static final String REQUEST_PLAYER = "req_pl";

	protected Function<P, UUID> getPlayerUUID;
	private TriConsumer<NET, RL, byte[]> sendPacket;
	private TriConsumer<P, RL, byte[]> sendToAllTracking;
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
	protected Map<ScalingOptions, BiConsumer<P, Float>> scaleSetters = new EnumMap<>(ScalingOptions.class);
	protected BiConsumer<? super P, ServerAnimationState> animStateUpdate;

	private List<ConfigChangeRequest<?, ?>> recommendedSettingChanges = new ArrayList<>();
	private EnumSet<ServerCaps> serverCaps = EnumSet.noneOf(ServerCaps.class);
	private boolean scalingWarning;

	protected Map<RL, Supplier<IPacket>> packetS2C = new HashMap<>(), packetC2S = new HashMap<>();
	protected Map<Class<? extends IPacket>, RL> packetLookup = new HashMap<>();
	private BiFunction<String, String, RL> keyFactory;

	public NetHandler(BiFunction<String, String, RL> keyFactory) {
		this.keyFactory = keyFactory;

		register(packetC2S, HELLO, HelloC2S.class, HelloC2S::new);
		register(packetS2C, HELLO, HelloS2C.class, HelloS2C::new);

		register(packetC2S, SET_SKIN, SetSkinC2S.class, SetSkinC2S::new);
		register(packetS2C, SET_SKIN, SetSkinS2C.class, SetSkinS2C::new);

		register(packetS2C, GET_SKIN, GetSkinS2C.class, GetSkinS2C::new);

		register(packetC2S, SET_SCALE, SetScaleC2S.class, SetScaleC2S::new);
		register(packetS2C, SET_SCALE, ScaleInfoS2C.class, ScaleInfoS2C::new);

		register(packetS2C, RECOMMEND_SAFETY, RecommendSafetyS2C.class, RecommendSafetyS2C::new);

		register(packetC2S, SUBSCRIBE_EVENT, SubEventC2S.class, SubEventC2S::new);

		register(packetS2C, RECEIVE_EVENT, ReceiveEventS2C.class, ReceiveEventS2C::new);

		register(packetC2S, GESTURE, GestureC2S.class, GestureC2S::new);

		register(packetS2C, SERVER_ANIMATION, ServerAnimationS2C.class, ServerAnimationS2C::new);

		register(packetC2S, PLUGIN, PluginMessageC2S.class, PluginMessageC2S::new);
		register(packetS2C, PLUGIN, PluginMessageS2C.class, PluginMessageS2C::new);

		register(packetC2S, REQUEST_PLAYER, RequestPlayerC2S.class, RequestPlayerC2S::new);
	}

	@SuppressWarnings("unchecked")
	protected <PCKT extends IPacket> void register(Map<RL, Supplier<IPacket>> map, String name, Class<PCKT> clazz, Supplier<PCKT> factory) {
		RL key = keyFactory.apply(MinecraftObjectHolder.NETWORK_ID, name);
		map.put(key, (Supplier<IPacket>) factory);
		packetLookup.put(clazz, key);
	}

	@SuppressWarnings("unchecked")
	public void onJoin(P player) {
		NBTTagCompound data = new NBTTagCompound();
		int kickTimer = ModConfig.getWorldConfig().getInt(ConfigKeys.KICK_PLAYERS_WITHOUT_MOD, 0);
		data.setInteger(NetworkUtil.KICK_TIME, kickTimer);
		data.setTag(NetworkUtil.SERVER_CAPS, writeCaps());

		ServerNetH net = getSNetH(player);
		PlayerData pd = newData();
		net.cpm$setEncodedModelData(pd);
		pd.load(getID(player));

		NBTTagCompound scaling = new NBTTagCompound();
		data.setTag(NetworkUtil.SCALING, scaling);
		for(ScalingOptions o : scaleSetters.keySet()) {
			float v = pd.scale.getOrDefault(o, 1F);
			if(v != 1) {
				scaling.setFloat(o.getNetKey(), v);
			}
		}

		sendPacketTo0((NET) net, new HelloS2C(data));
	}

	private NBTTag writeCaps() {
		NBTTagCompound data = new NBTTagCompound();
		scaleSetters.keySet().stream().map(ScalingOptions::getCaps).filter(e -> e != null).distinct().forEach(c -> setCap(data, c));
		setCap(data, ServerCaps.MODEL_EVENT_SUBS);
		setCap(data, ServerCaps.GESTURES);
		setCap(data, ServerCaps.PLUGIN_MESSAGES);
		if(ModConfig.getWorldConfig().getBoolean(ConfigKeys.ENABLE_INVIS_GLOW, true))setCap(data, ServerCaps.INVIS_GLOW);
		return data;
	}

	private void setCap(NBTTagCompound tag, ServerCaps caps) {
		tag.setBoolean(caps.name().toLowerCase(Locale.ROOT), true);
	}

	protected PlayerData newData() {
		return new PlayerData();
	}

	public void receiveServer(RL key, InputStream data, ServerNetH net) {
		processPacket(packetC2S, key, data, net);
	}

	public void receiveClient(RL key, InputStream data, NetH net) {
		processPacket(packetS2C, key, data, net);
	}

	private void processPacket(Map<RL, Supplier<IPacket>> map, RL key, InputStream data, NetH net) {
		try {
			Supplier<IPacket> factory = map.get(key);
			if(factory != null) {
				IPacket pckt = factory.get();
				IOHelper h = new IOHelper(data);
				pckt.read(h);
				pckt.handleRaw(this, net);
			}
		} catch (Throwable e) {
			Log.error("Exception while processing cpm packet: " + key, e);
		}
	}

	public void handleServerCaps(NBTTagCompound tag) {
		serverCaps.clear();
		for(ServerCaps c : ServerCaps.VALUES) {
			if(tag.getBoolean(c.name().toLowerCase(Locale.ROOT))) {
				serverCaps.add(c);
			}
		}
	}

	public void sendSkinData() {
		if(hasModClient())
			NetworkUtil.sendSkinDataToServer(this);
	}

	public void setSkin(P pl, String skin, boolean force, boolean save) {
		ServerNetH h = getSNetH(pl);
		PlayerData pd = h.cpm$getEncodedModelData();
		pd.setModel(skin, force, save);
		if(skin == null) {
			sendPacketTo(h, new GetSkinS2C());
		}
		sendPacketToTracking(pl, NetworkUtil.writeSkinData(this, pd, pl));
		pd.save(getID(pl));
	}

	public void setSkin(P pl, byte[] skin, boolean force) {
		PlayerData pd = getSNetH(pl).cpm$getEncodedModelData();
		pd.setModel(skin, force, false);
		sendPacketToTracking(pl, NetworkUtil.writeSkinData(this, pd, pl));
		pd.save(getID(pl));
	}

	public void setScale(ScaleData scl) {
		if(hasModClient() && serverCaps.contains(ServerCaps.SCALING)) {
			if(scl == null)scl = ScaleData.NULL;
			NBTTagCompound nbt = new NBTTagCompound();
			for(Entry<ScalingOptions, Float> e : scl.getScaling().entrySet()) {
				if(serverCaps.contains(e.getKey().getCaps())) {
					nbt.setFloat(e.getKey().getNetKey(), e.getValue());
				}
			}
			sendPacketToServer(new SetScaleC2S(nbt));
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
				updatePlayer(p, dt.state);
				for (ModelEventType type : ModelEventType.SNYC_TYPES) {
					if(dt.eventSubs.contains(type)) {
						type.write(dt.state, evt);
					}
				}
				if(evt.tagCount() > 0) {
					sendPacketToTracking(p, new ReceiveEventS2C(getPlayerId.applyAsInt(p), evt));
				}
			}
		}
	}

	public void updatePlayer(P player, ServerAnimationState state) {
		animStateUpdate.accept(player, state);
	}

	public boolean hasModClient() {
		NET n = getClientNet();
		return n instanceof NetH && ((NetH) n).cpm$hasMod();
	}

	public void onLogOut() {
		recommendedSettingChanges.clear();
		serverCaps.clear();
		scalingWarning = false;
	}

	public void sendEventSubs(ModelDefinition def) {
		if(serverCaps.contains(ServerCaps.MODEL_EVENT_SUBS)) {
			NBTTagCompound tag = new NBTTagCompound();
			{
				NBTTagList list = new NBTTagList();
				tag.setTag(NetworkUtil.EVENT_LIST, list);
				def.getAnimations().getAnimations().keySet().stream().filter(e -> e instanceof VanillaPose).
				map(ModelEventType::getType).filter(e -> e != null).distinct().map(ModelEventType::getName).
				map(NBTTagString::new).forEach(list::appendTag);
			}

			AnimationRegistry reg = def.getAnimations();
			NBTTagList list = new NBTTagList();
			tag.setTag(NetworkUtil.ANIMATIONS, list);
			reg.getCustomPoses().values().forEach(p -> {
				int enc = reg.getEncoded(p);
				if(enc == -1)return;
				addAnimTag(list, p, (byte) enc, 0);
			});
			reg.getGestures().values().forEach(g -> {
				if(g.getType() != AnimationType.GESTURE)return;
				int enc = reg.getEncoded(g);
				if(enc == -1)return;
				addAnimTag(list, g, (byte) enc, 1);
			});
			reg.forEachLayer((g, i) -> addAnimTag(list, g, i.byteValue(), 2));

			sendPacketToServer(new SubEventC2S(tag));
		}
	}

	private void addAnimTag(NBTTagList list, IManualGesture g, byte id, int typeId) {
		NBTTagCompound t = new NBTTagCompound();
		t.setString("name", g.getName());
		t.setByte("id", id);
		t.setByte("type", (byte) ((g.isCommand() ? 16 : 0) | typeId));
		list.appendTag(t);
	}

	public boolean hasServerCap(ServerCaps cap) {
		return hasModClient() && serverCaps.contains(cap);
	}

	public void onJump(P p) {
		onEvent(p, ModelEventType.JUMPING);
	}

	public void onEvent(P p, ModelEventType event) {
		ServerNetH net = getSNetH(p);
		PlayerData dt = net.cpm$getEncodedModelData();
		if(!event.autoSync() && dt != null && dt.eventSubs.contains(event)) {
			NBTTagCompound evt = new NBTTagCompound();
			event.write(dt.state, evt);
			sendPacketToTracking(p, new ReceiveEventS2C(getPlayerId.applyAsInt(p), evt));
		}
	}

	public void playAnimation(P p, String animation, int value) {
		sendPacketTo(getSNetH(p), new ServerAnimationS2C(animation, value));
	}

	public boolean sendPluginMessage(String id, NBTTagCompound msg, int flags) {
		if(hasModClient() && serverCaps.contains(ServerCaps.PLUGIN_MESSAGES)) {
			sendPacketToServer(new PluginMessageC2S(id, msg, flags));
			return true;
		}
		return false;
	}

	public boolean enableInvisGlow() {
		return hasServerCap(ServerCaps.INVIS_GLOW);
	}

	public int getAnimationPlaying(P player, String animation) {
		PlayerData pd = getSNetH(player).cpm$getEncodedModelData();
		AnimationInfo id = pd.animNames.get(animation);
		if(id == null)return -1;
		switch (id.type & 0xf) {
		case 0://Pose
			return pd.gestureData[0] == id.id ? 1 : 0;
		case 1://Gesture
			return pd.gestureData[1] == id.id ? 1 : 0;
		case 2://Layer
			if (id.id > 1 && id.id < pd.gestureData.length)return Byte.toUnsignedInt(pd.gestureData[id.id]);
			else return -1;
		default:
			return -1;
		}
	}

	public void requestPlayerState(UUID other) {
		if (!hasModClient())return;
		sendPacketToServer(new RequestPlayerC2S(other, false));
	}

	public void requestPlayerData(UUID other) {
		if (!hasModClient())return;
		sendPacketToServer(new RequestPlayerC2S(other, true));
	}

	public String getID(P pl) {
		return getPlayerUUID.apply(pl).toString();
	}

	public NET getClientNet() {
		return getNet.apply(getClient.get());
	}

	public ServerNetH getSNetH(P player) {
		return (ServerNetH) getNet.apply(player);
	}

	public void sendPlayerData(P target, P to) {
		NetworkUtil.sendPlayerData(this, target, to);
	}

	@SuppressWarnings("unchecked")
	public void execute(NetH net, Runnable task) {
		executor.apply((NET) net).execute(task);
	}

	public List<ConfigChangeRequest<?, ?>> getRecommendedSettingChanges() {
		return recommendedSettingChanges;
	}

	public void setGetPlayerUUID(Function<P, UUID> getPlayerUUID) {
		this.getPlayerUUID = getPlayerUUID;
	}

	public <PB> void setSendPacketDirect(Function<byte[], PB> wrapper, TriConsumer<NET, RL, PB> sendPacket, TriConsumer<P, RL, PB> sendToAllTracking) {
		this.sendPacket = (a, b, c) -> sendPacket.accept(a, b, wrapper.apply(c));
		this.sendToAllTracking = (a, b, c) -> sendToAllTracking.accept(a, b, wrapper.apply(c));
	}

	public void setSendPacketDirect(TriConsumer<NET, RL, byte[]> sendPacket, TriConsumer<P, RL, byte[]> sendToAllTracking) {
		this.sendPacket = sendPacket;
		this.sendToAllTracking = sendToAllTracking;
	}

	public <PB> void setSendPacketClient(Function<byte[], PB> wrapper, TriConsumer<NET, RL, PB> sendPacket) {
		this.sendPacket = (a, b, c) -> sendPacket.accept(a, b, wrapper.apply(c));
	}

	public void setSendPacketClient(TriConsumer<NET, RL, byte[]> sendPacket) {
		this.sendPacket = sendPacket;
	}

	private void sendPacketServer(P to, RL pck, byte[] data) {
		NET n = getNet.apply(to);
		if(n instanceof ServerNetH && ((ServerNetH)n).cpm$hasMod()) {
			sendPacket.accept(n, pck, data);
		}
	}

	public <PB, C> void setSendPacketServer(Function<byte[], PB> wrapper, TriConsumer<NET, RL, PB> sendPacket, Function<P, Collection<C>> forEachTracking, Function<C, P> toPlayer) {
		this.sendPacket = (a, b, c) -> sendPacket.accept(a, b, wrapper.apply(c));
		this.sendToAllTracking = (p, rl, d) -> {
			for (C t : forEachTracking.apply(p)) {
				sendPacketServer(toPlayer.apply(t), rl, d);
			}
			sendPacketServer(p, rl, d);
		};
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

	public void setGetPlayerAnimGetters(BiConsumer<? super P, ServerAnimationState> animStateUpdate) {
		this.animStateUpdate = animStateUpdate;
	}

	public <E extends Throwable> void registerOut(ThrowingConsumer<RL, E> reg) throws E {
		for (RL e : packetS2C.keySet()) {
			reg.accept(e);
		}
	}

	public <E extends Throwable> void registerIn(ThrowingConsumer<RL, E> reg) throws E {
		for (RL e : packetC2S.keySet()) {
			reg.accept(e);
		}
	}

	public <K> void setScaler(ScalerInterface<P, K> intf) {
		for(ScalingOptions opt : ScalingOptions.VALUES) {
			K key;
			try {
				key = intf.toKey(opt);
			} catch (Throwable e) {
				Log.warn("Failed to create scaler key for " + opt.name().toLowerCase(Locale.ROOT) + ". Make sure your scaling supported mods are up to date!", e);
				continue;
			}
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

	@SuppressWarnings("unchecked")
	public P getPlayer(ServerNetH net) {
		return getPlayer.apply((NET) net);
	}

	public void forEachTracking(P pl, Consumer<P> cons) {
		findTracking.accept(pl, cons);
	}

	@SuppressWarnings("unchecked")
	public void sendPacketTo(ServerNetH net, IPacket packet) {
		if(!net.cpm$hasMod())return;
		sendPacketTo0((NET) net, packet);
	}

	public void sendPacketToServer(IPacket packet) {
		if(!hasModClient())return;
		sendPacketTo0(getClientNet(), packet);
	}

	private void sendPacketTo0(NET net, IPacket packet) {
		RL id = packetLookup.get(packet.getClass());
		if(id == null)return;
		byte[] data;
		try {
			data = packet2byte(packet);
		} catch (IOException e) {
			return;
		}
		sendPacket.accept(net, id, data);
	}

	public void sendPacketToTracking(P player, IPacket packet) {
		RL id = packetLookup.get(packet.getClass());
		if(id == null)return;
		byte[] data;
		try {
			data = packet2byte(packet);
		} catch (IOException e) {
			return;
		}
		sendToAllTracking.accept(player, id, data);
	}

	private byte[] packet2byte(IPacket pckt) throws IOException {
		IOHelper h = new IOHelper();
		pckt.write(h);
		return h.toBytes();
	}

	public int getPlayerId(P target) {
		return getPlayerId.applyAsInt(target);
	}

	public void sendChat(P player, IText chatMsg) {
		sendChat.accept(player, chatMsg);
	}

	public Map<ScalingOptions, BiConsumer<P, Float>> getScaleSetters() {
		return scaleSetters;
	}

	public void displayText(IText text) {
		displayText.accept(text);
	}

	public P getPlayerById(int entityId) {
		return getPlayerById.apply(entityId);
	}

	public P getPlayerByUUID(UUID uuid) {
		return getOnlinePlayers.get().stream().filter(p -> uuid.equals(getPlayerUUID.apply(p))).findFirst().orElse(null);
	}

	public Object getLoaderId(P player) {
		return playerToLoader.apply(player);
	}

	public void setScalingWarning() {
		this.scalingWarning = true;
	}

	public boolean hasScalingWarning() {
		return scalingWarning;
	}
}
