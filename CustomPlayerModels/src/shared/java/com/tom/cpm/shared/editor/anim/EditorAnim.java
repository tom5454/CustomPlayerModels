package com.tom.cpm.shared.editor.anim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.IPose;
import com.tom.cpm.shared.animation.InterpolatorChannel;
import com.tom.cpm.shared.animation.PolynomialSplineInterpolator;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.editor.gui.AnimPanel.IAnim;

public class EditorAnim implements IAnim {
	private List<ModelElement> components;
	private final List<AnimFrame> frames = new ArrayList<>();
	public int duration = 1000;
	private DoubleUnaryOperator[][] psfs;
	private AnimFrame currentFrame;
	public final Editor editor;
	public String filename;
	public AnimationType type;
	public IPose pose;
	public String displayName;
	public boolean add = true;
	public boolean loop;
	public int priority;

	public EditorAnim(Editor e, String filename, AnimationType type, boolean initNew) {
		this.editor = e;
		this.filename = filename;
		this.type = type;
		if(initNew)addFrame();
	}

	private void calculateSplines() {
		components = frames.stream().flatMap(AnimFrame::getAllElements).distinct().collect(Collectors.toList());

		psfs = new DoubleUnaryOperator[components.size()][InterpolatorChannel.VALUES.length];

		for (int component = 0; component < components.size(); component++) {
			for (InterpolatorChannel channel : InterpolatorChannel.VALUES) {
				PolynomialSplineInterpolator i = new PolynomialSplineInterpolator();
				i.init(AnimFrame.toArray(this, components.get(component), channel), channel);
				psfs[component][channel.channelID()] = i;
			}
		}
	}

	public float getValue(ModelElement component, InterpolatorChannel attribute, double time) {
		return (float) psfs[components.indexOf(component)][attribute.channelID()].applyAsDouble(time);
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
					getValue(component, InterpolatorChannel.ROT_X, step),
					getValue(component, InterpolatorChannel.ROT_Y, step),
					getValue(component, InterpolatorChannel.ROT_Z, step)
					);
			component.rc.setPosition(add,
					getValue(component, InterpolatorChannel.POS_X, step),
					getValue(component, InterpolatorChannel.POS_Y, step),
					getValue(component, InterpolatorChannel.POS_Z, step));
			component.rc.setColor(
					getValue(component, InterpolatorChannel.COLOR_R, step),
					getValue(component, InterpolatorChannel.COLOR_G, step),
					getValue(component, InterpolatorChannel.COLOR_B, step));
			component.rc.setVisible(frames.get((int) step).getVisible(component));
		}
	}

	public void setPosition(Vec3f v) {
		if(currentFrame != null && editor.getSelectedElement() != null) {
			currentFrame.setPos(editor.getSelectedElement(), v);
		}
		components = null;
		psfs = null;
	}

	public void setRotation(Vec3f v) {
		if(currentFrame != null && editor.getSelectedElement() != null) {
			currentFrame.setRot(editor.getSelectedElement(), v);
		}
		components = null;
		psfs = null;
	}

	public void switchVisible() {
		if(currentFrame != null && editor.getSelectedElement() != null) {
			currentFrame.switchVis(editor.getSelectedElement());
		}
		components = null;
		psfs = null;
	}

	public void clearSelectedData() {
		if(currentFrame != null && editor.getSelectedElement() != null) {
			currentFrame.clearSelectedData(editor.getSelectedElement());
		}
		components = null;
		psfs = null;
	}

	public void setColor(int rgb) {
		if(currentFrame != null && editor.getSelectedElement() != null) {
			currentFrame.setColor(editor.getSelectedElement(), rgb);
		}
		components = null;
		psfs = null;
	}

	public void addFrame() {
		AnimFrame frm = new AnimFrame(this);
		editor.action("add", "action.cpm.animFrame").addToList(frames, frm).onAction(this::clearCache).execute();
		if(currentFrame != null)frm.copy(currentFrame);
		currentFrame = frm;
	}

	public void deleteFrame() {
		if(currentFrame != null) {
			editor.action("remove", "action.cpm.animFrame").removeFromList(frames, currentFrame).onAction(this::clearCache).execute();
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
		return frames.stream().flatMap(AnimFrame::getAllElementsFiltered).distinct().collect(Collectors.toList());
	}

	@Override
	public String toString() {
		if(pose != null)return editor.gui().i18nFormat("label.cpm.anim_pose", pose.getName(editor.gui(), displayName));
		return editor.gui().i18nFormat("label.cpm.anim_gesture", displayName);
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
		return type == AnimationType.GESTURE || pose instanceof CustomPose;
	}

	public void clearCache() {
		components = null;
		psfs = null;
	}
}
