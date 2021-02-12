package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.util.List;

import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper;
import com.tom.cpm.shared.model.RenderedCube;

public class ModelPartTemplate implements IModelPart {
	private Link link;
	private int[] args;
	private ModelDefinitionLoader loader;
	public ModelPartTemplate(IOHelper in, ModelDefinitionLoader loader) throws IOException {
		link = new Link(in);
		/*int argC = in.read();
		args = new int[argC];
		for(int i = 0;i<argC;i++) {
			int ch2 = in.read();
			int ch3 = in.read();
			int ch4 = in.read();
			if ((ch2 | ch3 | ch4) < 0)
				throw new EOFException();
			args[i] = ((0xff << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
		}*/
		this.loader = loader;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		/*try(IOHelper is = new IOHelper(loader.load(link))) {
			int count = is.read();
			List<Cube> cubes = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				Cube c = Cube.loadTemplateCube(is, args);
				c.id = (byte) (i+10);
				cubes.add(c);
			}
			List<RenderedCube> rc = Cube.resolveCubes(cubes);
			return new ModelPartResolvedTemplate(rc);
		}*/
		return null;
	}

	public class ModelPartResolvedTemplate implements IResolvedModelPart {
		private List<RenderedCube> cubes;

		public ModelPartResolvedTemplate(List<RenderedCube> cubes) {
			this.cubes = cubes;
		}

		@Override
		public List<RenderedCube> getModel() {
			return cubes;
		}
	}

	@Override
	public void write(IOHelper dout) throws IOException {
		/*link.write(dout);
		dout.write(args.length);
		for (int i = 0; i < args.length; i++) {
			int v = args[i];
			dout.write((v >>> 16) & 0xFF);
			dout.write((v >>>  8) & 0xFF);
			dout.write((v >>>  0) & 0xFF);
		}*/
	}

	@Override
	public ModelPartType getType() {
		return ModelPartType.TEMPLATE;
	}
}
