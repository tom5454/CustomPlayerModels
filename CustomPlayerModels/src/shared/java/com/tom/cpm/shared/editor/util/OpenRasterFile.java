package com.tom.cpm.shared.editor.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.tom.cpl.util.Image;
import com.tom.cpl.util.ImageIO;
import com.tom.cpm.shared.editor.project.ProjectFile;

public class OpenRasterFile {
	private Stack root = new Stack(null, 0);
	private int width, height;

	public OpenRasterFile(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void addLayer(int ordering, Image img, int x, int y, String layerName) {
		root.addLayer(ordering, img, x, y, layerName);
	}

	public Stack addStack(String name, int ordering) {
		return root.addStack(name, ordering);
	}

	public CompletableFuture<Void> write(File file) throws IOException {
		ProjectFile pf = new ProjectFile();
		pf.setEntry("mimetype", "image/openraster".getBytes());
		root.sort();

		Image merged = new Image(width, height);
		List<Layer> layers = new ArrayList<>();
		root.forEachLayer(layers::add);

		for (int i = layers.size() - 1; i >= 0; i--) {
			Layer l = layers.get(i);
			merged.draw(l.img, l.x, l.y);
		}

		try (OutputStream os = pf.setAsStream("mergedimage.png")) {
			ImageIO.write(merged, os);
		}

		float aspectRatio = (float) width / height;
		int newWidth, newHeight;
		if (width > height) {
			newWidth = 256;
			newHeight = (int) (newWidth / aspectRatio);
		} else {
			newHeight = 256;
			newWidth = (int) (newHeight * aspectRatio);
		}

		Image icon = new Image(newWidth, newHeight);
		icon.draw(merged, 0, 0, newWidth, newHeight);

		for (Layer layer : layers) {
			try (OutputStream os = pf.setAsStream(layer.fileName)) {
				ImageIO.write(layer.img, os);
			}
		}

		try (OutputStream os = pf.setAsStream("Thumbnails/thumbnail.png")) {
			ImageIO.write(icon, os);
		}

		try (PrintWriter wr = new PrintWriter(new OutputStreamWriter(pf.setAsStream("stack.xml"), StandardCharsets.UTF_8))) {
			wr.println("<?xml version='1.0' encoding='UTF-8'?>");
			wr.println("<image version=\"0.0.3\" w=\"" + width + "\" h=\"" + height + "\" xres=\"600\" yres=\"600\">");
			wr.println("<stack>");
			root.printConfig(wr);
			wr.println("</stack>");
			wr.println("</image>");
		}

		return pf.save(file);
	}

	private static interface ImageElement extends Comparable<ImageElement> {
		int getOrdering();

		@Override
		public default int compareTo(ImageElement o) {
			return -Integer.compare(getOrdering(), o.getOrdering());
		}
	}

	private static class Layer implements ImageElement {
		private int ordering;
		private Image img;
		private int x, y;
		private String name;
		private String fileName;

		public Layer(int ordering, Image img, int x, int y, String name) {
			this.ordering = ordering;
			this.img = img;
			this.x = x;
			this.y = y;
			this.name = name;
			fileName = "data/" + UUID.randomUUID().toString().replace("-", "") + ".png";
		}

		@Override
		public int getOrdering() {
			return ordering;
		}
	}

	public static class Stack implements ImageElement {
		private String name;
		private List<ImageElement> elems = new ArrayList<>();
		private int ordering;

		public Stack(String name, int ordering) {
			this.name = name;
			this.ordering = ordering;
		}

		public void printConfig(PrintWriter wr) {
			for (ImageElement i : elems) {
				if (i instanceof Layer) {
					Layer layer = (Layer) i;
					wr.println("<layer name=\"" + layer.name + "\" src=\"" + layer.fileName + "\" x=\"" + layer.x +  "\" y=\"" + layer.y + "\" />");
				} else if (i instanceof Stack) {
					Stack s = (Stack) i;
					wr.println("<stack name=\"" + s.name + "\">");
					s.printConfig(wr);
					wr.println("</stack>");
				}
			}
		}

		public void forEachLayer(Consumer<Layer> c) {
			for (ImageElement i : elems) {
				if (i instanceof Layer)c.accept((Layer) i);
				else if (i instanceof Stack)
					((Stack)i).forEachLayer(c);
			}
		}

		public void sort() {
			Collections.sort(elems);
			for (ImageElement i : elems) {
				if (i instanceof Stack)
					((Stack)i).sort();
			}
		}

		@Override
		public int getOrdering() {
			return ordering;
		}

		public void addLayer(int ordering, Image img, int x, int y, String layerName) {
			elems.add(new Layer(ordering, img, x, y, layerName));
		}

		public Stack addStack(String name, int ordering) {
			Stack s = new Stack(name, ordering);
			elems.add(s);
			return s;
		}
	}
}
