package com.tom.cpm.shared.editor.anim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tom.cpm.externals.org.apache.commons.math3.PolynomialSplineFunction;
import com.tom.cpm.externals.org.apache.commons.math3.SplineInterpolator;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.gui.AnimPanel.IAnim;
import com.tom.cpm.shared.math.Vec3f;

public class EditorAnim implements IAnim {
	private List<ModelElement> components;
	private final List<AnimFrame> frames = new ArrayList<>();
	public int duration = 1000;
	private PolynomialSplineFunction[][] psfs;
	private AnimFrame currentFrame;
	public final Editor editor;
	public final String filename;
	public IPose pose;
	public String gestureName;
	public boolean add = true;
	public boolean loop;

	public EditorAnim(Editor e, String filename, boolean initNew) {
		this.editor = e;
		this.filename = filename;
		if(initNew)addFrame();
	}

	private void calculateSplines() {
		components = frames.stream().flatMap(AnimFrame::getAllElements).distinct().collect(Collectors.toList());

		psfs = new PolynomialSplineFunction[components.size()][9];

		SplineInterpolator si = new SplineInterpolator();

		double[] xArr = new double[frames.size() + 5];
		for (int i = 0; i < frames.size() + 5; i++)
			xArr[i] = i - 2;

		for (int component = 0; component < components.size(); component++) {
			double[][] yArr = new double[9][frames.size() + 5];
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < frames.size() + 5; j++) {
					IElem data = frames.get((j + frames.size() - 2) % frames.size()).getData(components.get(component));
					if (data == null)
						data = components.get(component);
					yArr[i][j] = data.part(i);
				}

				psfs[component][i] = si.interpolate(xArr, yArr[i]);
			}
		}
	}

	public float getValue(ModelElement component, int attribute, double time) {
		return (float) psfs[components.indexOf(component)][attribute].value(time);
	}

	public void apply() {
		if(currentFrame != null) {
			currentFrame.apply();
		}
	}

	public void applyPlay(long millis) {
		if(components == null || psfs == null)calculateSplines();
		float step = (float) millis % duration / duration * frames.size();
		for (int i = 0; i < components.size(); i++) {
			ModelElement component = components.get(i);
			component.rc.setRotation(add,
					(float) Math.toRadians(getValue(component, 3, step)),
					(float) Math.toRadians(getValue(component, 4, step)),
					(float) Math.toRadians(getValue(component, 5, step))
					);
			component.rc.setPosition(add, getValue(component, 0, step), getValue(component, 1, step), getValue(component, 2, step));
			component.rc.setColor(getValue(component, 6, step), getValue(component, 7, step), getValue(component, 8, step));
			component.rc.setVisible(frames.get((int) step).getVisible(component));
		}
	}

	public void setPosition(Vec3f v) {
		if(currentFrame != null) {
			currentFrame.setPos(editor.selectedElement, v);
		}
		components = null;
	}

	public void setRotation(Vec3f v) {
		if(currentFrame != null) {
			currentFrame.setRot(editor.selectedElement, v);
		}
		components = null;
	}

	public void switchVisible() {
		if(currentFrame != null) {
			currentFrame.switchVis(editor.selectedElement);
		}
		components = null;
	}

	public void setColor(int rgb) {
		if(currentFrame != null) {
			currentFrame.setColor(editor.selectedElement, rgb);
		}
		components = null;
	}

	public void addFrame() {
		AnimFrame frm = new AnimFrame(this);
		editor.addUndo(() -> frames.remove(frm));
		editor.runOp(() -> frames.add(frm));
		currentFrame = frm;
	}

	public void deleteFrame() {
		if(currentFrame != null) {
			AnimFrame frm = currentFrame;
			editor.addUndo(() -> frames.add(frm));
			editor.runOp(() -> frames.remove(frm));
		}
	}

	public void loadFrame(Map<String, Object> data) {
		AnimFrame frm = new AnimFrame(this);
		frm.loadFrom(data);
		frames.add(frm);
		if(currentFrame == null)currentFrame = frm;
	}

	public List<Map<String, Object>> writeFrames() {
		return frames.stream().map(AnimFrame::store).collect(Collectors.toList());
	}

	public List<AnimFrame> getFrames() {
		return frames;
	}

	public List<ModelElement> getComponentsFiltered() {
		return components = frames.stream().flatMap(AnimFrame::getAllElementsFiltered).distinct().collect(Collectors.toList());
	}

	@Override
	public String toString() {
		if(pose != null)return editor.gui.getGui().i18nFormat("label.cpm.anim_pose", pose.getName(editor.gui.getGui()::i18nFormat));
		return editor.gui.getGui().i18nFormat("label.cpm.anim_gesture", gestureName);
	}

	public AnimFrame getSelectedFrame() {
		return currentFrame;
	}

	public void prevFrame() {
		if(currentFrame == null && !frames.isEmpty())currentFrame = frames.get(0);
		if(frames.size() > 1) {
			int ind = frames.indexOf(currentFrame) - 1 + frames.size();
			currentFrame = frames.get(ind % frames.size());
		}
	}

	public void nextFrame() {
		if(currentFrame == null && !frames.isEmpty())currentFrame = frames.get(0);
		if(frames.size() > 1) {
			int ind = frames.indexOf(currentFrame) + 1;
			currentFrame = frames.get(ind % frames.size());
		}
	}

	public boolean isCustom () {
		return gestureName != null || pose instanceof CustomPose;
	}
}
