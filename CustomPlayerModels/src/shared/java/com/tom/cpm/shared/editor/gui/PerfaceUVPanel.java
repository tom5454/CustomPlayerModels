package com.tom.cpm.shared.editor.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.model.render.PerFaceUV.Face;
import com.tom.cpm.shared.model.render.PerFaceUV.Rot;

public class PerfaceUVPanel extends Panel {

	public PerfaceUVPanel(Frame frm, Editor editor, TabFocusHandler tabHandler) {
		super(frm.getGui());
		setBounds(new Box(0, 0, 170, 95));

		DropDownBox<NamedElement<Direction>> faces = new DropDownBox<>(frm, Arrays.stream(Direction.VALUES).
				map(d -> new NamedElement<>(d, dir -> gui.i18nFormat("label.cpm.dir." + dir.name().toLowerCase()))).
				collect(Collectors.toList()));
		faces.setBounds(new Box(5, 0, 160, 20));
		faces.setAction(() -> {
			editor.perfaceFaceDir = faces.getSelected().getElem();
			editor.updateGui();
		});
		addElement(faces);

		Spinner spinnerSU = new Spinner(gui);
		Spinner spinnerSV = new Spinner(gui);
		Spinner spinnerEU = new Spinner(gui);
		Spinner spinnerEV = new Spinner(gui);

		spinnerSU.setBounds(new Box(5, 25, 35, 18));
		spinnerSV.setBounds(new Box(45, 25, 35, 18));
		spinnerEU.setBounds(new Box(85, 25, 35, 18));
		spinnerEV.setBounds(new Box(125, 25, 35, 18));
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
					autoUV(v, editor.perfaceFaceDir, el.size);
					editor.setFaceUVs.accept(v);
				}
				ab.updateValueOp(f, f.getVec(), v, Face::set);
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

		NameMapper<Rot> rotMap = new NameMapper<>(Rot.VALUES, rot -> gui.i18nFormat("label.cpm.rot." + rot.name().toLowerCase()));
		DropDownBox<NamedElement<Rot>> rots = new DropDownBox<>(frm, new ArrayList<>(rotMap.asList()));
		rotMap.setSetter(rots::setSelected);
		rots.setBounds(new Box(5, 45, 80, 20));
		rots.setAction(() -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				ActionBuilder ab = editor.action("rotateUV");
				Face f = getFace(editor, ab);
				ab.updateValueOp(f, f.rotation, rots.getSelected().getElem(), (a, b) -> a.rotation = b);
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
					autoUV(v, editor.perfaceFaceDir, el.size);
					ab.updateValueOp(f, f.getVec(), v, Face::set);
					editor.setFaceUVs.accept(v);
				}
				ab.execute();
			}
		});
		autoUV.setBounds(new Box(5, 70, 60, 18));
		editor.setAutoUV.add(autoUV::updateState);
		addElement(autoUV);

		ButtonIcon delBtn = new ButtonIcon(gui, "editor", 14, 16, () -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				Face f = el.faceUV.faces.get(editor.perfaceFaceDir);
				if(f != null) {
					editor.action("deleteFace").removeFromMap(el.faceUV.faces, editor.perfaceFaceDir, f).execute();
					editor.updateGui();
				}
			}
		});
		delBtn.setBounds(new Box(70, 70, 18, 18));
		delBtn.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.deleteFace")));
		addElement(delBtn);

		Button toAllUVs = new Button(gui, gui.i18nFormat("button.cpm.toAllFaces"), () -> {
			ModelElement el = editor.getSelectedElement();
			if(el != null && el.faceUV != null) {
				Face f = el.faceUV.faces.get(editor.perfaceFaceDir);
				if(f != null) {
					ActionBuilder ab = editor.action("toAllFaces");
					for(Direction d : Direction.VALUES) {
						if(d == editor.perfaceFaceDir)continue;
						ab.addToMap(el.faceUV.faces, d, new Face(f));
					}
					ab.execute();
					editor.updateGui();
				}
			}
		});
		toAllUVs.setBounds(new Box(92, 70, 70, 18));
		toAllUVs.setTooltip(new Tooltip(frm, gui.i18nFormat("tooltip.cpm.toAllFaces")));
		addElement(toAllUVs);
	}

	private static Face getFace(Editor editor, ActionBuilder ab) {
		ModelElement el = editor.getSelectedElement();
		Face f = el.faceUV.faces.get(editor.perfaceFaceDir);
		if(f == null) {
			f = new Face();
			ab.addToMap(el.faceUV.faces, editor.perfaceFaceDir, f);
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
