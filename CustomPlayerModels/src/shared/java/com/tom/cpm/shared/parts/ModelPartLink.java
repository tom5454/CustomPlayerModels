package com.tom.cpm.shared.parts;

import java.io.IOException;
import java.io.InputStream;

import com.tom.cpm.shared.config.ResourceLoader.ResourceEncoding;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.ChecksumInputStream;
import com.tom.cpm.shared.io.IOHelper;

public abstract class ModelPartLink implements IModelPart {
	private Link link;
	private ModelDefinition def;

	public ModelPartLink(IOHelper in, ModelDefinition def) throws IOException {
		link = new Link(in);
		this.def = def;
	}

	public ModelPartLink(Link link) {
		this.link = link;
	}

	@Override
	public IResolvedModelPart resolve() throws IOException {
		try(InputStream in = def.getLoader().load(link, ResourceEncoding.BASE64, def)) {
			if(in.read() != ModelDefinitionLoader.HEADER)throw new IOException();
			ChecksumInputStream cis = new ChecksumInputStream(in);
			IModelPart part = load(new IOHelper(cis), def);
			cis.checkSum();
			return part.resolve();
		}
	}

	protected abstract IModelPart load(IOHelper din, ModelDefinition def) throws IOException;

	@Override
	public void write(IOHelper dout) throws IOException {
		link.write(dout);
	}

	@Override
	public String toString() {
		StringBuilder bb = new StringBuilder("Link\n\tPath: ");
		bb.append(link);
		return bb.toString();
	}

	public Link getLink() {
		return link;
	}
}
