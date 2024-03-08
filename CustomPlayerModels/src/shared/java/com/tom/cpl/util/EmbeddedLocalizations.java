package com.tom.cpl.util;

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
		//Do not edit below this line
		String l;
		//==Auto generator marker==
		loadProject.setFallback("Load Project");
		saveProject.setFallback("Save Project");
		importFile.setFallback("Import");
		exportSkin.setFallback("Export Skin");
		saveSkin.setFallback("Save Skin");
		loadSkin.setFallback("Load Custom Skin");
		openSkin.setFallback("Open Skin");
		saveLogs.setFallback("Export Logs");
		fileProject.setFallback("Project file (.cpmproject)");
		filePng.setFallback("Image (.png)");
		fileLog.setFallback("Exported logs (.zip)");
		exportUV.setFallback("Export UV Map");
		fileOra.setFallback("Open Raster file (.ora)");
		l = "es";
		loadProject.addLocale(l, "Cargar Proyecto");
		saveProject.addLocale(l, "Guardar Proyecto");
		importFile.addLocale(l, "Importar");
		exportSkin.addLocale(l, "Exportar Skin");
		saveSkin.addLocale(l, "Guardar Skin");
		loadSkin.addLocale(l, "Cargar Skin Personalizada");
		openSkin.addLocale(l, "Abrir Skin");
		saveLogs.addLocale(l, "Exportar Registro");
		fileProject.addLocale(l, "Archivo de Proyecto (.cpmproject)");
		filePng.addLocale(l, "Imagen (.png)");
		fileLog.addLocale(l, "Registros exportados (.zip)");
		l = "zn_cn";
		loadProject.addLocale(l, "\u52A0\u8F7D\u9879\u76EE");
		saveProject.addLocale(l, "\u4FDD\u5B58\u9879\u76EE");
		importFile.addLocale(l, "\u5BFC\u5165");
		exportSkin.addLocale(l, "\u5BFC\u51FA\u76AE\u80A4");
		saveSkin.addLocale(l, "\u4FDD\u5B58\u76AE\u80A4");
		loadSkin.addLocale(l, "\u52A0\u8F7D\u81EA\u5B9A\u4E49\u76AE\u80A4");
		openSkin.addLocale(l, "\u6253\u5F00\u76AE\u80A4");
		saveLogs.addLocale(l, "\u5BFC\u51FA\u65E5\u5FD7");
		fileProject.addLocale(l, "\u9879\u76EE\u6587\u4EF6\uFF08.cpmproject\uFF09");
		filePng.addLocale(l, "\u56FE\u7247\uFF08.png\uFF09");
		fileLog.addLocale(l, "\u5BFC\u51FA\u7684\u65E5\u5FD7\uFF08.zip\uFF09");
		//==Auto generator end==
		EmbeddedLocalization.validateEmbeds();
	}
}
