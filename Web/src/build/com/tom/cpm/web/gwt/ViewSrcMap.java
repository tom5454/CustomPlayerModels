package com.tom.cpm.web.gwt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.atlassian.sourcemap.Mapping;
import com.atlassian.sourcemap.SourceMapImpl;

public class ViewSrcMap {
	private static BufferedReader rd;

	public static void main(String[] args) throws IOException {
		rd = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter source map path:");
		File f = new File(rd.readLine());
		SourceMapImpl map;
		try(BufferedReader rd = new BufferedReader(new FileReader(f))) {
			map = new SourceMapImpl(rd.lines().collect(Collectors.joining("\n")));
		}
		System.out.println("Enter line:col");
		String ln;
		while((ln = rd.readLine()) != null) {
			if(ln.isEmpty())break;
			String[] sp = ln.split(":");
			try {
				int line = Integer.parseInt(sp[0]);
				int col = Integer.parseInt(sp[1]);
				Mapping m = map.getMapping(line - 1, col - 1);
				System.out.println(m.getSourceSymbolName() + " (" + m.getSourceFileName() + ":" + m.getSourceLine() + ")");
			} catch (Exception e) {
			}
		}
	}
}
