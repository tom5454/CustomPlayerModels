package com.tom.cpmoscc;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.animation.Gesture;
import com.tom.cpm.shared.definition.ModelDefinition;

public class OSCMessageManager {
	private Map<String, Set<OSCMapping>> mappings = new ConcurrentHashMap<>();

	public void update(ModelDefinition def) {
		this.mappings.clear();
		Set<OSCMapping> mappings = new HashSet<>();
		AnimationRegistry ar = def.getAnimations();
		ar.getCustomPoses().values().stream().filter(CustomPose::isCommand).filter(p -> p.getName().startsWith("osc:/")).forEach(p -> {
			OSCMapping m = new OSCMapping(p);
			if(m.getOscPacketId() != null)
				mappings.add(m);
		});
		ar.getGestures().values().stream().filter(Gesture::isCommand).filter(p -> p.getName().startsWith("osc:/")).forEach(p -> {
			OSCMapping m = new OSCMapping(p);
			if(m.getOscPacketId() != null)
				mappings.add(m);
		});
		this.mappings.putAll(mappings.stream().collect(Collectors.groupingBy(OSCMapping::getOscPacketId, Collectors.toSet())));
		CPMOSC.LOGGER.info("Loaded OSC mappings: " + this.mappings.keySet());
	}

	public void acceptOsc(String address, List<Object> arguments) {
		Set<OSCMapping> ms = mappings.get(address);
		if(ms != null) {
			ms.forEach(m -> m.applyOsc(arguments));
		}
	}

	public void tick() {
		mappings.values().forEach(s -> s.forEach(OSCMapping::tick));
	}
}
