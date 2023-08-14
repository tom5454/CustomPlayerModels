package com.tom.cpm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import com.tom.cpl.block.BlockStateHandler;
import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.item.ItemStackHandler;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.text.TextStyle;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.common.BlockStateHandlerImpl;
import com.tom.cpm.common.ItemStackHandlerImpl;
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
		api.buildCommon().player(Player.class).init();
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
	public TextRemapper<MutableComponent> getTextRemapper() {
		return new TextRemapper<>(Component::translatable, Component::literal, MutableComponent::append, Component::keybind,
				CommonBase::styleText);
	}

	private static MutableComponent styleText(MutableComponent in, TextStyle style) {
		return in.withStyle(Style.EMPTY.withBold(style.bold).withItalic(style.italic).withUnderlined(style.underline).withStrikethrough(style.strikethrough));
	}

	@Override
	public CPMApiManager getApi() {
		return api;
	}

	@Override
	public String getMCVersion() {
		return SharedConstants.getCurrentVersion().getName();
	}

	@Override
	public BlockStateHandler<?> getBlockStateHandler() {
		return BlockStateHandlerImpl.impl;
	}

	@Override
	public ItemStackHandler<?> getItemStackHandler() {
		return ItemStackHandlerImpl.impl;
	}
}
