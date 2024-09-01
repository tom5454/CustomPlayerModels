package com.tom.blockbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.tom.cpl.util.Pair;
import com.tom.cpm.web.gwt.ClasspathFix;

public class BuildBlockbench {
	public static final String VERSION = "0.6.18_pre1";

	public static void main(String[] args0) {
		Pair<File, File> in = main(false, true);
		File out = new File(args0[0], "Blockbench\\cpm_plugin.js");
		copy(in.getKey(), out);
		out = new File(args0[0], "Blockbench\\cpm_plugin.map");
		copy(in.getValue(), out);
		out = new File(args0[1], "blockbench_plugin-" + VERSION + ".js");
		copy(in.getKey(), out);
		out = new File(args0[1], "blockbench_plugin-" + VERSION + ".map");
		copy(in.getValue(), out);
	}

	public static Pair<File, File> main(boolean debug, boolean prod) {
		System.out.println("Running CPM Blockbench Plugin builder");
		String version = VERSION;
		File f = new File(".");
		File war = new File(f, "war");

		buildGwt(version, f, "Blockbench", debug);

		File symbolMaps = new File(war, "WEB-INF/deploy/cpmblockbench/symbolMaps");
		File[] sm = symbolMaps.listFiles(s -> s.getName().endsWith("_sourceMap0.json"));
		File sourceMap = null;
		File r = new File(war, "cpmblockbench/cpmblockbench.nocache.js");
		if(sm.length == 1) {
			sourceMap = sm[0];
		}
		return Pair.of(r, sourceMap);
	}

	private static void buildGwt(String version, File f, String mode, boolean debug) {
		//"-Xdebug", "-Xrunjdwp:server=n,transport=dt_socket,address=4013,suspend=y"
		String[] args = new String[] {"java", "-cp", ClasspathFix.getFixedClasspath(), "-Dugwt.sourcemap=http://localhost:8000/src/cpmblockbench.map", "com.tom.cpm.web.gwt.MainWrapper", debug ? "--buildDebug" : "--build", version, mode};
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(f);
		pb.inheritIO();
		System.out.println("Launching GWT compiler, mode: " + mode);
		try {
			int i = pb.start().waitFor();
			System.out.println("GWT exit: " + i);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("GWT compiler finished");
	}

	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] d = new byte[1024];
		int len = 0;
		while((len = is.read(d, 0, d.length)) > 0){
			os.write(d, 0, len);
		}
	}

	private static void copy(File from, File to){
		try (InputStream is = new FileInputStream(from);OutputStream os = new FileOutputStream(to)){
			copy(is, os);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
