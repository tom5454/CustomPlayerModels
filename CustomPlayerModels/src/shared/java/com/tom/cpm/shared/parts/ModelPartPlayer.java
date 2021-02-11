package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.Arrays;

import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.ModelRenderManager.ModelPart;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RootModelElement;

public class ModelPartPlayer implements IModelPart, IResolvedModelPart {
	private boolean[] keep = new boolean[8];
	public ModelPartPlayer(IOHelper in, ModelDefinitionLoader loader) throws IOException {
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
			if(e.rc instanceof RootModelElement) {
				ModelPart part = ((RootModelElement)e.rc).getPart();
				if(part != null && part instanceof PlayerModelParts) {
					keep[((PlayerModelParts)part).ordinal()] = e.show;
				}
			}
		});
	}

	public boolean doRenderPart(PlayerModelParts part) {
		return keep[part.ordinal()];
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
