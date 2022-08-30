package com.tom.cpm;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import net.minecraftforge.versions.forge.ForgeVersion;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.text.TextStyle;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.VersionCheck;

@Mod("cpm")
public class CustomPlayerModels implements MinecraftCommonAccess {

	public CustomPlayerModels() {
		api = new CPMApiManager();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new ServerHandler());
	}

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);

	public static CPMApiManager api;

	private void doClientStuff(final FMLClientSetupEvent event) {
		CustomPlayerModelsClient.INSTANCE.init();
	}

	private ModConfigFile cfg;

	public void setup(FMLCommonSetupEvent evt) {
		cfg = new ModConfigFile(new File(FMLPaths.CONFIGDIR.get().toFile(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);
		LOG.info("Customizable Player Models Initialized");
	}

	@SuppressWarnings("unchecked")
	private void processIMC(final InterModProcessEvent event) {
		event.getIMCStream().forEach(m -> {
			try {
				if(m.method().equals("api")) {
					ICPMPlugin plugin = ((Supplier<ICPMPlugin>) m.messageSupplier().get()).get();
					api.register(plugin);
				}
			} catch (Throwable e) {
				LOG.error("Mod {} provides a broken implementation of CPM api", m.senderModId(), e);
			}
		});
		LOG.info("Customizable Player Models IMC processed: " + api.getPluginStatus());
		api.buildCommon().player(Player.class).init();
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CustomPlayerModelsClient::apiInit);
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@SubscribeEvent
	public void onStart(FMLServerStartingEvent e) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(e.getServer()));
	}

	@SubscribeEvent
	public void onStop(FMLServerStoppingEvent e) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}

	@Override
	public ILogger getLogger() {
		return log;
	}

	private static final EnumSet<PlatformFeature> features = EnumSet.of(
			PlatformFeature.EDITOR_HELD_ITEM,
			PlatformFeature.EDITOR_SUPPORTED
			);

	@Override
	public EnumSet<PlatformFeature> getSupportedFeatures() {
		return features;
	}

	@Override
	public String getMCVersion() {
		return SharedConstants.getCurrentVersion().getName();
	}

	@Override
	public String getMCBrand() {
		return "(forge/" + ForgeVersion.getVersion() + ")";
	}

	@Override
	public String getModVersion() {
		return ModList.get().getModContainerById("cpm").map(m -> m.getModInfo().getVersion().toString()).orElse("?UNKNOWN?");
	}

	@Override
	public TextRemapper<MutableComponent> getTextRemapper() {
		return new TextRemapper<>(TranslatableComponent::new, TextComponent::new, MutableComponent::append, KeybindComponent::new,
				CustomPlayerModels::styleText);
	}

	private static MutableComponent styleText(MutableComponent in, TextStyle style) {
		return in.withStyle(Style.EMPTY.withBold(style.bold).withItalic(style.italic).withUnderlined(style.underline).withStrikethrough(style.strikethrough));
	}

	@Override
	public CPMApiManager getApi() {
		return api;
	}

	@Override
	public IVersionCheck getVersionCheck() {
		return VersionCheck.get(() -> ModList.get().getModContainerById("cpm").map(c -> VersionChecker.getResult(c.getModInfo()).changes()).orElse(Collections.emptyMap()));
	}
}
