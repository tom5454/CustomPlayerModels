package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.util.TextureStitcher;

public class ModelPartCollection extends ArrayList<IModelPart> implements PartCollection {
	private static final long serialVersionUID = 7943493602412147742L;

	public static class PackageLink extends ModelPartLink {

		public PackageLink(IOHelper in, ModelDefinition def) throws IOException {
			super(in, def);
		}

		private PackageLink(Link link) {
			super(link);
		}

		@Override
		public ModelPartType getType() {
			return ModelPartType.PACKAGE_LINK;
		}

		@Override
		protected IModelPart load(IOHelper din, ModelDefinition def) throws IOException {
			return new Pack(din, def);
		}

	}

	private static class Pack implements IModelPart, IResolvedModelPart {
		private List<IModelPart> parts;
		private List<IResolvedModelPart> resolvedParts;

		@SuppressWarnings("deprecation")
		private Pack(IOHelper is, ModelDefinition def) throws IOException {
			parts = new ArrayList<>();
			while (true) {
				IModelPart part = is.readObjectBlock(ModelPartType.VALUES, (t, d) -> t.getFactory().create(d, def));
				if(part == null)continue;
				if(part instanceof ModelPartEnd)break;
				switch (part.getType()) {
				case DEFINITION:
				case DEFINITION_LINK:
				case SKIN_LINK:
				case PACKAGE_LINK:
					throw new IOException("Invalid tag in package");

				case END: break;

				default:
					parts.add(part);
					break;
				}
			}
		}

		private Pack(List<IModelPart> parts) {
			this.parts = parts;
		}

		@Override
		public IResolvedModelPart resolve() throws IOException {
			resolvedParts = new ArrayList<>();
			for (IModelPart t : parts) {
				resolvedParts.add(t.resolve());
			}
			return this;
		}

		@Override
		public void write(IOHelper dout) throws IOException {
			throw new IOException("Can't write Pack");
		}

		@Override
		public ModelPartType getType() {
			return null;
		}

		@Override
		public void apply(ModelDefinition def) {
			resolvedParts.forEach(p -> p.apply(def));
		}

		@Override
		public void preApply(ModelDefinition def) {
			resolvedParts.forEach(p -> p.preApply(def));
		}

		@Override
		public void stitch(TextureStitcher stitcher) {
			resolvedParts.forEach(p -> p.stitch(stitcher));
		}

		@Override
		public String toString() {
			StringBuilder bb = new StringBuilder("PartCollection:");
			for (IModelPart iModelPart : parts) {
				bb.append("\n\t\t");
				bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
			}
			return bb.toString();
		}
	}

	@Override
	public void writeBlocks(IOHelper dout) throws IOException {
		for (IModelPart part : this) {
			dout.writeObjectBlock(part);
		}
	}

	@Override
	public String toString() {
		StringBuilder bb = new StringBuilder("PartCollection:");
		for (IModelPart iModelPart : this) {
			bb.append("\n\t\t");
			bb.append(iModelPart.toString().replace("\n", "\n\t\t"));
		}
		return bb.toString();
	}

	@Override
	public void writePackage(IOHelper dout) throws IOException {
		writeBlocks(dout);
		dout.writeObjectBlock(ModelPartEnd.END);
	}

	@Override
	public IModelPart toLink(Link link) {
		return new PackageLink(link);
	}
}
