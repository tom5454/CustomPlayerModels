package com.tom.cpm.shared.editor.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.tom.cpm.shared.editor.ElementType;
import com.tom.cpm.shared.editor.ModelElement;

public class StoreIDGen {
	private Random rng = new Random();
	private Set<Long> genValues = new HashSet<>();

	public void setID(ModelElement elem) {
		if(elem.type != ElementType.ROOT_PART) {
			if(elem.storeID != 0) {
				if(genValues.contains(elem.storeID)) {
					elem.storeID = 0;
				} else {
					genValues.add(elem.storeID);
				}
			}
			if(elem.storeID == 0) {
				elem.storeID = newId();
			}
		}
	}

	public long newId() {
		long v;
		do {
			v = Math.abs(rng.nextLong());
		} while(genValues.contains(v) && v > 10);
		return v;
	}
}
