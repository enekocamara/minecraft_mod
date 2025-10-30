package net.enhalo.tutorialmod.worldgen;

import net.enhalo.tutorialmod.TutorialMod;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.OptionalLong;

public class EmptyBiome {
    public static final RegistryKey<Biome> EMPTY_BIOME = RegistryKey.of(RegistryKeys.BIOME,
            new Identifier(TutorialMod.MOD_ID, "biome/empty"));
    public static void bootstrapType(Registerable<Biome> constext){

        try {
            constext.register(EMPTY_BIOME, new Biome.Builder()
                    .precipitation(true)
                    .temperature(0.5f)
                    .downfall(0f)
                    .effects(new BiomeEffects.Builder()
                            .fogColor(0xC0D8FF)         // light blue fog
                            .waterColor(0x3F76E4)       // default water
                            .waterFogColor(0x050533)    // default water fog
                            .skyColor(0x77ADFF)         // sky color
                            .foliageColor(0x00FF00)     // optional, can remove
                            .grassColor(0x00FF00)       // optional, can remove
                            .build())
                    .spawnSettings(new SpawnSettings.Builder().build())
                    .generationSettings(new GenerationSettings.Builder().build())
                    .build());
        }catch (IllegalStateException e){
            throw new RuntimeException("Failed to create empty biome:" + e.getMessage());
        }
    }
}
