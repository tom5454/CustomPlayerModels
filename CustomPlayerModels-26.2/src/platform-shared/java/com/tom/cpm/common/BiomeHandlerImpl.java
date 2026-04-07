package com.tom.cpm.common;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.tom.cpl.block.BiomeHandler;
import com.tom.cpm.MinecraftServerObject;
import com.tom.cpm.common.WorldImpl.BiomeInfo;
import com.tom.cpm.shared.MinecraftServerAccess;

public class BiomeHandlerImpl extends BiomeHandler<BiomeInfo> {
	public static final BiomeHandlerImpl clientImpl = new BiomeHandlerImpl(() -> Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.BIOME));
	public static final BiomeHandlerImpl serverImpl = new BiomeHandlerImpl(() -> ((MinecraftServerObject) MinecraftServerAccess.get()).getServer().registryAccess().lookupOrThrow(Registries.BIOME));
	private static final BlockPos SEA_LEVEL = new BlockPos(0, 64, 0);

	public static BiomeHandlerImpl getImpl(Level level) {
		return level.isClientSide() ? clientImpl : serverImpl;
	}

	private final Supplier<Registry<Biome>> registry;

	public BiomeHandlerImpl(Supplier<Registry<Biome>> registry) {
		this.registry = registry;
	}

	@Override
	public List<com.tom.cpl.block.Biome> listNativeEntries(String tag) {
		Identifier rl = Identifier.tryParse(tag);
		if (rl == null)return Collections.emptyList();
		Biome b = registry.get().getValue(ResourceKey.create(Registries.BIOME, rl));
		if (b != null)return Collections.singletonList(wrap(new BiomeInfo(registry.get().wrapAsHolder(b), SEA_LEVEL)));
		return Collections.emptyList();
	}

	@Override
	public List<String> listNativeTags() {
		return registry.get().getTags().map(k -> k.key().location().toString()).toList();
	}

	@Override
	public com.tom.cpl.block.Biome emptyObject() {
		return wrap(null);
	}

	@Override
	public boolean isInTag(String tag, BiomeInfo state) {
		if (tag.charAt(0) == '#') {
			Identifier rl = Identifier.tryParse(tag.substring(1));
			if (rl != null) {
				TagKey<Biome> i = TagKey.create(Registries.BIOME, rl);
				return state.biome().is(i);
			}
		} else {
			return getBiomeId(state).equals(tag);
		}
		return false;
	}

	@Override
	public List<String> listTags(BiomeInfo state) {
		return state.biome().tags().map(k -> "#" + k.location()).toList();
	}

	@Override
	public List<com.tom.cpl.block.Biome> getAllElements() {
		return StreamSupport.stream(registry.get().asHolderIdMap().spliterator(), false).map(e -> wrap(new BiomeInfo(e, SEA_LEVEL))).collect(Collectors.toList());
	}

	@Override
	public boolean equals(BiomeInfo a, BiomeInfo b) {
		return a.value() == b.value();
	}

	@Override
	public String getBiomeId(BiomeInfo state) {
		return registry.get().getKey(state.value()).toString();
	}

	@Override
	public float getTemperature(BiomeInfo state) {
		return state.value().getBaseTemperature();
	}

	@Override
	public float getHumidity(BiomeInfo state) {
		return PlatformCommon.getClimateSettings(state.value()).downfall();
	}

	@Override
	public RainType getRainType(BiomeInfo state) {
		return RainType.get(state.value().getPrecipitationAt(state.at(), state.at().getY()).name());
	}

	@Override
	public boolean isAvailable() {
		try {
			return registry.get() != null;
		} catch (Exception e) {
			return false;
		}
	}
}
