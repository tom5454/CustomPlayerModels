package com.tom.cpm.shared.editor.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.MarkdownParser;
import com.tom.cpl.util.MarkdownRenderer;
import com.tom.cpm.externals.org.apache.maven.artifact.versioning.ComparableVersion;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.util.MdResourceLoader;
import com.tom.cpm.shared.util.VersionCheck;

public class ChangelogPanel extends Panel {
	private MarkdownRenderer mdr;

	public ChangelogPanel(IGui gui, String version) {
		super(gui);
		String ch = "# " + gui.i18nFormat("label.cpm.changelog.title") + "\n\n" + gui.i18nFormat("label.cpm.loading");
		mdr = new MarkdownRenderer(gui, new MdResourceLoader(gui::openURL, null, true), ch);
		addElement(mdr);

		VersionCheck vc = VersionCheck.get();
		vc.getFinished().thenRunAsync(() -> {
			StringBuilder sb = new StringBuilder("# ");
			Map<ComparableVersion, String> allChanges;
			if(version != null) {
				ComparableVersion ver = new ComparableVersion(version);
				allChanges = new LinkedHashMap<>();
				for (Entry<ComparableVersion, String> e : vc.getAllChanges().entrySet()) {
					if(e.getKey().compareTo(ver) > 0)
						allChanges.put(e.getKey(), e.getValue());
				}
				sb.append(gui.i18nFormat("label.cpm.changelog.changesSince", version));
				sb.append("\n\n");
			} else {
				allChanges = vc.getAllChanges();
				sb.append(gui.i18nFormat("label.cpm.changelog.title"));
				sb.append("\n\n");
			}
			ComparableVersion c = new ComparableVersion(MinecraftCommonAccess.get().getModVersion());
			allChanges.entrySet().stream().sorted(Collections.reverseOrder(Comparator.comparing(Entry::getKey))).forEach(e -> {
				sb.append("## ");
				if(c.equals(e.getKey()))
					sb.append(gui.i18nFormat("label.cpm.changelog.currentVersion", e.getKey().toString()));
				else
					sb.append(e.getKey().toString().replace("_", "\\_"));
				sb.append('\n');
				String chlnk = e.getValue().replaceAll("#(\\d+)", "[#$1](https://github.com/tom5454/CustomPlayerModels/issues/$1)");
				String chmd = chlnk.replaceAll("- (.+)\n([^-])", "- $1\n\n$2").replace("\n", "  \n");
				sb.append(chmd);
				sb.append("\n\n");
			});

			mdr.setContent(new MarkdownParser(sb.toString()));
		}, gui::executeLater).handleAsync((__, e) -> {
			if(e != null)mdr.setContent(MarkdownParser.makeErrorPage(gui, e));
			return null;
		}, gui::executeLater);
	}

	@Override
	public GuiElement setBounds(Box bounds) {
		mdr.setBounds(new Box(0, 0, bounds.w, bounds.h));
		return super.setBounds(bounds);
	}
}
