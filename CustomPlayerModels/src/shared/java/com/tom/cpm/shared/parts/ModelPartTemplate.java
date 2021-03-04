package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.tom.cpm.shared.config.ResourceLoader.ResourceEncoding;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.template.Template;
import com.tom.cpm.shared.util.TextureStitcher;

public class ModelPartTemplate implements IModelPart {
	private Link link;
	private IOHelper args;
	private ModelDefinitionLoader loader;
	public ModelPartTemplate(IOHelper in, ModelDefinitionLoader loader) throws IOException {
		link = new Link(in);
		args = in.readNextBlock();
		this.loader = loader;
	}

	public ModelPartTemplate(Template template) throws IOException {
		this.link = template.getLink();
		this.args = template.writeArgs();
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		try (InputStreamReader rd = new InputStreamReader(loader.load(link, ResourceEncoding.NO_ENCODING))) {
			Template template = new Template(link, rd, null, args);
			return new ModelPartResolvedTemplate(template);
		}
	}

	public class ModelPartResolvedTemplate implements IResolvedModelPart {
		private Template template;

		public ModelPartResolvedTemplate(Template template) {
			this.template = template;
		}

		@Override
		public List<RenderedCube> getModel() {
			return template.getCubes();
		}

		@Override
		public void stitch(TextureStitcher stitcher) {
			if(!template.isTextureMerged()) {
				stitcher.stitchImage(template.getTexture(), template::onStitch);
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
