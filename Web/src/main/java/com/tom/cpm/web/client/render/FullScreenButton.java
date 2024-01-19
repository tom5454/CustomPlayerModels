package com.tom.cpm.web.client.render;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.UIColors;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpm.web.client.render.GuiImpl.HTMLNativeElement;
import com.tom.cpm.web.client.util.ScreenEx;

import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLStyleElement;
import jsinterop.base.Js;

public class FullScreenButton extends Panel {
	private Div div;

	public FullScreenButton(IGui gui, Runnable onClick) {
		super(gui);
		div = new Div();
		UIColors col = gui.getColors();

		HTMLStyleElement style = Js.cast(RenderSystem.getDocument().createElement("style"));
		style.innerHTML = ".fullScreenBtn:hover { color: " + HTMLNativeElement.toCSSColor(col.button_text_hover) + "; background-color: " + HTMLNativeElement.toCSSColor(col.button_hover) + "; }" +
				".fullScreenBtn { color: " + HTMLNativeElement.toCSSColor(col.button_text_color) + "; background-color: " + HTMLNativeElement.toCSSColor(col.button_fill) + "; }";

		div.element.append(style);
		HTMLAnchorElement btn = Js.cast(RenderSystem.getDocument().createElement("a"));
		btn.classList.add("fullScreenBtn");
		btn.style.fontSize = CSSProperties.FontSizeUnionType.of(RenderSystem.fontSize + "px");
		btn.style.textAlign = "center";
		int dr = RenderSystem.displayRatio;
		btn.style.padding = CSSProperties.PaddingUnionType.of((dr * 3) + "px 0px");
		btn.style.textDecoration = "none";
		btn.style.border = RenderSystem.displayRatio + "px solid " + HTMLNativeElement.toCSSColor(col.button_border);
		btn.style.fontFamily = "Minecraftia";
		btn.style.display = "block";
		btn.style.margin = CSSProperties.MarginUnionType.of("0px " + (dr * 2) + "px " + dr + "px 0px");
		btn.style.cursor = "pointer";
		btn.textContent = gui.i18nFormat("web-button.fullscreen");
		btn.onclick = e -> {
			onClick.run();
			ScreenEx s = Js.uncheckedCast(DomGlobal.screen);
			if (DomGlobal.document.fullscreenElement == null) {
				DomGlobal.document.documentElement.requestFullscreen();
				s.orientation.lock(s.orientation.type).catch_(ex -> null);
			} else {
				DomGlobal.document.exitFullscreen();
				s.orientation.unlock();
			}
			return null;
		};
		div.element.append(btn);
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		div.setBounds(bounds, visible);
		super.draw(event, partialTicks);
	}

	private class Div extends HTMLNativeElement<HTMLDivElement> {

		public Div() {
			super("div", gui);
		}
	}
}
