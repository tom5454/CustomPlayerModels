package com.tom.cpl.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.ScrollPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.MarkdownParser.Cursor;

public class MarkdownRenderer extends Panel {
	private Panel panel;
	private ScrollPanel scp;
	private Map<String, Integer> headerPositions = new HashMap<>();
	private MarkdownParser content;
	private MarkdownResourceLoader loader;
	private Set<Runnable> cleanup = new HashSet<>();
	protected Map<String, CustomMdElementFactory> customElementFactories = new HashMap<>();

	public MarkdownRenderer(IGui gui, MarkdownResourceLoader loader, String in) {
		this(gui, loader, new MarkdownParser(in));
	}

	public MarkdownRenderer(IGui gui, MarkdownResourceLoader loader, MarkdownParser parsed) {
		super(gui);
		this.content = parsed;
		this.panel = new Panel(gui);
		this.loader = loader;
		setBackgroundColor(gui.getColors().panel_background);

		scp = new ScrollPanel(gui);
		addElement(scp);
		scp.setDisplay(panel);
	}

	public void refresh() {
		cleanup.forEach(Runnable::run);
		cleanup.clear();
		panel.getElements().clear();
		int h = content.toElements(this, bounds.w - 15, panel.getElements()::addAll, headerPositions);
		panel.setBounds(new Box(0, 0, bounds.w - 10, h));
	}

	public void setContent(MarkdownParser content) {
		scp.setScrollY(0);
		this.content = content;
		if(bounds != null)
			refresh();
	}

	public Panel getPanel() {
		return panel;
	}

	@Override
	public GuiElement setBounds(Box bounds) {
		scp.setBounds(new Box(5, 0, bounds.w - 5, bounds.h));
		super.setBounds(bounds);
		refresh();
		return this;
	}

	public MarkdownResourceLoader getLoader() {
		return loader;
	}

	public void browse(String url) {
		if(url.startsWith("#")) {
			String h = url.substring(1);
			if(headerPositions.containsKey(h)) {
				int l = headerPositions.get(h);
				scp.setScrollY(l);
			}
		} else {
			loader.browse(this, url);
		}
	}

	public interface MarkdownResourceLoader {
		CompletableFuture<Image> loadImage(String url);
		void browse(MarkdownRenderer rd, String url);
	}

	public void cleanup() {
		cleanup.forEach(Runnable::run);
		cleanup.clear();
	}

	public void registerCleanup(Runnable r) {
		cleanup.add(r);
	}

	public void registerCustomElement(String id, CustomMdElementFactory func) {
		customElementFactories.put(id, func);
	}

	public static interface CustomMdElementFactory {
		List<GuiElement> create(MarkdownRenderer r, Cursor cursor, String args);
	}

	public void setLoader(MarkdownResourceLoader loader) {
		this.loader = loader;
	}
}
