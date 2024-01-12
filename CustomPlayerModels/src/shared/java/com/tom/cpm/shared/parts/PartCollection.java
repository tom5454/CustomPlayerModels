package com.tom.cpm.shared.parts;

import java.io.IOException;

import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.io.IOHelper;

public interface PartCollection {
	void writeBlocks(IOHelper dout) throws IOException;
	void writePackage(IOHelper dout) throws IOException;
	IModelPart toLink(Link link);
}
