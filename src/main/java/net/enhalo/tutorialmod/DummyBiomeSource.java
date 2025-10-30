package net.enhalo.tutorialmod;

import com.mojang.serialization.Codec;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.stream.Stream;

public class DummyBiomeSource extends BiomeSource {
    private final RegistryEntry<Biome> dummyBiome;

    public DummyBiomeSource(RegistryEntry<Biome> biome) {
        super(); // seed array, can be anything
        this.dummyBiome = biome;
    }

    /*@Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z) {
        return dummyBiome;
    }*/

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return null;
    }

    @Override
    protected Stream<RegistryEntry<Biome>> biomeStream() {
        return Stream.empty();
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        return null;
    }
}

