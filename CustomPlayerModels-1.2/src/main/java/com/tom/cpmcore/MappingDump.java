package com.tom.cpmcore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class MappingDump {
	private Map<String, ClassNode> classNodes = new HashMap<>();
	private Set<String> clzs = new HashSet<>(), fields = new HashSet<>(), methods = new HashSet<>();
	private PrintWriter w;
	private boolean applyTransform;

	public void run(File zip, File dest, boolean verify) throws IOException {
		byte[] buffer = new byte[1024];
		applyTransform = verify;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));PrintWriter w = new PrintWriter(dest)) {
			this.w = w;
			ZipEntry ze = zis.getNextEntry();
			while(ze != null){
				String name = ze.getName();

				if (!ze.isDirectory() && name.startsWith("com/tom/cpm") && !name.startsWith("com/tom/cpm/shared") && !name.startsWith("com/tom/cpm/externals") && !name.startsWith("com.tom.cpl") && !name.startsWith("com/tom/cpm/api") && name.endsWith(".class")) {

					ByteArrayOutputStream fos = new ByteArrayOutputStream();
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					System.out.println("Class: " + name);

					if (verify) {
						if (name.startsWith("com/tom/cpm/client"))continue;
						w.println("Class: " + name);
						byte[] dt = fos.toByteArray();
						try {
							dt = CPMTransformerService.transform(name.substring(0, name.length() - 6).replace('/', '.'), dt);
						} catch (Throwable e) {
							w.println("Class: " + name);
							e.printStackTrace(w);
							w.println();
						}

						ClassNode classNode = new ClassNode();
						ClassReader classReader = new ClassReader(dt);
						classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

						classNodes.put(name.substring(0, name.length() - 6), classNode);

						ClassNode cn = new ClassNode();
						classNode.accept(new RemappingClassAdapter(cn, new Finder()));

						w.println();
					} else {
						ClassNode classNode = new ClassNode();
						ClassReader classReader = new ClassReader(fos.toByteArray());
						classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

						ClassNode cn = new ClassNode();
						classNode.accept(new RemappingClassAdapter(cn, new Dumper(classNode)));
					}
				}

				ze = zis.getNextEntry();
			}

			zis.closeEntry();
		}
	}

	public class Finder extends Remapper {

		public Finder() {
		}

		@Override
		public String map(String var1) {
			if (!var1.startsWith("java") && getClassNode(var1) == null)
				w.println("Class not found: " + var1);
			return var1;
		}

		@SuppressWarnings("unchecked")
		@Override
		public String mapFieldName(String owner, String name, String desc) {
			if (owner.startsWith("java"))return name;
			ClassNode sup = getClassNode(owner);
			boolean found = false;
			while (sup != null) {
				for (FieldNode f : (List<FieldNode>) sup.fields) {
					if (f.name.equals(name)) {
						if (!f.desc.equals(desc))
							w.println("Found field with desc mismatch: " + sup.name + "." + f.name + " " + f.desc + " expected: " + owner + "." + name + " " + desc);
						found = true;
					}
				}
				sup = getClassNode(sup.superName);
			}
			if(!found)w.println("Field not found: " + owner + "." + name);
			return name;
		}

		@SuppressWarnings("unchecked")
		@Override
		public String mapMethodName(String owner, String name, String desc) {
			if (owner.startsWith("java"))return name;
			ClassNode sup = getClassNode(owner);
			boolean found = false;
			while (sup != null) {
				for (MethodNode f : (List<MethodNode>) sup.methods) {
					if (f.name.equals(name) && f.desc.equals(desc)) {
						found = true;
					}
				}
				sup = getClassNode(sup.superName);
			}
			if(!found)w.println("Method not found: " + owner + "." + name + desc);
			return name;
		}
	}

	public class Dumper extends Remapper {
		private ClassNode classNode;
		private Set<String> fs = new HashSet<>();
		private Set<String> ms = new HashSet<>();
		private boolean sm;

		@SuppressWarnings("unchecked")
		public Dumper(ClassNode classNode) {
			this.classNode = classNode;

			for (FieldNode f : (List<FieldNode>) classNode.fields) {
				fs.add(f.name);
			}

			for (MethodNode f : (List<MethodNode>) classNode.methods) {
				ms.add(f.name);
			}

			sm = classNode.superName.startsWith("net/minecraft");
		}

		@Override
		public String map(String var1) {
			mapObj(var1);
			return var1;
		}

		@Override
		public String mapFieldName(String owner, String name, String desc) {
			if (owner.startsWith("net/minecraft")) {
				String key = owner.substring(owner.lastIndexOf('/') + 1) + ";" + name;
				if (fields.add(key)) {
					w.println("field " + key);
				}
			} else if (!fs.contains(name) && owner.equals(classNode.name) && sm) {
				String key = classNode.superName.substring(classNode.superName.lastIndexOf('/') + 1) + ";" + name;
				if (fields.add(key)) {
					w.println("field " + key);
				}
			}
			return name;
		}

		@Override
		public String mapMethodName(String owner, String name, String desc) {
			if (name.equals("<init>") || name.equals("<clinit>"))return name;
			if (owner.startsWith("net/minecraft")) {
				String key = owner.substring(owner.lastIndexOf('/') + 1) + ";" + name + desc;
				if (methods.add(key)) {
					w.println("method " + key);
				}
			}
			if (owner.equals(classNode.name) && sm) {
				String key = classNode.superName.substring(classNode.superName.lastIndexOf('/') + 1) + ";" + name + desc;
				if (ms.contains(name)) {
					System.out.println("Scanning for overrides: " + name);
					ClassNode sup = classNode;
					boolean found = false;
					do {
						sup = getClassNode(sup.superName);
						if (sup == null)break;
						for (MethodNode mn : (List<MethodNode>) sup.methods) {
							if (mn.name.equals(name) && mn.desc.equals(desc)) {
								found = true;
								w.println("override " + owner + " " + name + desc);
								break;
							}
						}
					} while(!found);
					if (!found)
						return name;
				}
				if (methods.add(key)) {
					w.println("method " + key);
				}
			}
			return name;
		}
	}

	private void mapObj(String owner) {
		mapType("L" + owner + ";");
	}

	public ClassNode getClassNode(String clazz) {
		if (clazz.startsWith("java"))return null;
		return classNodes.computeIfAbsent(clazz, n -> {
			System.out.println("Loading class: " + n);
			InputStream is = MappingDump.class.getResourceAsStream("/" + n + ".class");
			if (is == null) {
				System.out.println("\tNot Found");
				return null;
			}
			byte[] buffer = new byte[1024];
			try (InputStream s = is) {
				ByteArrayOutputStream fos = new ByteArrayOutputStream();
				int len;
				while ((len = s.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				byte[] cl = fos.toByteArray();
				if (applyTransform) cl = CPMTransformerService.transform(n, cl);
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader(cl);
				classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
				return classNode;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	private void mapType(String desc) {
		if (desc.startsWith("Lnet/minecraft") && clzs.add(desc)) {
			w.println("class " + desc.substring(1, desc.length() - 1).replace('/', '.'));
		}
	}
}
