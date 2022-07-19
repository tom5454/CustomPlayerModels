package com.tom.cpm.shared.editor.gui;

import com.tom.cpl.function.TriFunction;
import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.gui.popup.ColorButton;

public enum ModeDisplayType {
	NULL(ModePanel::new),
	COLOR(ColorPanel::new),
	TEX(UVPanel::new),
	VALUE(ValuePanel::new),
	TEX_FACE(PerfaceUVPanel::new),
	;

	public final TriFunction<Frame, Editor, TabFocusHandler, Panel> factory;
	public static final ModeDisplayType[] VALUES = values();

	private ModeDisplayType(TriFunction<Frame, Editor, TabFocusHandler, Panel> factory) {
		this.factory = factory;
	}

	public static class ModePanel extends Panel {

		public ModePanel(Frame frm, Editor editor, TabFocusHandler tabHandler) {
			super(frm.getGui());
			setBounds(new Box(0, 0, 170, 22));

			Button modeBtn = new Button(gui, gui.i18nFormat("button.cpm.mode"), editor::switchMode);
			modeBtn.setBounds(new Box(5, 0, 160, 20));
			editor.setModeBtn.add(b -> {
				if(b == null) {
					modeBtn.setEnabled(false);
					modeBtn.setText(gui.i18nFormat("button.cpm.mode"));
				} else {
					modeBtn.setEnabled(true);
					modeBtn.setText(b);
				}
			});
			addElement(modeBtn);
		}
	}

	public static class ColorPanel extends ModePanel {

		public ColorPanel(Frame frm, Editor editor, TabFocusHandler tabHandler) {
			super(frm, editor, tabHandler);
			setBounds(new Box(0, 0, 170, 47));

			ColorButton colorBtn = new ColorButton(gui, frm, editor::setColor);
			addElement(colorBtn);
			editor.setPartColor.add(c -> {
				if(c != null)colorBtn.setColor(c);
			});
			colorBtn.setBounds(new Box(5, 25, 160, 20));
		}

	}

	public static class ValuePanel extends Panel {
		private Spinner spinnerS;

		public ValuePanel(Frame frm, Editor editor, TabFocusHandler tabHandler) {
			super(frm.getGui());
			setBounds(new Box(0, 0, 170, 20));

			spinnerS = new Spinner(gui);
			spinnerS.setBounds(new Box(5, 0, 160, 18));
			spinnerS.setDp(2);
			spinnerS.addChangeListener(() -> editor.setValue(spinnerS.getValue()));
			editor.setValue.add(spinnerS::setValue);
			addElement(spinnerS);
			tabHandler.add(spinnerS);
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			spinnerS.setVisible(visible);
		}
	}
}