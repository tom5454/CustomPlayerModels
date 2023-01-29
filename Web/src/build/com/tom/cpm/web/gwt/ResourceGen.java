package com.tom.cpm.web.gwt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ResourceGen {
	private static Path ent;
	private static boolean min;
	private static ZipArchive pf;

	public static String run(File wd, boolean min) {
		//File out = new File(wd, min ? "resources.min.js" : "resources.js");
		ResourceGen.min = min;
		pf = new ZipArchive();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			run(new File(wd, "../CustomPlayerModels/src/shared/resources"));
			run(new File(wd, "../CustomPlayerModels-EditorWeb/src/main/resources"));
			run(new File(wd, "../CustomPlayerModels-EditorWeb/src/blockbench/resources"));
			pf.save(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Generated Resources");
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	private static void run(File entry) throws IOException {
		ent = entry.toPath();
		Files.walk(ent).forEach(ResourceGen::process);
	}

	private static void process(Path p) {
		Path r = ent.relativize(p);
		File f = p.toFile();
		if(f.exists() && !f.isDirectory() && !f.getName().endsWith(".xcf")) {
			String path = r.toString().replace('\\', '/');
			if(min && path.startsWith("assets/cpm/textures/") && !path.endsWith("/cape.png") && !path.contains("/armor") && !path.endsWith("/elytra.png") && !path.endsWith("/slim.png") && !path.endsWith("/default.png") && !path.endsWith("free_space_template.png"))return;
			if(min && path.startsWith("assets/cpm/wiki/"))return;
			if(path.equals("icon.png") || path.endsWith(".lang"))return;
			try(InputStream is = new FileInputStream(f);OutputStream os = pf.setAsStream(path)) {
				byte[] data = new byte[1024];
				int n;
				while((n = is.read(data)) > 0) {
					os.write(data, 0, n);
				}
			} catch (IOException e) {
			}
		}
	}
}
