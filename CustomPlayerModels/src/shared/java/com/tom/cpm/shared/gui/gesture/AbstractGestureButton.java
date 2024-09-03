package com.tom.cpm.shared.gui.gesture;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ChooseElementPopup;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.gui.gesture.IGestureButtonContainer.BoundKeyInfo;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;

public class AbstractGestureButton extends Button implements IGestureButton {
	protected boolean isHovered;
	protected AbstractGestureButtonData data;
	protected BoundKeyInfo kb;
	protected IGestureButtonContainer container;

	public AbstractGestureButton(IGestureButtonContainer c, AbstractGestureButtonData data, String name, Runnable action) {
		super(c.gui(), name, action);
		this.data = data;
		this.container = c;
	}

	@Override
	public void draw(MouseEvent event, float partialTicks) {
		isHovered = event.isHovered(bounds);

		int w = gui.textWidth(name);
		int bgColor = gui.getColors().button_fill;
		int color = gui.getColors().button_text_color;
		if(!enabled) {
			color = gui.getColors().button_text_disabled;
			bgColor = gui.getColors().button_disabled;
		} else if(event.isHovered(bounds)) {
			color = gui.getColors().button_text_hover;
			bgColor = gui.getColors().button_hover;
		}
		if(event.isHovered(bounds) && tooltip != null)
			tooltip.set();
		gui.drawBox(bounds.x, bounds.y, bounds.w, bounds.h, gui.getColors().button_border);
		gui.drawBox(bounds.x+1, bounds.y+1, bounds.w-2, bounds.h-2, bgColor);
		int nameY = -4;
		if (kb != null && kb.bound != null) {
			int w2 = gui.textWidth(kb.bound);
			gui.drawText(bounds.x + bounds.w / 2 - w2 / 2, bounds.y + bounds.h / 2 + 4, kb.bound, color);
			nameY = -10;
		}
		gui.drawText(bounds.x + bounds.w / 2 - w / 2, bounds.y + bounds.h / 2 + nameY, name, color);
	}

	protected boolean canHold() {
		return true;
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if (event.isHovered(bounds) && event.btn == 1 && container.canBindKeys()) {
			PopupMenu p = new PopupMenu(gui, gui.getFrame());

			p.addButton(gui.i18nFormat("button.cpm.quick_key.unbind"), () -> {
				container.updateKeybind(null, data.getKeybindId(), false);
			});

			p.addButton(gui.i18nFormat("button.cpm.quick_key.bindMenu"), () -> {
				List<QuickAccess> qas = IntStream.rangeClosed(1, IKeybind.QUICK_ACCESS_KEYBINDS_COUNT).mapToObj(QuickAccess::new).collect(Collectors.toList());
				gui.getFrame().openPopup(new ChooseElementPopup<>(gui.getFrame(), gui.i18nFormat("button.cpm.quick_key.bindMenu"), gui.i18nFormat("label.cpm.quick_key.bindMenu"), qas, q -> {
					container.updateKeybind(q.getId(), data.getKeybindId(), canHold() && gui.isCtrlDown());
				}, null));
			});

			Vec2i pos = event.getPos();
			p.display(pos.x, pos.y);
			event.consume();
		}
		super.mouseClick(event);
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		super.keyPressed(event);
		if (isHovered) {
			String keybindPressed = null;
			for (IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
				if(kb.getName().startsWith("qa")) {
					if(kb.isPressed(event)) {
						keybindPressed = kb.getName();
					}
				}
			}
			if (keybindPressed != null) {
				container.updateKeybind(keybindPressed, data.getKeybindId(), canHold() && gui.isCtrlDown());
				event.consume();
			}
		}
	}

	@Override
	public void updateKeybinds() {
		kb = container.getBoundKey(data.getKeybindId());
	}

	private class QuickAccess {
		private final int id;

		public QuickAccess(int id) {
			this.id = id;
		}

		public String getId() {
			return "qa_" + id;
		}

		@Override
		public String toString() {
			String i = getId();
			String bound = null;
			for (IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
				if (kb.getName().equals(i)) {
					bound = kb.getBoundKey();
					if (bound.isEmpty())bound = null;
				}
			}
			if (bound == null)bound = gui.i18nFormat("label.cpm.key_unbound");
			return gui.i18nFormat("key.cpm.qa_" + id) + " (" + bound + ")";
		}
	}
}
