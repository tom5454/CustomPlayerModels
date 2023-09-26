package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.Locale;

import com.tom.cpl.gui.Frame;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.ButtonIcon;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.TabFocusHandler;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.MathHelper;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.util.Direction;
import com.tom.cpl.util.NamedElement;
import com.tom.cpl.util.NamedElement.NameMapper;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;

public class PerfaceUVPanel extends Panel {
	private Spinner spinnerSU, spinnerSV, spinnerEU, spinnerEV;

	public PerfaceUVPanel(Frame frm, Editor editor, TabFocusHandler tabHandler) {
		super(frm.getGui());
		setBounds(new Box(0, 0, 170, 100));

		NameMapper<Direction> dirMap = new NameMapper<>(Direction.VALUES, dir -> gui.i18nFormat("label.cpm.dir." + dir.name().toLowerCase(Locale.ROOT)));
		DropDownBox<NamedElement<Direction>> faces = new DropDownBox<>(frm, dirMap.asList());
		faces.setBounds(new Box(5, 0, 160, 20));
		dirMap.setSetter(faces::setSelected);
		faces.setAction(() -> {
			editor.perfaceFaceDir.accept(faces.getSelected().getElem());
			editor.updateGui();
		});
		editor.perfaceFaceDir.add(dirMap::setValue);
		addElement(faces);

		spinnerSU = new Spinner(gui);
		spinnerSV = new Spinner(gui);
		spinnerEU = new Spinner(gui);
		spinnerEV = new Spinner(gui);

		spinnerSU.setBounds(new Box(5, 25, 35, 20));
		spinnerSV.setBounds(new Box(45, 25, 35, 20));
		spinnerEU.setBounds(new Box(90, 25, 35, 20));
		spinnerEV.setBounds(new Box(130, 25, 35, 20));
		spinnerSU.setDp(0);
		spinnerSV.setDp(0);
		spinnerEU.setDp(0);
		spinnerEV.setDp(0);

		Runnable r = () -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				ActionBuilder ab = editor.action("set", "action.cpm.texUV");
				Face f = getFace(editor, ab);
				Vec4f v = new Vec4f(spinnerSU.getValue(), spinnerSV.getValue(), spinnerEU.getValue(), spinnerEV.getValue());
				if(f.autoUV) {
					autoUV(v, editor.perfaceFaceDir.get(), el.size);
					editor.setFaceUVs.accept(v);
				}
				ab.updateValueOp(f, f.getVec(), v, Face::set);
				ab.onAction(el::markDirty);
				ab.execute();
			}
		};
		Runnable r2 = () -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				ActionBuilder ab = editor.action("set", "action.cpm.texUV");
				Face f = getFace(editor, ab);
				Vec4f v = new Vec4f(spinnerSU.getValue(), spinnerSV.getValue(), spinnerEU.getValue(), spinnerEV.getValue());
				ab.updateValueOp(f, f.getVec(), v, Face::set);
				ab.updateValueOp(f, f.autoUV, false, (a, b) -> a.autoUV = b);
				ab.onAction(el::markDirty);
				ab.execute();
			}
		};
		spinnerSU.addChangeListener(r);
		spinnerSV.addChangeListener(r);
		spinnerEU.addChangeListener(r2);
		spinnerEV.addChangeListener(r2);

		tabHandler.add(spinnerSU);
		tabHandler.add(spinnerSV);
		tabHandler.add(spinnerEU);
		tabHandler.add(spinnerEV);

		addElement(spinnerSU);
		addElement(spinnerSV);
		addElement(spinnerEU);
		addElement(spinnerEV);

		editor.setFaceUVs.add(v -> {
			spinnerSU.setValue(v.x);
			spinnerSV.setValue(v.y);
			spinnerEU.setValue(v.z);
			spinnerEV.setValue(v.w);
		});

		NameMapper<Rot> rotMap = new NameMapper<>(Rot.VALUES, rot -> gui.i18nFormat("label.cpm.rot." + rot.name().toLowerCase(Locale.ROOT)));
		DropDownBox<NamedElement<Rot>> rots = new DropDownBox<>(frm, new ArrayList<>(rotMap.asList()));
		rotMap.setSetter(rots::setSelected);
		rots.setBounds(new Box(5, 50, 80, 20));
		rots.setAction(() -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				ActionBuilder ab = editor.action("rotateUV");
				Face f = getFace(editor, ab);
				ab.updateValueOp(f, f.rotation, rots.getSelected().getElem(), (a, b) -> a.rotation = b);
				ab.onAction(el::markDirty);
				ab.execute();
			}
		});
		editor.setFaceRot.add(rotMap::setValue);
		addElement(rots);

		Checkbox autoUV = new Checkbox(gui, gui.i18nFormat("label.cpm.auto_uv"));
		autoUV.setAction(() -> {
			autoUV.setSelected(!autoUV.isSelected());
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				ActionBuilder ab = editor.action("autoUV");
				Face f = getFace(editor, ab);
				ab.updateValueOp(f, f.autoUV, autoUV.isSelected(), (a, b) -> a.autoUV = b);
				if(!f.autoUV) {
					Vec4f v = f.getVec();
					autoUV(v, editor.perfaceFaceDir.get(), el.size);
					ab.updateValueOp(f, f.getVec(), v, Face::set);
					editor.setFaceUVs.accept(v);
				}
				ab.onAction(el::markDirty);
				ab.execute();
			}
		});
		autoUV.setBounds(new Box(5, 75, 60, 20));
		editor.setAutoUV.add(autoUV::updateState);
		addElement(autoUV);

		ButtonIcon delBtn = new ButtonIcon(gui, "editor", 16, 16, () -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				Face f = el.faceUV.faces.get(editor.perfaceFaceDir.get());
				if(f != null) {
					editor.action("deleteFace").removeFromMap(el.faceUV.faces, editor.perfaceFaceDir.get(), f).onAction(el::markDirty).execute();
					editor.updateGui();
				}
			}
		});
		delBtn.setBounds(new Box(70, 75, 20, 20));
		delBtn.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.deleteFace")));
		addElement(delBtn);

		Button toAllUVs = new Button(gui, gui.i18nFormat("button.cpm.toAllFaces"), () -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				Face f = el.faceUV.faces.get(editor.perfaceFaceDir.get());
				if(f != null) {
					ActionBuilder ab = editor.action("toAllFaces");
					for(Direction d : Direction.VALUES) {
						if(d == editor.perfaceFaceDir.get())continue;
						ab.addToMap(el.faceUV.faces, d, new Face(f));
					}
					ab.onAction(el::markDirty);
					ab.execute();
					editor.updateGui();
				}
			}
		});
		toAllUVs.setBounds(new Box(95, 75, 70, 20));
		toAllUVs.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.toAllFaces")));
		addElement(toAllUVs);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		spinnerSU.setVisible(visible);
		spinnerSV.setVisible(visible);
		spinnerEU.setVisible(visible);
		spinnerEV.setVisible(visible);
	}

	private static Face getFace(Editor editor, ActionBuilder ab) {
		ModelElement el = editor.getSelectedElement();
		Face f = el.faceUV.faces.get(editor.perfaceFaceDir.get());
		if(f == null) {
			f = new Face();
			ab.addToMap(el.faceUV.faces, editor.perfaceFaceDir.get(), f);
		}
		return f;
	}

	private static void autoUV(Vec4f v, Direction dir, Vec3f size) {
		int dx = MathHelper.ceil(size.x);
		int dy = MathHelper.ceil(size.y);
		int dz = MathHelper.ceil(size.z);
		int tx = (int) v.x;
		int ty = (int) v.y;
		switch (dir) {
		case NORTH:
		case SOUTH:
			tx += dx;
			ty += dy;
			break;

		case UP:
		case DOWN:
			tx += dx;
			ty += dz;
			break;

		case EAST:
		case WEST:
			tx += dz;
			ty += dy;
			break;

		default:
			break;
		}
		v.z = tx;
		v.w = ty;
	}
}
