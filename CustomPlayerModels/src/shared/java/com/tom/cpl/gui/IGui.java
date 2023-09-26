package com.tom.cpl.gui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ConfirmPopup;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.MessagePopup;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;
import com.tom.cpl.text.IText;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.util.ErrorLog;
import com.tom.cpm.shared.util.ErrorLog.LogLevel;
import com.tom.cpm.shared.util.Log;

public interface IGui extends UI {
	public static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");

	void drawBox(int x, int y, int w, int h, int color);
	void drawGradientBox(int x, int y, int w, int h, int topLeft, int topRight, int bottomLeft, int bottomRight);
	void drawText(int x, int y, String text, int color);
	@Override
	String i18nFormat(String key, Object... obj);
	int textWidth(String text);
	void drawTexture(int x, int y, int w, int h, int u, int v, String texture);
	void drawTexture(int x, int y, int w, int h, int u, int v, String texture, int color);
	void drawTexture(int x, int y, int width, int height, float u1, float v1, float u2, float v2);
	void closeGui();
	UIColors getColors();
	void setCloseListener(Consumer<Runnable> listener);
	boolean isShiftDown();
	boolean isCtrlDown();
	boolean isAltDown();
	KeyCodes getKeyCodes();
	NativeGuiComponents getNative();
	void setClipboardText(String text);
	String getClipboardText();
	Frame getFrame();
	void setScale(int value);
	int getScale();
	int getMaxScale();
	CtxStack getStack();
	void displayError(String msg);
	void drawFormattedText(float x, float y, IText text, int color, float scale);
	int textWidthFormatted(IText text);
	void openURL0(String uri);
	void drawStack(int x, int y, com.tom.cpl.item.Stack stack);
	void drawStackTooltip(int mx, int my, com.tom.cpl.item.Stack stack);

	default void drawBox(float x, float y, float w, float h, int color) {
		drawBox((int) x, (int) y, (int) w, (int) h, color);
	}

	@Override
	default void executeLater(Runnable r) {
		MinecraftClientAccess.get().executeLater(() -> {
			try {
				r.run();
			} catch (Throwable e) {
				Log.error("Exception while executing task", e);
				ErrorLog.addLog(LogLevel.ERROR, "Exception while executing task", e);
			}
		});
	}

	public static class Ctx {
		public Vec2i off;
		public Box cutBox;

		public Ctx(Ctx old) {
			off = new Vec2i(old.off);
			cutBox = new Box(old.cutBox);
		}

		public Ctx(int w, int h) {
			off = new Vec2i(0, 0);
			cutBox = new Box(0, 0, w, h);
		}
	}

	default void pushMatrix() {
		getStack().push();
	}

	default void setPosOffset(Box box) {
		Ctx current = getContext();
		current.cutBox = current.cutBox.intersect(new Box(current.off.x + box.x, current.off.y + box.y, box.w, box.h));
		current.cutBox.w = Math.max(current.cutBox.w, 0);
		current.cutBox.h = Math.max(current.cutBox.h, 0);
		current.off = new Vec2i(current.off.x + box.x, current.off.y + box.y);
	}

	void setupCut();

	default void popMatrix() {
		getStack().pop();
	}

	default Ctx getContext() {
		return getStack().current;
	}

	public static class CtxStack {
		private Stack<Ctx> stack;
		private Ctx current;

		public CtxStack(int w, int h) {
			current = new Ctx(w, h);
			stack = new Stack<>();
		}

		public void push() {
			stack.push(current);
			current = new Ctx(current);
		}

		public Ctx pop() {
			return current = stack.pop();
		}
	}

	default Vec2i getOffset() {
		return getStack().current.off;
	}

	default void drawRectangle(int x, int y, int w, int h, int color) {
		drawBox(x, y, w, 1, color);
		drawBox(x, y, 1, h, color);
		drawBox(x, y+h-1, w, 1, color);
		drawBox(x+w-1, y, 1, h, color);
	}

	default void drawRectangle(float x, float y, float w, float h, int color) {
		drawBox(x, y, w, 1, color);
		drawBox(x, y, 1, h, color);
		drawBox(x, y+h-1, w, 1, color);
		drawBox(x+w-1, y, 1, h, color);
	}

	@Override
	default void onGuiException(String msg, Throwable e, boolean fatal) {
		Log.error(msg, e);
		ErrorLog.addLog(LogLevel.ERROR, msg, e);
		if(fatal) {
			Frame frm = getFrame();
			if(frm != null) {
				try {
					frm.onCrashed(msg, e);
				} catch (Throwable ex) {
					e.addSuppressed(ex);
				}
			}
			displayError(msg + ": " + e.toString());
		} else {
			Frame frm = getFrame();
			if(frm != null) {
				try {
					frm.logMessage(msg + ": " + e.toString());
				} catch (Throwable ex) {
					e.addSuppressed(ex);
					onGuiException(msg, e, true);
				}
			} else {
				displayError(msg + "\n" + e.toString());
			}
		}
	}

	default String wordWrap(String in, int w) {
		return wordWrap(in, w, this::textWidth);
	}

	public static String wordWrap(String in, int w, ToIntFunction<String> width) {
		List<String> text = new ArrayList<>();
		int splitStart = 0;
		int space = -1;
		for (int i = 0;i<in.length();i++) {
			char c = in.charAt(i);
			if(c == ' ' || c == '\\') {
				String s = in.substring(splitStart, i);
				int lw = width.applyAsInt(s);
				if(lw > w) {
					if(splitStart == space + 1) {
						text.add(s);
						splitStart = i + 1;
					} else {
						text.add(in.substring(splitStart, space));
						splitStart = space + 1;
					}
				}
				space = i;
			}
			if(c == '\\') {
				text.add(in.substring(splitStart, i));
				splitStart = i + 1;
				continue;
			}
		}
		text.add(in.substring(splitStart, in.length()));
		return text.stream().collect(Collectors.joining("\\"));
	}

	default void drawText(int x, int y, String text, int bgColor, int color) {
		drawBox(x, y, textWidth(text), 10, bgColor);
		drawText(x, y + 1, text, color);
	}

	default boolean canScaleVanilla() {
		return true;
	}

	default void openURL(String url) {
		try {
			URI uri = new URI(url);
			String s = uri.getScheme();
			if (s == null) {
				throw new URISyntaxException(url, "Missing protocol");
			}

			if (!ALLOWED_PROTOCOLS.contains(s.toLowerCase(Locale.ROOT))) {
				throw new URISyntaxException(url, "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
			}

			ConfirmPopup p = new ConfirmPopup(getFrame(), i18nFormat("label.cpm.openURL.title"), i18nFormat("label.cpm.openURL.text", url), () -> {
				openURL0(url);
			}, null);
			GuiElement cancel = p.getElements().remove(p.getElements().size() - 1);
			String c = i18nFormat("button.cpm.copy");
			Button copy = new Button(this, c, () -> {
				setClipboardText(url);
				p.close();
			});
			copy.setBounds(new Box(cancel.getBounds().x, cancel.getBounds().y, 25 + textWidth(c), 20));
			cancel.setBounds(new Box(cancel.getBounds().x + copy.getBounds().w + 5, cancel.getBounds().y, cancel.getBounds().w, cancel.getBounds().h));
			p.addElement(copy);
			p.addElement(cancel);

			getFrame().openPopup(p);
		} catch (URISyntaxException urisyntaxexception) {
			Log.error("Can't open url for " + url, urisyntaxexception);
		}
	}

	default <T> T getNativeGui() {
		return (T) this;
	}

	@Override
	default void displayMessagePopup(String title, String text, String closeBtn) {
		getFrame().openPopup(new MessagePopup(getFrame(), title, text, closeBtn));
	}

	@Override
	default void displayPopup(Function<Frame, PopupPanel> factory) {
		getFrame().openPopup(factory.apply(getFrame()));
	}

	@Override
	default void displayConfirm(String title, String msg, Runnable ok, Runnable cancel, String okTxt,
			String cancelTxt) {
		getFrame().openPopup(new ConfirmPopup(getFrame(), title, msg, ok, cancel, okTxt, cancelTxt));
	}
}
