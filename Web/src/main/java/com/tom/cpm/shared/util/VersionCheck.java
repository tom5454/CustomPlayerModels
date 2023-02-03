package com.tom.cpm.shared.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.tom.cpl.util.Pair;
import com.tom.cpm.externals.org.apache.maven.artifact.versioning.ComparableVersion;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;

public class VersionCheck implements IVersionCheck {
	private static final String PREV_TAG = "0.0.0";
	private String version, current;
	private Map<ComparableVersion, String> changes = new LinkedHashMap<>();
	private Map<ComparableVersion, String> allChanges = new LinkedHashMap<>();
	private static VersionCheck vc;
	private String homepage;
	private CompletableFuture<Void> finished = new CompletableFuture<>();

	public VersionCheck(String version, String current) {
		this.version = version;
		this.current = current;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		loadVersionCheck().thenAccept(data -> {
			try {
				Map<String, Object> json = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(data, Object.class);
				Map<String, String> promos = (Map<String, String>) json.get("promos");
				String lat = promos.get(version + "-latest");
				ComparableVersion current = new ComparableVersion(this.current);
				ComparableVersion target = null;
				if (lat != null) {
					target = new ComparableVersion(lat);
				}
				Map<String, String> tmp = (Map<String, String>) json.get(version);
				if (tmp != null) {
					List<ComparableVersion> ordered = new ArrayList<>();
					for (String key : tmp.keySet()) {
						ComparableVersion ver = new ComparableVersion(key);
						if (ver.compareTo(current) > 0 && (target == null || ver.compareTo(target) < 1)) {
							ordered.add(ver);
						}
					}
					Collections.sort(ordered);

					for (ComparableVersion ver : ordered) {
						changes.put(ver, tmp.get(ver.toString()));
					}
				}
				List<Pair<ComparableVersion, String>> ordered = new ArrayList<>();
				ComparableVersion oldFilter = null;
				while(tmp != null) {
					for (String key : tmp.keySet()) {
						if(key.equals(PREV_TAG))continue;
						ComparableVersion ver = new ComparableVersion(key);
						if(oldFilter != null && ver.compareTo(oldFilter) > 0)continue;
						if(ordered.stream().map(Pair::getKey).noneMatch(ver::equals)) {
							ordered.add(Pair.of(ver, tmp.get(ver.toString())));
						}
					}

					String prev = tmp.get(PREV_TAG);
					if(prev != null && prev.indexOf(':') != -1) {
						String[] sp = prev.split(":");
						prev = sp[0];
						oldFilter = new ComparableVersion(sp[1]);
					} else {
						oldFilter = ordered.stream().map(Pair::getKey).sorted().findFirst().orElse(oldFilter);
					}
					tmp = (Map<String, String>) json.get(prev);
				}
				ordered.sort(Comparator.comparing(Pair::getKey));

				for (Pair<ComparableVersion, String> ver : ordered) {
					allChanges.put(ver.getKey(), ver.getValue());
				}

				if(version.indexOf('-') > 0) {
					homepage = (String) json.get("homepage-" + version.substring(version.indexOf('-') + 1));
				} else {
					homepage = (String) json.get("homepage");
				}
				finished.complete(null);
			} catch (Exception e) {
				Log.warn("CPM version check failed", e);
				finished.completeExceptionally(e);
			}
		}).exceptionally(e -> {
			Log.warn("CPM version check failed", e);
			finished.completeExceptionally(e);
			return null;
		});
	}

	public Map<ComparableVersion, String> getAllChanges() {
		return allChanges;
	}

	public Map<ComparableVersion, String> getChangesV() {
		return changes;
	}

	@Override
	public Map<String, String> getChanges() {
		return changes.entrySet().stream().map(e -> Pair.of(e.getKey().toString(), e.getValue())).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}

	@Override
	public boolean isOutdated() {
		return false;
	}

	public String getHomepage() {
		return homepage;
	}

	private static CompletableFuture<String> loadVersionCheck() {
		return MdResourceIO.fetch0("changelog").thenApply(pg -> new String(pg, StandardCharsets.UTF_8));
	}

	public static VersionCheck get(String mcVersion, String modVersion) {
		if(vc == null) {
			vc = new VersionCheck(mcVersion, modVersion);
			vc.run();
		}
		return vc;
	}

	public static VersionCheck get() {
		return get(MinecraftCommonAccess.get().getMCVersion(), MinecraftCommonAccess.get().getModVersion());
	}

	public CompletableFuture<Void> getFinished() {
		return finished;
	}
}
