package com.tom.cpm.web.gwt;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ClassSrcTransformer {
	public static final List<Consumer<ClassFile>> transformers = new ArrayList<>();
	public static final List<String> buggyFiles = new ArrayList<>();

	public static void addImportTransform(String clazz, String to) {
		transformers.add(c -> {
			if(c.imports.remove(clazz))c.imports.add(to);
		});
	}

	public static void addImportTransformRegex(String clazz, String to) {
		transformers.add(c -> c.imports.replaceAll(i -> i.replaceAll(clazz, to)));
	}

	public static byte[] transform(String name, InputStream is) throws IOException {
		ClassFile cf;
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
			cf = new ClassFile(name, rd);
		}

		transformers.forEach(t -> t.accept(cf));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		File out = new File("dump", name);
		out.getParentFile().mkdirs();
		try (PrintWriter wr = new PrintWriter(new DualOutputStream(baos, new FileOutputStream(out)))){
			cf.print(wr);
		}
		return baos.toByteArray();
	}

	public static class ClassFile {
		public String pkg;
		public String name;
		public List<String> imports;
		public List<String> classBody;
		private int classStart;

		public ClassFile(String name, BufferedReader rd) throws IOException {
			StringBuilder sb = new StringBuilder();
			rd.lines().forEach(l -> {
				sb.append(l);
				sb.append('\n');
			});
			String[] lines = sb.toString().split("\\\n");//.replaceAll("((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|\\/\\/[^\\n]*|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/", "")
			imports = new ArrayList<>();
			classBody = new ArrayList<>();
			this.name = name;
			boolean inBody = false;
			for (String string : lines) {
				if(inBody) {
					classBody.add(string);
					continue;
				}
				classStart++;
				if(string.startsWith("package")) {
					String[] sp = string.split(" ");
					pkg = sp[1].substring(0, sp[1].length() - 1);
				} else if(string.startsWith("import")) {
					String[] sp = string.split(" ", 2);
					String i = sp[1].substring(0, sp[1].length() - 1);
					imports.add(i);
				} else if(pkg != null) {
					if(!string.trim().isEmpty()) {
						inBody = true;
						classBody.add(string);
					}
				}
			}
		}

		public void replace(ClassFile c) {
			pkg = c.pkg;
			name = c.name;
			imports = c.imports;
			classBody = c.classBody;
		}

		public void print(PrintWriter w) {
			w.print("package ");
			w.print(pkg);
			w.println(';');
			w.println();
			imports.forEach(i -> {
				w.print("import ");
				w.print(i);
				w.print("; ");
			});
			w.println();
			for(int i = 4;i<classStart;i++)
				w.println();

			classBody.forEach(w::println);
		}

		public void regexTransformBody(String exp, String to) {
			classBody.replaceAll(s -> s.replaceAll(exp, to));
		}

		public void regexTransformBody(String exp, Supplier<String> to) {
			classBody.replaceAll(s -> s.replaceAll(exp, to.get()));
		}

		public void replaceBody(UnaryOperator<String> func) {
			String newBody = func.apply(classBody.stream().collect(Collectors.joining("\n")));
			classBody = new ArrayList<>(Arrays.asList(newBody.split("\\\n")));
		}
	}
}
