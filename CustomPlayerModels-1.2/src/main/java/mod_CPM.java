import net.minecraft.src.EntityPlayer;

import com.tom.cpl.util.ILogger;
import com.tom.cpm.CPMVersion;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.retro.JavaLogger;
import com.tom.cpmcore.CPMLoadingPlugin;

import cpw.mods.fml.common.Loader;

public class mod_CPM extends BaseMod {
	static {
		if (!CPMLoadingPlugin.isLoaded) {
			ILogger log = new JavaLogger(Loader.log, "CPM");
			log.error("###########################################");
			log.error("CPM is a Java Agent!");
			log.error("Please place the CustomPlayerModels-1.2.5-" + CPMVersion.getVersion() + ".jar");
			log.error("into the base folder in your Minecraft installation.");
			log.error("Then add '-javaagent:CustomPlayerModels-1.2.5-" + CPMVersion.getVersion() + ".jar' to your JVM arguments.");
			log.error("See the wiki for more info: https://github.com/tom5454/CustomPlayerModels/wiki/Agent-Install");
			log.error("###########################################");
			throw new RuntimeException("CPM is a Java Agent! Check log for instructions.");
		}
	}

	private CustomPlayerModels mod = new CustomPlayerModels();

	@Override
	public String getVersion() {
		return CPMVersion.getVersion();
	}

	@Override
	public void load() {
		mod.init();
	}

	@Override
	public void onClientLogin(EntityPlayer player) {
		ServerHandler.netHandler.onJoin(player);
	}

	@Override
	public boolean onServerCommand(String command, String sender, Object listener) {
		return CustomPlayerModels.proxy.runCommand(command, sender, listener);
	}
}
