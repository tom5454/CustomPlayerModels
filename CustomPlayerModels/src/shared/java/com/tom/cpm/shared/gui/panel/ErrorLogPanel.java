package com.tom.cpm.shared.gui.panel;

import static com.tom.cpm.shared.MinecraftObjectHolder.gson;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.FileChooserPopup;
import com.tom.cpl.gui.elements.FileChooserPopup.FileFilter;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.editor.project.ProjectFile;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogEntry;

public class ErrorLogPanel extends Panel {
	private final Frame frm;
	private Button saveLogs;
	private Panel panel;
	private List<LogEntry> entries;

	public ErrorLogPanel(Frame frm, int w, int h) {
		super(frm.getGui());
		this.frm = frm;
		setBounds(new Box(0, 0, w, h));

		ScrollPanel scp = new ScrollPanel(gui);
		scp.setBounds(new Box(5, 5, w - 10, h - 35));
		addElement(scp);

		panel = new Panel(gui);
		panel.setBackgroundColor(0xff777777);
		scp.setDisplay(panel);

		saveLogs = new Button(gui, gui.i18nFormat("button.cpm.saveLogs"), () -> {
			FileChooserPopup fc = new FileChooserPopup(frm);
			fc.setTitle(gui.i18nFormat("button.cpm.saveLogs"));
			fc.setFileDescText(gui.i18nFormat("label.cpm.file_logs"));
			fc.setFilter(new FileFilter("zip"));
			fc.setExtAdder(n -> n + ".zip");
			fc.setAccept(this::saveLogs);
			fc.setSaveDialog(true);
			fc.setButtonText(gui.i18nFormat("button.cpm.ok"));
			fc.setFileName("cpm-errorlog-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".zip");
			frm.openPopup(fc);
		});
		saveLogs.setBounds(new Box(5, h - 25, 100, 20));
		addElement(saveLogs);

		Button clear = new Button(gui, gui.i18nFormat("button.cpm.clearLogs"), () -> {
			ErrorLog.clear();
			loadLogs();
		});
		clear.setBounds(new Box(110, h - 25, 100, 20));
		addElement(clear);

		loadLogs();

		String v = gui.i18nFormat("label.cpm.runtimeVersion", PlatformFeature.getVersion());
		addElement(new Label(gui, v).setBounds(new Box(w - gui.textWidth(v) - 3, h - 11, 0, 0)));
	}

	private void loadLogs() {
		panel.getElements().clear();
		entries = ErrorLog.collectErrors();
		entries.sort(Comparator.comparing(LogEntry::getLevel));

		panel.setBounds(new Box(0, 0, bounds.w - 10, entries.size() * 16));

		for (int i = 0; i < entries.size(); i++) {
			LogEntry e = entries.get(i);
			panel.addElement(new EntryPanel(e, i * 16, bounds.w - 10));
		}

		saveLogs.setEnabled(!entries.isEmpty());
	}

	private void saveLogs(File f) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", MinecraftClientAccess.get().getClientPlayer().getName());
		List<Object> a = new ArrayList<>();
		map.put("entries", a);
		map.put("platform", PlatformFeature.getVersion());
		entries.forEach(e -> a.add(e.toMap()));
		ProjectFile pf = new ProjectFile();
		try {
			try(OutputStreamWriter os = new OutputStreamWriter(pf.setAsStream("logs.json"))) {
				gson.toJson(map, os);
			}
			pf.save(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private class EntryPanel extends Panel {
		private Tooltip tooltip;
		private Tooltip tooltipST;

		public EntryPanel(LogEntry entry, int y, int w) {
			super(ErrorLogPanel.this.getGui());

			addElement(new Label(gui, gui.i18nFormat("label.cpm.level." + entry.getLevel().name().toLowerCase())).setBounds(new Box(5, 4, 20, 10)));

			addElement(new Label(gui, entry.getMessage().toString(gui)).setBounds(new Box(45, 4, w - 35, 10)));

			tooltip = new Tooltip(frm, gui.wordWrap(entry.toTooltipString(gui, false), frm.getBounds().w));
			tooltipST = new Tooltip(frm, gui.wordWrap(entry.toTooltipString(gui, true), frm.getBounds().w));

			switch (entry.getLevel()) {
			case ERROR:
				setBackgroundColor(0xffff2222);
				break;
			case INFO:
				setBackgroundColor(0xff888888);
				break;
			case WARNING:
				setBackgroundColor(0xffaaaa22);
				break;
			default:
				break;
			}

			setBounds(new Box(0, y, w, 15));
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			super.draw(event, partialTicks);
			if(event.isHovered(bounds)) {
				if(gui.isShiftDown())tooltipST.set();
				else tooltip.set();
			}
		}
	}
}
