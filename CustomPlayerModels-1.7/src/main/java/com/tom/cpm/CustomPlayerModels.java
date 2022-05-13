package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import net.minecraftforge.common.MinecraftForge;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = CustomPlayerModels.ID, acceptableRemoteVersions = "*", guiFactory = "com.tom.cpm.Config")
public class CustomPlayerModels implements MinecraftCommonAccess {
	public static final String ID = "customplayermodels";

	@SidedProxy(clientSide = "com.tom.cpm.client.ClientProxy", serverSide = "com.tom.cpm.CommonProxy")
	public static CommonProxy proxy;

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);
	public static CPMApiManager api;

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		api = new CPMApiManager();
		proxy.init();
		ServerHandler sh = new ServerHandler();
		MinecraftForge.EVENT_BUS.register(sh);
		FMLCommonHandler.instance().bus().register(sh);
		LOG.info("Customizable Player Models Initialized");
	}

	@EventHandler
	public void processIMC(IMCEvent event) {
		event.getMessages().forEach(m -> {
			try {
				if(m.key.equals("api")) {
					ICPMPlugin plugin = (ICPMPlugin) Class.forName(m.getStringValue()).newInstance();
					api.register(plugin);
				}
			} catch (Throwable e) {
				LOG.error("Mod {} provides a broken implementation of CPM api", m.getSender(), e);
			}
		});
		LOG.info("Customizable Player Models IMC processed: " + api.getPluginStatus());
		api.buildCommon().init();
		proxy.apiInit();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent evt) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(evt.getServer()));
		new Command(evt::registerServerCommand);
	}

	@EventHandler
	public void serverStop(FMLServerStoppingEvent evt) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}

	private ModConfigFile cfg;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		cfg = new ModConfigFile(new File(evt.getModConfigurationDirectory(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public ILogger getLogger() {
		return log;
	}

	private static final EnumSet<PlatformFeature> features = EnumSet.noneOf(PlatformFeature.class);

	@Override
	public EnumSet<PlatformFeature> getSupportedFeatures() {
		return features;
	}

	@Override
	public String getPlatformVersionString() {
		return "Minecraft 1.7.10 (" + FMLCommonHandler.instance().getModName() + ") " + Loader.instance().getIndexedModList().get(CustomPlayerModels.ID).getDisplayVersion();
	}

	@Override
	public TextRemapper<IChatComponent> getTextRemapper() {
		return new TextRemapper<>(ChatComponentTranslation::new, ChatComponentText::new, IChatComponent::appendSibling, null);
	}

	@Override
	public CPMApiManager getApi() {
		return api;
	}
}
