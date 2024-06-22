package com.tom.cpm.bukkit;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.tom.cpl.text.TextRemapper;
import com.tom.cpm.bukkit.text.BukkitText;

public interface CommandHandler {
	boolean onCommand(CommandSender sender, Command command, String label, String[] args);
	List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
	TextRemapper<? extends BukkitText> remapper();
}
