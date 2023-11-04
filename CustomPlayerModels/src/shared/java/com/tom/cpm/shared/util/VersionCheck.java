package com.tom.cpm.shared.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tom.cpl.util.Pair;
import com.tom.cpm.externals.org.apache.maven.artifact.versioning.ComparableVersion;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.io.HTTPIO;

public class VersionCheck implements Runnable, IVersionCheck {
	private static final String PREV_TAG = "0.0.0";
	private String version, current;
	private Map<ComparableVersion, String> changes = new LinkedHashMap<>();
	private Map<ComparableVersion, String> allChanges = new LinkedHashMap<>();
	private boolean outdated;
	private static VersionCheck vc;
	private static IVersionCheck platform;
	private String homepage;
	private CompletableFuture<Void> finished = new CompletableFuture<>();

	public VersionCheck(String version, String current) {
		this.version = version;
		this.current = current;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			Log.info("[VersionChecker] Starting CPM version checker...");
			URL url = new URL(MinecraftObjectHolder.VERSION_CHECK_URL);
			HttpURLConnection httpCon = HTTPIO.createUrlConnection(url, true);
			String data = HTTPIO.getResponse(httpCon, url);
			Map<String, Object> json = (Map<String, Object>) MinecraftObjectHolder.gson.fromJson(data, Object.class);
			Map<String, String> promos = (Map<String, String>) json.get("promos");
			String lat = promos.get(version + "-latest");
			ComparableVersion current = new ComparableVersion(this.current);
			ComparableVersion target = null;
			if (lat != null) {
				target = new ComparableVersion(lat);
				outdated = current.compareTo(target) < 0;
			}
			Log.info("[VersionChecker] Current: " + current + " Target: " + target + " " + (outdated ? "Outdated" : "Up-to-date"));
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
			Set<String> dejavu = new HashSet<>();
			dejavu.add(version);
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
				if(dejavu.add(prev))
					tmp = (Map<String, String>) json.get(prev);
				else tmp = null;
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
		return outdated;
	}

	public String getHomepage() {
		return homepage;
	}

	public static void main(String[] args) {
		VersionCheck vc = new VersionCheck("1.19", "0.4.3a");
		vc.run();
		System.out.println(vc.getAllChanges().keySet());
	}

	public static VersionCheck get(String mcVersion, String modVersion) {
		if(vc == null) {
			vc = new VersionCheck(mcVersion, modVersion);
			ModelDefinitionLoader.THREAD_POOL.execute(vc);
		}
		return vc;
	}

	public static VersionCheck get() {
		return get(MinecraftCommonAccess.get().getMCVersion(), MinecraftCommonAccess.get().getModVersion());
	}

	public static <CV> IVersionCheck get(Supplier<Map<CV, String>> changes) {
		if(platform == null)platform = new PlatformCheck<>(changes);
		return platform;
	}

	private static class PlatformCheck<CV> implements IVersionCheck {
		private final Supplier<Map<CV, String>> changes;
		private Map<CV, String> c;

		public PlatformCheck(Supplier<Map<CV, String>> changes) {
			this.changes = changes;
		}

		private Map<CV, String> getC() {
			if(c == null)c = changes.get();
			if(c == null)return Collections.emptyMap();
			else return c;
		}

		@Override
		public boolean isOutdated() {
			return !getC().isEmpty();
		}

		@Override
		public Map<String, String> getChanges() {
			return getC().entrySet().stream().map(e -> Pair.of(e.getKey().toString(), e.getValue())).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		}
	}

	public CompletableFuture<Void> getFinished() {
		return finished;
	}
}
