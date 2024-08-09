package com.tom.cpm.shared;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tom.cpl.command.ArgType;
import com.tom.cpl.command.CommandCtx;
import com.tom.cpl.command.CommandHandler;
import com.tom.cpl.command.LiteralCommandBuilder;
import com.tom.cpl.command.RequiredCommandBuilder;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.IGui;
import com.tom.cpl.text.FormatText;
import com.tom.cpl.util.Pair;
import com.tom.cpm.shared.MinecraftClientAccess.ServerStatus;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.TestIngameManager;
import com.tom.cpm.shared.gui.SettingsGui;
import com.tom.cpm.shared.gui.SocialGui;

public class CommandCPMClient {

	public static void register(CommandHandler<?> dispatcher) {
		LiteralCommandBuilder cpm = new LiteralCommandBuilder("cpmclient").
				then(new LiteralCommandBuilder("profile").
						then(new RequiredCommandBuilder("player", ArgType.STRING, false).
								setPossibleValues(() -> getPlayers()).
								run(c -> openProfile(c.getArgument("player")))
								)
						).
				then(new LiteralCommandBuilder("safety").run(c -> openSafety())).
				then(new LiteralCommandBuilder("animate").
						then(new RequiredCommandBuilder("animation", ArgType.STRING, false).
								setPossibleValues(c -> getAnimationList(c)).
								run(c -> setAnimation(c, -1)).
								then(new RequiredCommandBuilder("value", ArgType.INT, Pair.of(0, 255)).
										run(c -> setAnimation(c, c.getArgument("value")))
										)
								)
						).
				then(new LiteralCommandBuilder("set_model").
						then(new RequiredCommandBuilder("model", ArgType.STRING, false).
								setPossibleValues(c -> getModelsList(c)).
								run(c -> setModel(c))
								)
						).
				then(new LiteralCommandBuilder("reset_model").run(c -> resetModel())
						);
		dispatcher.register(cpm, false);
	}

	private static void resetModel() {
		ModConfig.getCommonConfig().clearValue(ConfigKeys.SELECTED_MODEL);
		ModConfig.getCommonConfig().save();
		if(MinecraftClientAccess.get().getServerSideStatus() == ServerStatus.INSTALLED) {
			MinecraftClientAccess.get().sendSkinUpdate();
		}
	}

	private static void setModel(CommandCtx<?> context) {
		if(MinecraftClientAccess.get().getServerSideStatus() != ServerStatus.INSTALLED) {
			context.fail(new FormatText("label.cpm.feature_unavailable"));
			return;
		}
		String modelF = context.getArgument("model");
		if (!getModelsList(context).contains(modelF)) {
			context.fail(new FormatText("commands.cpmclient.set_model.not_found", modelF));
			return;
		}
		ModConfig.getCommonConfig().setString(ConfigKeys.SELECTED_MODEL, modelF);
		ModConfig.getCommonConfig().save();
		MinecraftClientAccess.get().sendSkinUpdate();
	}

	private static List<String> getModelsList(CommandCtx<?> c) {
		List<String> l = new ArrayList<>();
		File modelsDir = new File(MinecraftClientAccess.get().getGameDir(), "player_models");
		if (modelsDir.exists()) {
			walkDirs(modelsDir, null, l);
		}
		return l;
	}

	private static void walkDirs(File d, String path, List<String> l) {
		File[] fs = d.listFiles((f, n) -> n.endsWith(".cpmmodel") || new File(f, n).isDirectory());
		for (int i = 0; i < fs.length; i++) {
			File f = fs[i];
			if(f.getName().equals(TestIngameManager.TEST_MODEL_NAME) || f.getName().equals("autosaves"))continue;
			String p = path != null ? path + "/" + f.getName() : f.getName();
			if(f.isDirectory()) {
				walkDirs(f, p, l);
			} else {
				l.add(p);
			}
		}
	}

	private static void openProfile(String player) {
		ModelDefinitionLoader<Object> d = MinecraftClientAccess.get().getDefinitionLoader();
		for (Object gp : MinecraftClientAccess.get().getPlayers()) {
			if (player.equals(d.getGP_Name(gp))) {
				UUID uuid = d.getGP_UUID(gp);
				openGui(g -> new SocialGui(g, uuid));
				return;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static List<String> getPlayers() {
		ModelDefinitionLoader<Object> d = MinecraftClientAccess.get().getDefinitionLoader();
		return MinecraftClientAccess.get().getPlayers().stream().map(d::getGP_Name).filter(e -> e != null).collect(Collectors.toList());
	}

	private static void openSafety() {
		openGui(SettingsGui::safetySettings);
	}

	private static void openGui(Function<IGui, Frame> creator) {
		MinecraftClientAccess.get().executeNextFrame(() -> MinecraftClientAccess.get().openGui(creator));
	}

	private static List<String> getAnimationList(CommandCtx<?> context) {
		List<String> l = new ArrayList<>();
		Player<?> player = MinecraftClientAccess.get().getCurrentClientPlayer();
		ModelDefinition def = player.getModelDefinition();
		if(def != null) {
			AnimationRegistry ar = def.getAnimations();
			return new ArrayList<>(ar.getCommandActionsMap().keySet());
		}
		return l;
	}

	private static void setAnimation(CommandCtx<?> context, int value) {
		String id = context.getArgument("animation");
		if (!playAnimation(id, value)) {
			context.fail(new FormatText("commands.cpmclient.animate.invalid", id));
		}
	}

	private static boolean playAnimation(String id, int value) {
		AnimationEngine an = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
		return an.applyCommand(id, value);
	}
}
