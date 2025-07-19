package com.tom.cpm.blockbench;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpm.blockbench.convert.ProjectConvert;
import com.tom.cpm.blockbench.proxy.Global;
import com.tom.cpm.web.client.WebMC;

import elemental2.dom.DomGlobal;

public class BBInstance extends WebMC {

	public BBInstance() {
		super(new ModConfigFile(DomGlobal.window, true, "cpm_plugin_config"), true, true);
	}

	@Override
	public String getMCVersion() {
		return "blockbench";
	}

	@Override
	public void populatePlatformSettings(String group, Panel panel) {
		super.populatePlatformSettings(group, panel);
		switch (group) {
		case "filePopup": {
			PopupMenu pp = (PopupMenu) panel;
			pp.addButton(panel.getGui().i18nFormat("bb-button.viewInBB"), ProjectConvert::viewInBB);
		}
		break;
		}
	}

	@Override
	public void openURL(String url) {
		Global.openExternal(url);
	}

	@Override
	public String getAppID() {
		return "Blockbench CPM Plugin";
	}

	@Override
	protected String getLangChangeDesc() {
		return "bb-label.language.reloadRequired.desc";
	}
}
