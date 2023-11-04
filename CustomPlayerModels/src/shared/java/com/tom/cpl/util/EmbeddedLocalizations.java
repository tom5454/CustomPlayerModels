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

	public static void load() {}

	static {
		//Do not edit below this line
		String l;
		//==Auto generator marker==
		loadProject.setFallback("TG9hZCBQcm9qZWN0");
		saveProject.setFallback("U2F2ZSBQcm9qZWN0");
		importFile.setFallback("SW1wb3J0");
		exportSkin.setFallback("RXhwb3J0IFNraW4=");
		saveSkin.setFallback("U2F2ZSBTa2lu");
		loadSkin.setFallback("TG9hZCBDdXN0b20gU2tpbg==");
		openSkin.setFallback("T3BlbiBTa2lu");
		saveLogs.setFallback("RXhwb3J0IExvZ3M=");
		fileProject.setFallback("UHJvamVjdCBmaWxlICguY3BtcHJvamVjdCk=");
		filePng.setFallback("SW1hZ2UgKC5wbmcp");
		fileLog.setFallback("RXhwb3J0ZWQgbG9ncyAoLnppcCk=");
		l = "es";
		loadProject.addLocale(l, "Q2FyZ2FyIFByb3llY3Rv");
		saveProject.addLocale(l, "R3VhcmRhciBQcm95ZWN0bw==");
		exportSkin.addLocale(l, "RXhwb3J0YXIgU2tpbg==");
		saveSkin.addLocale(l, "R3VhcmRhciBTa2lu");
		loadSkin.addLocale(l, "Q2FyZ2FyIFNraW4gUGVyc29uYWxpemFkYQ==");
		openSkin.addLocale(l, "QWJyaXIgU2tpbg==");
		saveLogs.addLocale(l, "RXhwb3J0YXIgUmVnaXN0cm8=");
		fileProject.addLocale(l, "QXJjaGl2byBkZSBQcm95ZWN0byAoLmNwbXByb2plY3Qp");
		filePng.addLocale(l, "SW1hZ2VuICgucG5nKQ==");
		fileLog.addLocale(l, "UmVnaXN0cm9zIGV4cG9ydGFkb3MgKC56aXAp");
		l = "zn_cn";
		loadProject.addLocale(l, "5Yqg6L296aG555uu");
		saveProject.addLocale(l, "5L+d5a2Y6aG555uu");
		exportSkin.addLocale(l, "5a+85Ye655qu6IKk");
		saveSkin.addLocale(l, "5L+d5a2Y55qu6IKk");
		loadSkin.addLocale(l, "5Yqg6L296Ieq5a6a5LmJ55qu6IKk");
		openSkin.addLocale(l, "5omT5byA55qu6IKk");
		saveLogs.addLocale(l, "5a+85Ye65pel5b+X");
		fileProject.addLocale(l, "6aG555uu5paH5Lu277yILmNwbXByb2plY3TvvIk=");
		filePng.addLocale(l, "5Zu+54mH77yILnBuZ++8iQ==");
		fileLog.addLocale(l, "5a+85Ye655qE5pel5b+X77yILnppcO+8iQ==");
		//==Auto generator end==
		EmbeddedLocalization.validateEmbeds();
	}
}
