package com.tom.cpl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EmbeddedLocalizations {
	public static final EmbeddedLocalization loadProject = new EmbeddedLocalization("label.cpm.loadFile");
	public static final EmbeddedLocalization saveProject = new EmbeddedLocalization("label.cpm.saveFile");
	public static final EmbeddedLocalization importFile = new EmbeddedLocalization("label.cpm.import");
	public static final EmbeddedLocalization exportSkin = new EmbeddedLocalization("label.cpm.exportSkin");
	public static final EmbeddedLocalization saveSkin = new EmbeddedLocalization("label.cpm.saveSkin");
	public static final EmbeddedLocalization loadSkin = new EmbeddedLocalization("label.cpm.loadSkin");
	public static final EmbeddedLocalization openSkin = new EmbeddedLocalization("button.cpm.openSkin");
	public static final EmbeddedLocalization saveLogs = new EmbeddedLocalization("button.cpm.saveLogs");
	public static final EmbeddedLocalization fileProject = new EmbeddedLocalization("label.cpm.file_project");
	public static final EmbeddedLocalization filePng = new EmbeddedLocalization("label.cpm.file_png");
	public static final EmbeddedLocalization fileLog = new EmbeddedLocalization("label.cpm.file_logs");
	public static final EmbeddedLocalization exportUV = new EmbeddedLocalization("label.cpm.exportUVs");
	public static final EmbeddedLocalization fileOra = new EmbeddedLocalization("label.cpm.file_openraster");

	public static void load() {}

	static {
		if (EmbeddedLocalization.validateEmbeds) {
			Map<String, EmbeddedLocalization> lookup = new HashMap<>();
			EmbeddedLocalization.forEachEntry(e -> lookup.put(e.getKey(), e));
			try (InputStream is = EmbeddedLocalizations.class.getResourceAsStream("/com/tom/cpl/util/embedded_lang.properties")){
				Properties pr = new Properties();
				pr.load(is);
				for (String key : pr.stringPropertyNames()) {
					String val = pr.getProperty(key);
					String[] sp = key.split("/");
					EmbeddedLocalization el = lookup.get(sp[0]);
					if (sp.length == 1) {
						el.setFallback(val);
					} else {
						el.addLocale(sp[1], val);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Corrupted mod JAR", e);
			}
			EmbeddedLocalization.validateEmbeds();
		}
	}
}
