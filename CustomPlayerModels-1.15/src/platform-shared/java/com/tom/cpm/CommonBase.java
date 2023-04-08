package com.tom.cpm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.text.TextStyle;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.shared.MinecraftCommonAccess;

public abstract class CommonBase implements MinecraftCommonAccess {
	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);

	public static CPMApiManager api;
	protected ModConfigFile cfg;

	public CommonBase() {
		api = new CPMApiManager();
	}

	protected void apiInit() {
		LOG.info("Customizable Player Models API initialized: " + api.getPluginStatus());
		api.buildCommon().player(PlayerEntity.class).init();
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public ILogger getLogger() {
		return log;
	}

	@Override
	public TextRemapper<ITextComponent> getTextRemapper() {
		return new TextRemapper<>(TranslationTextComponent::new, StringTextComponent::new, ITextComponent::append,
				KeybindTextComponent::new, CommonBase::styleText);
	}

	private static ITextComponent styleText(ITextComponent in, TextStyle style) {
		return in.withStyle(s -> s.setBold(style.bold).setItalic(style.italic).setUnderlined(style.underline).setStrikethrough(style.strikethrough));
	}

	@Override
	public CPMApiManager getApi() {
		return api;
	}

	@Override
	public String getMCVersion() {
		return SharedConstants.getCurrentVersion().getName();
	}
}
