package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.Cube;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.skin.SkinProvider;

public class ModelPartDefinition implements IModelPart, IResolvedModelPart {
	private List<Cube> cubes;
	private List<RenderedCube> rc;
	private List<ModelPartTemplate> templates;
	private List<IModelPart> otherParts;
	private List<IResolvedModelPart> resolvedOtherParts;
	private List<IResolvedModelPart> resolvedTemplates;
	private IModelPart skin;
	private ModelPartPlayer player;
	private IResolvedModelPart skinImage;

	public ModelPartDefinition(IOHelper is, ModelDefinitionLoader loader) throws IOException {
		int count = is.read();
		cubes = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Cube c = Cube.loadDefinitionCube(is);
			c.id = i+10;
			cubes.add(c);
		}
		rc = Cube.resolveCubes(cubes);
		templates = new ArrayList<>();
		otherParts = new ArrayList<>();
		while(true) {
			IModelPart part = is.readObjectBlock(ModelPartType.VALUES, (t, d) -> t.getFactory().create(d, loader));
			if(part == null)continue;
			if(part instanceof ModelPartEnd)break;
			switch (part.getType()) {
			case TEMPLATE://Template
				templates.add((ModelPartTemplate) part);
				break;

			case SKIN://Skin
				if(skin != null)throw new IOException("Multipile skin tags");
				skin = part;
				break;

			case DEFINITION:
			case DEFINITION_LINK:
			case SKIN_LINK:
				throw new IOException("Invalid tag in definition");

			case PLAYER://Player
				if(player != null)throw new IOException("Multipile player tags");
				player = (ModelPartPlayer) part;
				break;

			case END: break;

			default:
				otherParts.add(part);
				break;
			}
		}
	}

	public ModelPartDefinition(ModelPartSkin skin, List<Cube> cubes) {
		this.cubes = cubes;
		this.skin = skin;
		this.otherParts = new ArrayList<>();
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		resolvedTemplates = new ArrayList<>();
		for (ModelPartTemplate t : templates) {
			IResolvedModelPart part = t.resolve();
			resolvedTemplates.add(part);
			rc.addAll(part.getModel());
		}
		if(skin != null)skinImage = skin.resolve();
		resolvedOtherParts = new ArrayList<>();
		for (IModelPart t : otherParts) {
			resolvedOtherParts.add(t.resolve());
		}
		return this;
	}

	@Override
	public List<RenderedCube> getModel() {
		return rc;
	}

	@Override
	public SkinProvider getSkin() {
		return skinImage != null ? skinImage.getSkin() : null;
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		dout.write(cubes.size());
		List<Cube> lst = new ArrayList<>(cubes);
		lst.sort((a, b) -> Integer.compare(a.id, b.id));
		for (Cube cube : lst) {
			Cube.saveDefinitionCube(dout, cube);
		}
		if(player != null) {
			dout.writeObjectBlock(player);
		}
		if(skin != null) {
			dout.writeObjectBlock(skin);
		}
		for (IModelPart part : otherParts) {
			dout.writeObjectBlock(part);
		}
		dout.writeObjectBlock(ModelPartEnd.END);
	}

	@Override
	public void apply(ModelDefinition def) {
		resolvedOtherParts.forEach(p -> p.apply(def));
	}

	public void setPlayer(ModelPartPlayer player) {
		this.player = player;
	}

	public void setOtherParts(List<IModelPart> otherParts) {
		this.otherParts = otherParts;
	}

	public ModelPartPlayer getPlayer() {
		return player;
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.DEFINITION;
	}

	@Override
	public String toString() {
		StringBuilder bb = new StringBuilder("PartDefinition\n\tCubes: ");
		bb.append(cubes.size());
		bb.append("\n\tSkin:\n\t\t");
		bb.append(String.valueOf(skin).replace("\n", "\n\t\t"));
		bb.append("\n\tPlayer:\n\t\t");
		bb.append(String.valueOf(player).replace("\n", "\n\t\t"));
		bb.append("\n\tOther:");
		for (IModelPart iModelPart : otherParts) {
			bb.append("\n\t\t");
			bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
		}
		return bb.toString();
	}
}
