package com.tom.cpm.shared.gui.gesture;

import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.GuiElement;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.math.Box;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.parts.anim.menu.DropdownButtonData;

public class GestureDropdown extends Panel implements IGestureButton {
	private NameMapper<String> gestureNameMapper;
	private DropDownBox<NamedElement<String>> box;

	public GestureDropdown(IGestureButtonContainer c, DropdownButtonData data) {
		super(c.gui());
		String none = gui.i18nFormat("label.cpm.layerNone");
		gestureNameMapper = new NameMapper<>(data.getActiveOptions(), e -> e.isEmpty() ? none : e);
		box = new DropDownBox<>(gui.getFrame(), gestureNameMapper.asList());
		gestureNameMapper.setSetter(box::setSelected);

		addElement(new Label(gui, data.getName()).setBounds(new Box(0, 0, 70, 10)));
		addElement(box);

		box.setAction(() -> {
			data.set(box.getSelected().getElem());
			c.valueChanged();
		});
		gestureNameMapper.setValue(data.get());
	}

	@Override
	public GuiElement setBounds(Box bounds) {
		if (bounds.h < 30)box.setBounds(new Box(bounds.w / 2, 0, bounds.w / 2, bounds.h));
		else box.setBounds(new Box(0, 10, bounds.w, bounds.h - 10));
		return super.setBounds(bounds);
	}

	@Override
	public void updateKeybinds() {
	}
}
