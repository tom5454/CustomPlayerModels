package com.tom.cpm.client;

import java.util.List;
import java.util.StringTokenizer;

import net.minecraft.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.tom.cpm.common.Command;

//Port of:
//https://gist.github.com/mojontwins/de1fb522bb3ed724987f12768a3cd2cb
public class SinglePlayerCommands {

	public static final Command.CommandHandlerBase<Void> cpm = new Command.CommandHandlerBase<>() {
		{
			registerClient();
		}

		@Override
		protected void sendMessage(Void sender, String string) {
			Minecraft.INSTANCE.inGameHud.addChatMessage(string);
		}
	};

	public static void executeCommand(Minecraft mc, String command) {
		StringTokenizer tokenizer = new StringTokenizer(command);

		int numTokens = tokenizer.countTokens();
		if (numTokens == 0) return;

		String[] tokens = new String [numTokens];
		int idx = 0;
		while (tokenizer.hasMoreTokens()) {
			tokens [idx++] = tokenizer.nextToken();
		}

		if (idx > 0) {
			String cmd = tokens [0];
			if ("/time".equals(cmd)) {
				if (idx > 2 && "set".equals(tokens [1])) {
					int timeSet = -1;
					if ("night".equals(tokens [2])) {
						timeSet = 14000;
					} else if ("day".equals(tokens [2])) {
						timeSet = 1000;
					} else {
						try {
							timeSet = Integer.parseInt(tokens [2]);
						} catch (Exception e) { }
					}
					long timeBaseDay = mc.world.getTime() / 24000L * 24000L;
					long elapsedDay = mc.world.getTime() % 24000L;
					if (timeSet > elapsedDay) timeBaseDay += 24000L;
					mc.world.method_262().setTime(timeBaseDay + timeSet);
					mc.inGameHud.addChatMessage("Time set to " + timeSet);
				}
			} else if ("/tp".equals(cmd)) {
				if (idx > 3) {
					double x = mc.player.x;
					double y = mc.player.y;
					double z = mc.player.z;

					try {
						x = Double.parseDouble(tokens [1]);
					} catch (Exception e) { }

					try {
						y = Double.parseDouble(tokens [2]);
					} catch (Exception e) { }

					try {
						z = Double.parseDouble(tokens [3]);
					} catch (Exception e) { }

					mc.player.method_1341(x, y, z, mc.player.yaw, mc.player.pitch);
					mc.inGameHud.addChatMessage("Teleporting to " + x + " " + y + " " + z);
				}
			} else if ("/give".equals(cmd)) {
				try {
					int var21 = Integer.parseInt(tokens[1]);
					if (Item.ITEMS[var21] != null) {
						mc.inGameHud.addChatMessage("Giving some " + var21);
						int amount = 1;
						int meta = 0;
						if (tokens.length > 2) {
							amount = Integer.parseInt(tokens[2]);
						}

						if (tokens.length > 3) {
							meta = Integer.parseInt(tokens[3]);
						}

						if (amount < 1) {
							amount = 1;
						}

						if (amount > 64) {
							amount = 64;
						}

						mc.player.method_513(new ItemStack(var21, amount, meta));
					} else {
						mc.inGameHud.addChatMessage("There's no item with id " + var21);
					}
				} catch (NumberFormatException var22) {
					mc.inGameHud.addChatMessage("There's no item with id " + tokens[1]);
				}
			} else if ("/toggledownfall".equals(cmd)) {
				mc.world.method_289(mc.world.method_270() ? 0 : 1);
				mc.inGameHud.addChatMessage("Toggling rain and snow, hold on...");
			} else {
				cpm.onCommand(null, command.substring(1));
			}
		}
	}

	public static List<String> tabComplete(String text) {
		return cpm.onTabComplete(text.substring(1));
	}
}
