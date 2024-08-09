package com.tom.cpm.shared.parts.anim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.AnimationExporter;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.util.PlayerSkinLayer;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;

public class AnimLoaderState {
	private ModelDefinition def;
	private ParameterDetails parameters = ParameterDetails.DEFAULT;
	private List<AbstractGestureButtonData> gestureButtons = new ArrayList<>();
	private Map<Integer, SerializedAnimation> anims = new HashMap<>();
	private Map<Integer, SerializedTrigger> triggers = new HashMap<>();
	private int trID = 0;
	private int anID = 0;
	private SerializedAnimation cA = null;
	private SerializedTrigger cT = null;
	private int blankId, resetId;
	private String modelProfilesId;
	private int defGidMask, valGidMask;
	private List<PlayerSkinLayer> allLayers;
	private List<Integer> stagedList = new ArrayList<>();

	public AnimLoaderState(ModelDefinition def) {
		this.def = def;
	}

	public ModelDefinition getDefinition() {
		return def;
	}

	public SerializedTrigger getTrigger() {
		return cT;
	}

	public SerializedAnimation getAnim() {
		return cA;
	}

	public int newTrigger(SerializedTrigger tr) {
		int id = trID++;
		triggers.put(id, tr);
		cT = tr;
		return id;
	}

	public int newAnimation(SerializedAnimation an) {
		int id = anID++;
		anims.put(id, an);
		cA = an;
		return id;
	}

	public Map<Integer, SerializedAnimation> getAnims() {
		return anims;
	}

	public Map<Integer, SerializedTrigger> getTriggers() {
		return triggers;
	}

	private void loadInfo(Editor e) {
		valGidMask = e.animEnc == null ? 0 :  PlayerSkinLayer.encode(e.animEnc.freeLayers);
		defGidMask = e.animEnc == null ? 0 : (PlayerSkinLayer.encode(e.animEnc.defaultLayerValue) & (~valGidMask));
		resetId = defGidMask | valGidMask;
		blankId = defGidMask | 0;
		modelProfilesId = e.modelId;
		allLayers = e.animEnc != null ? new ArrayList<>(e.animEnc.freeLayers) : new ArrayList<>();
		Collections.sort(allLayers);
	}

	public void loadFromEditor(Editor e) {
		loadInfo(e);
		AnimationExporter exporter = new AnimationExporter(e, this);
		exporter.processElements();
		e.animations.forEach(exporter::processAnimation);
		parameters = exporter.paramAlloc.finish();
		gestureButtons = exporter.sortButtons();
		exporter.linkStagingAnims();
		LayerEncondingHelper.handleLayers(this.triggers, allLayers, gestureButtons, blankId, resetId, valGidMask, defGidMask);
	}

	public static void parseInfo(IOHelper block, AnimLoaderState state) throws IOException {
		state.blankId = block.read();
		state.resetId = block.read();
		state.modelProfilesId = block.readUTF();
	}

	public void writeInfo(IOHelper dout) throws IOException {
		try (IOHelper d = dout.writeNextObjectBlock(TagType.CONTROL_INFO)) {
			d.write(blankId);
			d.write(resetId);
			if (modelProfilesId == null)d.writeVarInt(0);
			else d.writeUTF(modelProfilesId);
		}
		if (!parameters.equals(ParameterDetails.DEFAULT)) {
			try (IOHelper d = dout.writeNextObjectBlock(TagType.PARAMETERS)) {
				parameters.write(d);
			}
		}
	}

	public void applyInfos(AnimationRegistry reg) {
		reg.setBlankGesture(blankId);
		reg.setPoseResetId(resetId);
		reg.setProfileId(modelProfilesId);
		reg.setParams(parameters);
	}

	public void processTriggers() {
		//SerializedTrigger.handleGestures(triggers, blankId, resetId);
	}

	public void setParameters(ParameterDetails parameters) {
		this.parameters = parameters;
	}

	public ParameterDetails getParameters() {
		return parameters;
	}

	public void addGestureButton(AbstractGestureButtonData button) {
		gestureButtons.add(button);
	}

	public List<AbstractGestureButtonData> getGestureButtons() {
		return gestureButtons;
	}

	public List<Integer> getStagedList() {
		return stagedList;
	}
}
