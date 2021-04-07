package com.tom.cpm.shared.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.tom.cpl.util.Image;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper.ImageBlock;

public class ModelFile {
	private String name, desc, fname;
	private Link link;
	private byte[] dataBlock, overflowLocal;
	private Image icon;

	public static ModelFile load(File in) throws IOException {
		try (FileInputStream fin = new FileInputStream(in)) {
			if(fin.read() != ModelDefinitionLoader.HEADER)throw new IOException("Magic number mismatch");
			ChecksumInputStream cis = new ChecksumInputStream(fin);
			IOHelper h = new IOHelper(cis);
			ModelFile mf = new ModelFile();
			mf.fname = in.getName();
			mf.name = h.readUTF();
			mf.desc = h.readUTF();
			mf.dataBlock = h.readByteArray();
			byte[] ovf = h.readByteArray();
			if(ovf.length != 0) {
				mf.overflowLocal = ovf;
				mf.link = new Link(h);
			}
			ImageBlock block = h.readImage();
			if(block.getWidth() > 256 || block.getHeight() > 256)
				throw new IOException("Texture size too large");
			block.doReadImage();
			mf.icon = block.getImage();
			cis.checkSum();
			return mf;
		}
	}

	public byte[] getDataBlock() {
		return dataBlock;
	}

	public Image getIcon() {
		return icon;
	}

	public void registerLocalCache(ModelDefinitionLoader loader) {
		if(overflowLocal != null) {
			loader.putLocalResource(link, overflowLocal);
		}
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public String getFileName() {
		return fname;
	}
}
