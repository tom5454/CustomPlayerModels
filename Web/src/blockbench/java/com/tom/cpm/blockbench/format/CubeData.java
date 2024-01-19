package com.tom.cpm.blockbench.format;

import java.util.HashMap;
import java.util.Map;

import com.tom.cpm.blockbench.proxy.Cube;
import com.tom.cpm.blockbench.util.JsonUtil;
import com.tom.cpm.shared.editor.project.JsonMap;

public class CubeData {
	private final Cube cube;
	private boolean changed;
	private Extrude extrude;

	public CubeData(Cube cube) {
		this.cube = cube;
		if(cube.pluginData != null && !cube.pluginData.isEmpty()) {
			JsonMap s = JsonUtil.fromJson(cube.pluginData);
			if(s.containsKey("extrude")) {
				this.extrude = new Extrude();
				this.extrude.u = s.getInt("u", 0);
				this.extrude.v = s.getInt("v", 0);
				this.extrude.ts = s.getInt("ts", 0);
			}
		}
	}

	private void markDirty() {
		changed = true;
	}

	public void flush() {
		if(changed) {
			Map<String, Object> pluginDt = new HashMap<>();
			if(extrude != null) {
				Map<String, Object> ex = new HashMap<>();
				ex.put("u", extrude.u);
				ex.put("v", extrude.v);
				ex.put("ts", extrude.ts);
				pluginDt.put("extrude", ex);
			}
			if(!pluginDt.isEmpty())cube.pluginData = JsonUtil.toJson(pluginDt);
			else cube.pluginData = null;
			changed = false;
		}
	}

	public Extrude getExtrude() {
		return extrude;
	}

	public Extrude setExtrude(boolean en) {
		if(en) {
			if(extrude == null)extrude = new Extrude();
		} else {
			extrude = null;
		}
		markDirty();
		return extrude;
	}

	public class Extrude {
		private int u, v, ts;

		public int getU() {
			return u;
		}

		public Extrude setU(int u) {
			this.u = u;
			markDirty();
			return this;
		}

		public int getV() {
			return v;
		}

		public Extrude setV(int v) {
			this.v = v;
			markDirty();
			return this;
		}

		public int getTs() {
			return ts;
		}

		public Extrude setTs(int ts) {
			this.ts = ts;
			markDirty();
			return this;
		}

		public CubeData up() {
			return CubeData.this;
		}
	}
}
