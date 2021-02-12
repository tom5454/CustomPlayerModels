package com.tom.cpm.shared.gui;

import com.tom.cpm.shared.math.Box;
import com.tom.cpm.shared.math.Vec2i;

public class Gui {
	public void draw(int mouseX, int mouseY, float partialTicks) {}
	public void keyPressed(KeyboardEvent event) {}
	public void mouseClick(MouseEvent event) {}
	public void mouseRelease(MouseEvent event) {}
	public void mouseDrag(MouseEvent event) {}
	public void mouseWheel(MouseEvent event) {}

	public static class GuiEvent {
		private boolean consumed;

		public boolean isConsumed() {
			return consumed;
		}

		public void consume() {
			this.consumed = true;
		}
	}

	public static class MouseEvent extends GuiEvent {
		public int x, y, btn;

		public MouseEvent(int x, int y, int btn) {
			this.x = x;
			this.y = y;
			this.btn = btn;
		}

		public MouseEvent offset(Box bounds) {
			MouseEvent t = this;
			return new MouseEvent(x - bounds.x, y - bounds.y, btn) {
				@Override
				public boolean isConsumed() {
					return t.isConsumed();
				}

				@Override
				public void consume() {
					t.consume();
				}

				@Override
				public Vec2i getPos() {
					return t.getPos();
				}
			};
		}

		public Vec2i getPos() {
			return new Vec2i(x, y);
		}

		public boolean isInBounds(Box bounds) {
			return bounds.isInBounds(x, y);
		}
	}

	public static class KeyboardEvent extends GuiEvent {
		public int keyCode, scancode;
		public char charTyped;
		public String keyName;

		public KeyboardEvent(int keyCode, int scancode, char charTyped, String keyName) {
			this.keyCode = keyCode;
			this.scancode = scancode;
			this.charTyped = charTyped;
			this.keyName = keyName;
		}

		public boolean matches(int keyCode) {
			return this.keyCode == keyCode;
		}

		public boolean matches(String keyName) {
			return this.keyName != null && this.keyName.equalsIgnoreCase(keyName);
		}
	}
}
