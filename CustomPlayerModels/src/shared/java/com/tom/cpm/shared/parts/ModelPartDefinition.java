package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.util.TextureStitcher;

@Deprecated
public class ModelPartDefinition implements IModelPart, IResolvedModelPart, PartCollection {
	private List<Cube> cubes;
	private List<RenderedCube> rc;
	private List<IModelPart> otherParts;
	private List<IResolvedModelPart> resolvedOtherParts;

	public ModelPartDefinition(IOHelper is, ModelDefinition def) throws IOException {
		try {
			int count = is.readVarInt();
			cubes = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				Cube c = Cube.loadDefinitionCube(is);
				c.id = i+10;
				cubes.add(c);
			}
		} catch (IOException e) {
			is.reset();
			int count = is.read();
			cubes = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				Cube c = Cube.loadDefinitionCube(is);
				c.id = i+10;
				cubes.add(c);
			}
		}
		rc = Cube.resolveCubes(cubes);
		otherParts = new ArrayList<>();
		while(true) {
			IModelPart part = is.readObjectBlock(ModelPartType.VALUES, (t, d) -> t.getFactory().create(d, def));
			if(part == null)continue;
			if(part instanceof ModelPartEnd)break;
			switch (part.getType()) {
			case DEFINITION:
			case DEFINITION_LINK:
			case SKIN_LINK:
				throw new IOException("Invalid tag in definition");

			case END: break;

			default:
				otherParts.add(part);
				break;
			}
		}
	}

	public ModelPartDefinition(List<Cube> cubes) {
		this.cubes = cubes;
		this.otherParts = new ArrayList<>();
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		resolvedOtherParts = new ArrayList<>();
		for (IModelPart t : otherParts) {
			resolvedOtherParts.add(t.resolve());
		}
		return this;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.writeVarInt(cubes.size());
		List<Cube> lst = new ArrayList<>(cubes);
		lst.sort((a, b) -> Integer.compare(a.id, b.id));
		for (Cube cube : lst) {
			Cube.saveDefinitionCube(dout, cube);
		}
		for (IModelPart part : otherParts) {
			dout.writeObjectBlock(part);
		}
		dout.writeObjectBlock(ModelPartEnd.END);
	}

	@Override
	public void preApply(ModelDefinition def) {
		def.addCubes(rc);
		resolvedOtherParts.forEach(p -> p.preApply(def));
	}

	@Override
	public void apply(ModelDefinition def) {
		resolvedOtherParts.forEach(p -> p.apply(def));
	}

	@Override
	public void stitch(TextureStitcher stitcher) {
		resolvedOtherParts.forEach(p -> p.stitch(stitcher));
	}

	public void setOtherParts(List<IModelPart> otherParts) {
		this.otherParts = otherParts;
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.DEFINITION;
	}

	@Override
	public String toString() {
		StringBuilder bb = new StringBuilder("PartDefinition\n\tCubes: ");
		bb.append(cubes.size());
		bb.append("\n\tParts:");
		for (IModelPart iModelPart : otherParts) {
			bb.append("\n\t\t");
			bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
		}
		return bb.toString();
	}

	@Override
	public void writePackage(IOHelper dout) throws IOException {
		write(dout);
	}

	@Override
	public IModelPart toLink(Link link) {
		return new ModelPartDefinitionLink(link);
	}

	@Override
	public void writeBlocks(IOHelper dout) throws IOException {
		dout.writeObjectBlock(this);
	}
}
