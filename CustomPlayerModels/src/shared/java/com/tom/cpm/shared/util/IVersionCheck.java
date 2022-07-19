package com.tom.cpm.shared.util;

import java.util.Map;

public interface IVersionCheck {
	boolean isOutdated();
	Map<String, String> getChanges();
}
