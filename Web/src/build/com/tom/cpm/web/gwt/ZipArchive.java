package com.tom.cpm.web.gwt;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchive {
	private ZipArchive.Entry root;

	public ZipArchive() {
		root = new Entry();
		root.children = new HashMap<>();
	}

	public boolean setEntry(String path, byte[] data) {
		ZipArchive.Entry e = root;
		for(String nm : path.split("/")) {
			if(e.data != null)return false;
			if(e.children == null)e.children = new HashMap<>();
			e = e.children.computeIfAbsent(nm, k -> new Entry());
		}
		e.data = data;
		return true;
	}

	public byte[] getEntry(String path) {
		ZipArchive.Entry e = root;
		for(String nm : path.split("/")) {
			e = e.children.get(nm);
			if(e == null)return null;
		}
		return e.data;
	}

	public OutputStream setAsStream(String path) throws IOException {
		if(!setEntry(path, new byte[0]))
			throw new FileNotFoundException();
		return new ByteArrayOutputStream() {

			@Override
			public void close() throws IOException {
				if(!setEntry(path, toByteArray()))
					throw new FileNotFoundException();
			}
		};
	}

	public void save(OutputStream os) throws IOException {
		Deque<ZipArchive.ZEntry> queue = new LinkedList<>();
		queue.push(new ZEntry(root, ""));
		ZipArchive.ZEntry directory;
		try (ZipOutputStream zout = new ZipOutputStream(os)) {
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (java.util.Map.Entry<String, ZipArchive.Entry> e : directory.entry.children.entrySet()) {
					String path = directory.path + "/" + e.getKey();
					if(e.getValue().children != null) {
						queue.push(new ZEntry(e.getValue(), path));
						zout.putNextEntry(new ZipEntry(path.substring(1) + "/"));
					} else {
						zout.putNextEntry(new ZipEntry(path.substring(1)));
						zout.write(e.getValue().data);
						zout.closeEntry();
					}
				}
			}
			zout.closeEntry();
		}
	}

	private static class Entry {
		private Map<String, ZipArchive.Entry> children;
		private byte[] data;

		@Override
		public String toString() {
			return data != null ? "Data" : children.toString();
		}
	}

	private static class ZEntry {
		private ZipArchive.Entry entry;
		private String path;

		public ZEntry(ZipArchive.Entry entry, String path) {
			this.entry = entry;
			this.path = path;
		}
	}
}