package com.tom.cpmoscc.mod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.relauncher.Side;

import com.tom.cpmoscc.CPMOSC;

@Mod(modid = CPMOSC.MOD_ID, acceptableRemoteVersions = "*", updateJSON = CPMOSC.VERSION_CHECK_URL, clientSideOnly = true, dependencies = "required-after:customplayermodels")
public class CPMOSCMod {

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		FMLInterModComms.sendMessage("customplayermodels", "api", "com.tom.cpmoscc.CPMOSCPlugin");

		if (evt.getSide() == Side.CLIENT) {
			CPMOSCClient.INSTANCE.init();
		}
	}
}