package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.config.ConfigEntry;
import com.tom.cpl.gui.IKeybind;
import com.tom.cpl.gui.KeyboardEvent;
import com.tom.cpl.gui.MouseEvent;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.PopupMenu;
import com.tom.cpl.math.Vec2i;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.config.ConfigKeys;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.gui.gesture.IGestureButtonContainer.BoundKeyInfo;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;

public class AbstractGestureButton extends Button implements IGestureButton {
	protected boolean isHovered;
	private AbstractGestureButtonData data;
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
		super.draw(event, partialTicks);
	}

	protected boolean canHold() {
		return true;
	}

	@Override
	public void mouseClick(MouseEvent event) {
		if (event.isHovered(bounds) && event.btn == 1) {
			PopupMenu p = new PopupMenu(gui, gui.getFrame());
			for (int i = 1;i<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;i++) {
				String id = "qa_" + i;
				p.addButton(gui.i18nFormat("button.cpm.quick_key.bind", i), () -> {
					container.updateKeybind(data.getKeybindId(), id, canHold() && gui.isCtrlDown());
				});
			}

			p.addButton(gui.i18nFormat("button.cpm.quick_key.unbind"), () -> {
				ConfigEntry ce = ModConfig.getCommonConfig().getEntry(ConfigKeys.KEYBINDS);
				for(int j = 1;j<=IKeybind.QUICK_ACCESS_KEYBINDS_COUNT;j++) {
					String c = ce.getString("qa_" + j, null);
					if(c != null && c.equals(data.getKeybindId())) {
						ce.setString("qa_" + j, "");
					}
				}
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
		String keybindPressed = null;
		for (IKeybind kb : MinecraftClientAccess.get().getKeybinds()) {
			if(kb.getName().startsWith("qa")) {
				if(kb.isPressed(event)) {
					keybindPressed = kb.getName();
				}
			}
		}
		if (keybindPressed != null) {
			container.updateKeybind(data.getKeybindId(), keybindPressed, canHold() && gui.isCtrlDown());
			event.consume();
		}
	}

	@Override
	public void updateKeybinds() {
		kb = container.getBoundKey(data.getKeybindId());
	}
}
