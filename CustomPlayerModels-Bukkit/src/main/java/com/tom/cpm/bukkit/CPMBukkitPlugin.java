package com.tom.cpm.bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.tom.cpl.config.ConfigEntry.ModConfigFile;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;

public class CPMBukkitPlugin extends JavaPlugin {
	public ModConfigFile config;
	private Network net;
	public I18n i18n;
	private BukkitLogger log;

	@Override
	public void onDisable() {
		super.onDisable();
		MinecraftObjectHolder.setCommonObject(null);
		config.save();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		getDataFolder().mkdirs();
		log = new BukkitLogger(getLogger());
		config = new ModConfigFile(new File(getDataFolder(), "cpm.json"));
		File tr = new File(getDataFolder(), "cpm.lang");
		if(tr.exists()) {
			try {
				i18n = I18n.loadLocaleData(new FileInputStream(tr));
			} catch (IOException e) {
				log.warn("Failed to load localization from cpm.lang", e);
			}
		}
		if(i18n == null) {
			try {
				i18n = I18n.loadLocaleData(CPMBukkitPlugin.class.getResourceAsStream("/assets/cpm/lang/en_us.lang"));
			} catch (IOException e) {
				log.error("Failed to load localization from builtin lang file", e);
				i18n = new I18n() {
					@Override
					public String format(String translateKey, Object... parameters) {
						return "Server failed to load builtin localization. This is a BUG, please report it to the server owner.";
					}
				};
			}
		}
		getCommand("cpm").setExecutor(this);
		MinecraftObjectHolder.setCommonObject(new MinecraftCommonAccess() {

			@Override
			public ModConfigFile getConfig() {
				return config;
			}

			@Override
			public ILogger getLogger() {
				return log;
			}
		});
		net = new Network(this);
		net.register();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new PlayerTracker(net), 0, 20);
		log.info("Customizable Player Models Initialized");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(ChatColor.RED + i18n.format("commands.cpm.usage"));
			return false;
		}
		switch (args[0]) {
		case "setskin":
		{
			boolean force = false;
			boolean save = true;
			boolean reset = false;
			if(args.length < 2) {
				sender.sendMessage(ChatColor.RED + i18n.format("commands.cpm.usage"));
				return false;
			}
			int i = 2;
			switch (args[1]) {
			case "-t":
				save = false;
				break;

			case "-r":
				reset = true;
				break;

			case "-f":
				force = true;
				break;

			default:
				i = 1;
				break;
			}
			if(args.length < i+1) {
				sender.sendMessage(ChatColor.RED + i18n.format("commands.cpm.usage"));
				return false;
			}
			Player player = Bukkit.getPlayer(args[i]);
			if(player == null) {
				sender.sendMessage(ChatColor.RED + i18n.format("commands.cpm.usage"));
				return false;
			}
			if(reset) {
				execute(player, sender, null, force, save);
			} else {
				if(args.length < i+2) {
					sender.sendMessage(ChatColor.RED + i18n.format("commands.cpm.usage"));
					return false;
				}
				String b64 = args[i+1];
				execute(player, sender, b64, force, save);
			}
			return true;
		}

		default:
		{
			sender.sendMessage(ChatColor.RED + i18n.format("commands.cpm.usage"));
			return false;
		}
		}
	}

	private void execute(Player player, CommandSender sender, String skin, boolean force, boolean save) {
		net.onCommand(player, skin, force, save);
		if(force)sender.sendMessage(i18n.format("commands.cpm.setskin.success.force", player.getDisplayName()));
		else sender.sendMessage(i18n.format("commands.cpm.setskin.success", player.getDisplayName()));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, new String[] {"setskin"});
		} else {
			switch (args[0]) {
			case "setskin":
			{
				List<String> result = new ArrayList<>();
				if(args.length == 2) {
					result.addAll(getListOfStringsMatchingLastWord(args, new String[] {"-f", "-r", "-t"}));
					result.addAll(getListOfStringsMatchingLastWord(args, Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())));
				} else if(args.length == 3 && args[2].startsWith("-")) {
					result.addAll(getListOfStringsMatchingLastWord(args, Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())));
				}
				return result;
			}

			default:
				break;
			}
		}
		return Collections.emptyList();
	}

	public static List<String> getListOfStringsMatchingLastWord(String[] args, String... possibilities) {
		return getListOfStringsMatchingLastWord(args, Arrays.asList(possibilities));
	}

	public static boolean doesStringStartWith(String original, String region) {
		return region.regionMatches(true, 0, original, 0, original.length());
	}

	public static List<String> getListOfStringsMatchingLastWord(String[] inputArgs, Collection<?> possibleCompletions) {
		String s = inputArgs[inputArgs.length - 1];
		List<String> list = Lists.<String>newArrayList();

		if (!possibleCompletions.isEmpty()) {
			for (String s1 : Iterables.transform(possibleCompletions, Functions.toStringFunction())) {
				if (doesStringStartWith(s, s1)) {
					list.add(s1);
				}
			}
		}

		return list;
	}
}
