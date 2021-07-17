package com.tom.cpm.shared.editor.gui;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.tom.cpl.gui.IGui;
import com.tom.cpl.util.Hand;
import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.HeldItem;
import com.tom.cpm.shared.editor.ModelElement;
import com.tom.cpm.shared.gui.ViewportPanelBase;
import com.tom.cpm.shared.model.RootModelType;
import com.tom.cpm.shared.model.SkinType;
import com.tom.cpm.shared.util.PlayerModelLayer;

public class ViewportPanel extends ViewportPanelBase {
	protected Editor editor;

	public ViewportPanel(IGui gui, Editor editor) {
		super(gui);
		this.editor = editor;
	}

	@Override
	public void draw0(float partialTicks) {
		gui.drawBox(0, 0, bounds.w, bounds.h, 0xff333333);

		if(enabled) {
			nat.renderSetup();
			if(editor.renderBase)nat.renderBase();
			nat.render(partialTicks);
			nat.renderFinish();
		}
	}

	@Override
	public ViewportCamera getCamera() {
		return editor.camera;
	}

	@Override
	public void preRender() {
		editor.preRender();
	}

	@Override
	public SkinType getSkinType() {
		return editor.skinType == SkinType.UNKNOWN ? SkinType.DEFAULT : editor.skinType;
	}

	@Override
	public ModelDefinition getDefinition() {
		return editor.definition;
	}

	@Override
	public boolean isTpose() {
		return !editor.applyAnim && editor.playerTpose;
	}

	@Override
	public boolean applyLighting() {
		return true;
	}

	@Override
	public void applyRenderPoseForAnim(Consumer<VanillaPose> func) {
		editor.applyRenderPoseForAnim(func);
	}

	@Override
	public HeldItem getHeldItem(Hand hand) {
		return editor.handDisplay.getOrDefault(hand, HeldItem.NONE);
	}

	@Override
	public float getScale() {
		return editor.applyScaling ? editor.scaling : 1;
	}

	@Override
	public AnimationMode getAnimMode() {
		return AnimationMode.PLAYER;
	}

	@Override
	public Set<PlayerModelLayer> getArmorLayers() {
		ModelElement el = editor.getSelectedElement();
		if(el != null) {
			ModelElement root = el.getRoot();
			if(root != null && root.typeData instanceof RootModelType) {
				PlayerModelLayer l = PlayerModelLayer.getLayer((RootModelType) root.typeData);
				if(l != null) {
					Set<PlayerModelLayer> set = new HashSet<>(editor.modelDisplayLayers);
					set.add(l);
					return set;
				}
			}
		}
		return editor.modelDisplayLayers;
	}
}
