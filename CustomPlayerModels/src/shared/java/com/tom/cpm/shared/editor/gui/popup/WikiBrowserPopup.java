package com.tom.cpm.shared.editor.gui.popup;

import java.util.Stack;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.util.MarkdownRenderer;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.util.MdResourceLoader;

public class WikiBrowserPopup extends PopupPanel {
	private Stack<String> backQueue = new Stack<>();
	private Stack<String> fwdQueue = new Stack<>();
	private boolean stepping = false;
	private Button back, fwd, openSidebar;
	private MarkdownRenderer content, sidebar;
	private static final int SIDEBAR_W = 100;

	public WikiBrowserPopup(IGui gui) {
		this(gui, "https://github.com/tom5454/CustomPlayerModels/wiki/Home.md");
	}

	public WikiBrowserPopup(IGui gui, String mainPage) {
		super(gui);

		back = new Button(gui, "<", () -> {
			if(backQueue.size() > 1) {
				stepping = true;
				String c = backQueue.pop();
				String p = backQueue.peek();
				fwdQueue.push(c);
				content.browse(p);
				stepping = false;
				back.setEnabled(backQueue.size() > 1);
				fwd.setEnabled(true);
			}
		});
		back.setBounds(new Box(5, 5, 20, 20));
		addElement(back);

		fwd = new Button(gui, ">", () -> {
			if(!fwdQueue.isEmpty()) {
				stepping = true;
				String n = fwdQueue.pop();
				backQueue.push(n);
				content.browse(n);
				stepping = false;
				back.setEnabled(true);
				fwd.setEnabled(!fwdQueue.isEmpty());
			}
		});
		fwd.setBounds(new Box(25, 5, 20, 20));
		addElement(fwd);

		fwd.setEnabled(false);
		back.setEnabled(false);

		boolean offline = ModConfig.getCommonConfig().getBoolean(ConfigKeys.WIKI_OFFLINE_MODE, false);

		content = new MarkdownRenderer(gui, new MdResourceLoader(gui::openURL, u -> {
			if(!stepping) {
				if(u.equals("https://github.com/tom5454/CustomPlayerModels/wiki/FirstStartGuide")) {
					close();
					gui.getFrame().openPopup(new FirstStartPopup(gui));
					return;
				}
				backQueue.push(u);
				fwdQueue.clear();
				fwd.setEnabled(false);
				back.setEnabled(backQueue.size() > 1);
			}
		}, offline), "Loading");
		content.setBounds(new Box(0, 0, 100, 100));
		addElement(content);
		content.browse(mainPage);

		sidebar = new MarkdownRenderer(gui, new MdResourceLoader(gui::openURL, null, offline), "Loading") {
			private boolean loaded;

			{
				setBounds(new Box(0, 0, 100, 100));
				browse("https://github.com/tom5454/CustomPlayerModels/wiki/_Sidebar.md");
				loaded = true;
			}

			@Override
			public void browse(String url) {
				if(!loaded)super.browse(url);
				else content.browse(url);
			}
		};

		openSidebar = new Button(gui, gui.i18nFormat("button.cpm.openWikiSidebar"), null) {

			@Override
			public void mouseClick(MouseEvent evt) {
				if(enabled && evt.isHovered(bounds)) {
					Vec2i p = evt.getPos();
					gui.getFrame().openPopup(new SidebarPopup(p.x - evt.x + bounds.x + bounds.w, p.y - evt.y + bounds.h + bounds.y));
					evt.consume();
				}
			}
		};
		addElement(openSidebar);

		Button openExt = new Button(gui, gui.i18nFormat("button.cpm.openWikiExt"), () -> {
			String p = backQueue.peek();
			if(p.endsWith(".md"))p = p.substring(0, p.length() - 3);
			gui.openURL(p);
		});
		openExt.setBounds(new Box(50, 5, 100, 20));
		addElement(openExt);

		onInit();
	}

	private class SidebarPopup extends PopupPanel {
		private boolean close, shouldClose;

		private SidebarPopup(int x, int y) {
			super(WikiBrowserPopup.this.gui);

			Box b = sidebar.getBounds();
			setBounds(new Box(x - b.w - 10, y, b.w + 10, b.h + 10));
			addElement(sidebar);
		}

		@Override
		public boolean hasDecoration() {
			return false;
		}

		@Override
		public void mouseRelease(MouseEvent event) {
			super.mouseRelease(event);
			if(shouldClose)close();
			else shouldClose = true;
		}

		@Override
		public void onInit() {
			close = true;
		}

		@Override
		public void draw(MouseEvent event, float partialTicks) {
			if(close)close();
			else super.draw(event, partialTicks);
		}
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("label.cpm.wiki.title");
	}

	@Override
	public void onInit() {
		Box b = gui.getFrame().getBounds();
		int w = b.w * 2 / 3;
		int h = b.h * 2 / 3;
		setBounds(new Box(0, 0, w, h));
		remove(sidebar);
		if(w < 500) {
			sidebar.setBounds(new Box(5, 5, SIDEBAR_W, h - 30));
			content.setBounds(new Box(5, 30, w - 10, h - 35));
			openSidebar.setVisible(true);
			openSidebar.setBounds(new Box(w - 45, 5, 40, 20));
		} else {
			addElement(sidebar);
			sidebar.setBounds(new Box(w - SIDEBAR_W - 5, 30, SIDEBAR_W, h - 35));
			content.setBounds(new Box(5, 30, w - SIDEBAR_W - 15, h - 35));
			openSidebar.setVisible(false);
			openSidebar.setBounds(new Box(0, 0, 0, 0));
		}
	}

	@Override
	public void onClosed() {
		content.cleanup();
		sidebar.cleanup();
	}
}
