package com.tom.cpm.bukkit.text;

import org.bukkit.command.CommandSender;

import com.tom.cpl.text.TextRemapper;
import com.tom.cpm.bukkit.CPMBukkitPlugin;

public interface BukkitText {
	void sendTo(CommandSender pl);

	public static class Simple implements BukkitText {
		private String value;

		public Simple(String value) {
			this.value = value;
		}

		public Simple(Simple a, Simple b) {
			this.value = a.value + b.value;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public void sendTo(CommandSender pl) {
			pl.sendMessage(value);
		}
	}

	public static TextRemapper<BukkitText.Simple> simple(CPMBukkitPlugin plugin) {
		return new TextRemapper<>((a, b) -> new Simple(plugin.stringMapper.translate(a, b)), Simple::new, Simple::new, null, (a, b) -> new Simple(plugin.stringMapper.styling(a.toString(), b)));
	}
}
