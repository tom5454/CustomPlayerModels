package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.io.InputStreamReader;

import com.tom.cpm.shared.config.ResourceLoader.ResourceEncoding;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.template.Template;
import com.tom.cpm.shared.util.TextureStitcher;

public class ModelPartTemplate implements IModelPart {
	private Link link;
	private IOHelper args;
	private ModelDefinition def;

	public ModelPartTemplate(IOHelper in, ModelDefinition def) throws IOException {
		link = new Link(in);
		args = in.readNextBlock();
		this.def = def;
	}

	public ModelPartTemplate(Template template) throws IOException {
		this.link = template.getLink();
		this.args = template.writeArgs();
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		try (InputStreamReader rd = new InputStreamReader(def.getLoader().load(link, ResourceEncoding.NO_ENCODING, def))) {
			Template template = new Template(link, rd, null, args, def);
			return new ModelPartResolvedTemplate(template);
		}
	}

	public class ModelPartResolvedTemplate implements IResolvedModelPart {
		private Template template;

		public ModelPartResolvedTemplate(Template template) {
			this.template = template;
		}

		@Override
		public void preApply(ModelDefinition def) {
			def.addCubes(template.getCubes());
		}

		@Override
		public void stitch(TextureStitcher stitcher) {
			if(!template.isTextureMerged()) {
				stitcher.stitchImage(template.getTemplateDefaultTexture(), template::onStitch);
			}
		}
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		link.write(dout);
		args.writeBlock(dout);
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.TEMPLATE;
	}
}
