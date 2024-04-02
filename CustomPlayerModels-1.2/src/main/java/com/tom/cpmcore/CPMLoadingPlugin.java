package com.tom.cpmcore;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import com.tom.cpm.CPMVersion;

public class CPMLoadingPlugin {
	public static boolean deobf;
	public static boolean isLoaded;

	public static void premain(String arg, Instrumentation ins) {
		CPMTransformerService.LOG.info("CPM Agent Starting");
		CPMTransformerService.LOG.info("CPM Version: " + CPMVersion.getVersion());
		CPMLoadingPlugin.deobf = System.getProperty("cpmcore.deobf", "false").equalsIgnoreCase("true");
		isLoaded = true;
		CPMTransformerService.init();
		ins.addTransformer(new ClassFileTransformer() {

			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				if (className == null)return classfileBuffer;
				try {
					return CPMTransformerService.transform(className.replace('/', '.'), classfileBuffer);
				} catch (Throwable e) {
					CPMTransformerService.LOG.error("Transformer failed", e);
					return null;
				}
			}

		});
		String d = System.getProperty("cpmcore.dump", "");
		if (!d.isEmpty()) {
			File zip = new File(System.getProperty("cpmcore.dumpsrc"));
			try {
				new MappingDump().run(zip, new File(d), System.getProperty("cpmcore.dump.verify", "false").equalsIgnoreCase("true"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (System.getProperty("cpmcore.dump.exit", "false").equalsIgnoreCase("true"))
				System.exit(0);
		}
	}

}
