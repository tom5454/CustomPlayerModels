package com.tom.cpm.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class GiveSkullCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "cpm_give_skull";
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return "/cpm_give_skull <to> <name> [amount]";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		if (var2.length >= 2) {
			EntityPlayerMP var3 = getPlayer(var1, var2[0]);

			int var5 = 1;
			if (var2.length >= 3) {
				var5 = parseIntBounded(var1, var2[2], 1, 64);
			}

			ItemStack is = new ItemStack(Item.skull, var5, 3);
			is.setTagCompound(new NBTTagCompound());
			is.getTagCompound().setString("SkullOwner", var2[1]);

			EntityItem var8 = var3.dropPlayerItem(is);
			var8.delayBeforeCanPickup = 0;
			notifyAdmins(var1, "commands.give.success", new Object[]{
					Item.skull.getItemStackDisplayName(is), Item.skull.itemID, var5, var3.getEntityName()});
		} else {
			throw new WrongUsageException("/cpm_give_skull <to> <name>", new Object[0]);
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}
}
