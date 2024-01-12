package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.Arrays;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.render.VanillaModelPart;

@Deprecated
public class ModelPartPlayer implements IModelPart, IResolvedModelPart {
	private boolean[] keep = new boolean[8];
	public ModelPartPlayer(IOHelper in, ModelDefinition def) throws IOException {
		int keep = in.read();
		for (int i = 0; i < this.keep.length; i++) {
			this.keep[i] = (keep & (1 << i)) != 0;
		}
	}

	public ModelPartPlayer() {
		Arrays.fill(keep, true);
	}

	public ModelPartPlayer(Editor editor) {
		editor.elements.forEach(e -> {
			if(e.rc instanceof RootModelElement && !e.duplicated) {
				VanillaModelPart part = ((RootModelElement)e.rc).getPart();
				if(part != null && part instanceof PlayerModelParts) {
					keep[((PlayerModelParts)part).ordinal()] = !e.hidden;
				}
			}
		});
	}

	@Override
	public void preApply(ModelDefinition def) {
		for (int i = 0;i<PlayerModelParts.VALUES.length;i++) {
			RootModelElement elem = def.getModelElementFor(PlayerModelParts.VALUES[i]).getMainRoot();
			elem.setHidden(!keep[i]);
		}
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		int v = 0;
		for (int i = 0; i < this.keep.length; i++) {
			if(keep[i])v |= (1 << i);
		}
		dout.write(v);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.PLAYER;
	}
}
