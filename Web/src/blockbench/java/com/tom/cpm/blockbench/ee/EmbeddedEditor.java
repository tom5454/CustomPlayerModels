package com.tom.cpm.blockbench.ee;

import elemental2.core.ArrayBuffer;
import elemental2.promise.Promise;

public interface EmbeddedEditor {
	public static final String PAGE_HTML = "<html><head><title>CPM Embedded Editor (Beta)</title></head><body style=\"background: rgb(51, 51, 51);\"><h1 id=\"loadingBar\" style=\"font-size: 2em; font-weight: bold; color: white; margin: 40px 0px 70px; text-align: center;\">Loading...</h1></body></html>";

	Promise<EmbeddedEditor> open();
	void focus();
	void close();
	boolean isClosed();
	void onReady();
	Promise<Void> openProject(ArrayBuffer dt);
	Promise<Void> openInHost(ArrayBuffer a);
}
