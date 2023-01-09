package com.tom.cpm.shared.editor.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.tom.cpm.web.client.FS;
import com.tom.cpm.web.client.util.ImageIO;
import com.tom.cpm.web.client.util.JSZip;
import com.tom.cpm.web.client.util.JSZip.ZipEntry;
import com.tom.cpm.web.client.util.JSZip.ZipFileProperties;
import com.tom.cpm.web.client.util.JSZip.ZipWriteProperties;

import elemental2.core.JsObject;
import elemental2.dom.Blob;
import elemental2.dom.FileReader;
import elemental2.promise.Promise;
import jsinterop.base.Js;

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
		for(String nm : path.split("/")) {
			e = e.children.get(nm);
			if(e == null)return null;
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
		root = new Entry();
		root.children = new HashMap<>();
		CompletableFuture<Void> cf = new CompletableFuture<>();
		FS.getContentFuture(file.getAbsolutePath()).
		then(b -> b.arrayBuffer()).
		then(b -> new JSZip().loadAsync(b)).
		then(this::load).then(v -> {
			cf.complete(null);
			return null;
		}).catch_(ex -> {
			if(ex instanceof Throwable)cf.completeExceptionally((Throwable) ex);
			else cf.completeExceptionally(new IOException(String.valueOf(ex)));
			return null;
		});
		return cf;
	}

	public Promise<Object[]> load(JSZip zip) {
		List<Promise<Object>> resolved = new ArrayList<>();
		for(String f : JsObject.keys(zip.files).asList()) {
			ZipEntry e = zip.file(f);
			if(e != null) {
				resolved.add(e.async("base64").then(dt -> {
					setEntry(f, Base64.getDecoder().decode(dt));
					if(f.endsWith(".png")) {
						return Js.cast(ImageIO.loadImage(dt, true, true));
					}
					return null;
				}));
			}
		}
		return Promise.all(Js.cast(resolved.toArray(new Promise[0])));
	}

	public CompletableFuture<Void> save(File file) {
		CompletableFuture<Void> cf = new CompletableFuture<>();
		save().then(blob -> {
			FileReader reader = new FileReader();
			reader.readAsDataURL(blob);
			reader.onloadend = e -> {
				String base64data = reader.result.asString();
				String dt = base64data.substring(base64data.indexOf(',') + 1);
				if(!FS.setContent(file.getAbsolutePath(), dt))cf.completeExceptionally(new IOException("Failed to write file"));
				else cf.complete(null);
				return null;
			};
			return null;
		});
		return cf;
	}

	public Promise<Blob> save() {
		JSZip zip = new JSZip();
		Deque<ZEntry> queue = new LinkedList<>();
		queue.push(new ZEntry(root, zip));
		ZEntry directory;
		while (!queue.isEmpty()) {
			directory = queue.pop();
			for (java.util.Map.Entry<String, Entry> e : directory.entry.children.entrySet()) {
				if(e.getValue().children != null) {
					queue.push(new ZEntry(e.getValue(), directory.zip.folder(e.getKey())));
				} else {
					directory.zip.file(e.getKey(), Base64.getEncoder().encodeToString(e.getValue().data), ZipFileProperties.make());
				}
			}
		}
		return zip.generateAsync(ZipWriteProperties.make());
	}

	@Override
	public InputStream getAsStream(String path) throws IOException {
		byte[] dt = getEntry(path);
		if(dt == null)throw new FileNotFoundException();
		return new BAIS(dt);
	}

	public static class BAIS extends ByteArrayInputStream {

		public BAIS(byte[] buf) {
			super(buf);
		}

		public byte[] getBuf() {
			return buf;
		}
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
		private JSZip zip;

		public ZEntry(Entry entry, JSZip zip) {
			this.entry = entry;
			this.zip = zip;
		}
	}
}
