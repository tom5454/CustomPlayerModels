package com.tom.cpm.shared.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.Link;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.IOHelper.ImageBlock;

public class ModelFile {
	private String name, desc, fname;
	private Link link;
	private byte[] dataBlock, overflowLocal;
	private ImageBlock icon;

	private ModelFile() {
	}

	public static ModelFile load(File in) throws IOException {
		try (FileInputStream fin = new FileInputStream(in)) {
			return load(in.getName(), fin);
		}
	}

	public static ModelFile load(InputStream fin) throws IOException {
		return load("unnamed", fin);
	}

	public static ModelFile load(String name, InputStream fin) throws IOException {
		if(fin.read() != ModelDefinitionLoader.HEADER)throw new IOException("Magic number mismatch");
		ChecksumInputStream cis = new ChecksumInputStream(fin);
		IOHelper h = new IOHelper(cis);
		ModelFile mf = new ModelFile();
		mf.fname = name;
		mf.name = h.readUTF();
		mf.desc = h.readUTF();
		mf.dataBlock = h.readByteArray();
		byte[] ovf = h.readByteArray();
		if(ovf.length != 0) {
			mf.overflowLocal = ovf;
			mf.link = new Link(h);
		}
		if (MinecraftClientAccess.get() != null) {
			ImageBlock block = h.readImage();
			if (block.getWidth() > 256 || block.getHeight() > 256)
				throw new IOException("Texture size too large");
			mf.icon = block;
		} else {
			h.readNextBlock();
		}
		cis.checkSum();
		return mf;
	}

	public byte[] getDataBlock() {
		return dataBlock;
	}

	public ImageBlock getIcon() {
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

	public boolean convertable() {
		return dataBlock.length <= 2048;
	}
}
