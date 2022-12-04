package com.tom.cpm.shared.editor.project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ProjectFile implements IProject {
	private Entry root;

	public ProjectFile() {
		root = new Entry();
		root.children = new HashMap<>();
	}

	public boolean setEntry(String path, byte[] data) {
		Entry e = root;
		for(String nm : path.split("/")) {
			if(e.data != null)return false;
			if(e.children == null)e.children = new HashMap<>();
			e = e.children.computeIfAbsent(nm, k -> new Entry());
		}
		e.data = data;
		return true;
	}

	@Override
	public byte[] getEntry(String path) {
		Entry e = root;
		for(String nm : path.split("/")) {
			e = e.children.get(nm);
			if(e == null)return null;
		}
		return e.data;
	}

	@Override
	public List<String> listEntires(String path) {
		Entry e = root;
		if(!path.isEmpty()) {
			for(String nm : path.split("/")) {
				e = e.children.get(nm);
				if(e == null)return null;
			}
		}
		return e.children == null ? null : new ArrayList<>(e.children.keySet());
	}

	public void delete(String path) {
		Entry e = root;
		String[] t = path.split("/");
		for (int i = 0; i < t.length-1; i++) {
			e = e.children.get(t[i]);
			if(e == null)return;
		}
		if(e.children != null)
			e.children.remove(t[t.length - 1]);
	}


	public void clearFolder(String path) {
		Entry e = root;
		for(String nm : path.split("/")) {
			e = e.children.get(nm);
			if(e == null)return;
		}
		if(e.children != null)
			e.children.clear();
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

	public CompletableFuture<Void> load(File file) {
		try {
			root = new Entry();
			root.children = new HashMap<>();
			byte[] buffer = new byte[1024];
			try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
				ZipEntry ze = zis.getNextEntry();
				while(ze != null){
					String fileName = ze.getName();

					if(fileName.endsWith("/"))fileName = fileName.substring(0, fileName.length() - 1);

					Entry e = root;
					for(String nm : fileName.split("/")) {
						if(e.children == null)e.children = new HashMap<>();
						e = e.children.computeIfAbsent(nm, k -> new Entry());
					}

					if(ze.isDirectory()){
						e.children = new HashMap<>();
					}else{
						ByteArrayOutputStream fos = new ByteArrayOutputStream();
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						e.data = fos.toByteArray();
					}
					ze = zis.getNextEntry();
				}

				zis.closeEntry();
			}
		} catch (IOException e) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(e);
			return f;
		}
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> save(File file) {
		try {
			Deque<ZEntry> queue = new LinkedList<>();
			queue.push(new ZEntry(root, ""));
			ZEntry directory;
			try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file))) {
				while (!queue.isEmpty()) {
					directory = queue.pop();
					for (java.util.Map.Entry<String, Entry> e : directory.entry.children.entrySet()) {
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
		} catch (IOException e) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(e);
			return f;
		}
		return CompletableFuture.completedFuture(null);
	}

	private static class Entry {
		private Map<String, Entry> children;
		private byte[] data;

		@Override
		public String toString() {
			return data != null ? "Data" : children.toString();
		}
	}

	private static class ZEntry {
		private Entry entry;
		private String path;

		public ZEntry(Entry entry, String path) {
			this.entry = entry;
			this.path = path;
		}
	}
}
