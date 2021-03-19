package com.tom.cpl.gui;

import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec2i;

public class MouseEvent extends GuiEvent {
	public int x, y, btn;

	public MouseEvent(int x, int y, int btn) {
		this.x = x;
		this.y = y;
		this.btn = btn;
	}

	public MouseEvent offset(Box bounds) {
		return offset(bounds.x, bounds.y);
	}

	public MouseEvent offset(int xo, int yo) {
		MouseEvent t = this;
		return new MouseEvent(x - xo, y - yo, btn) {
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