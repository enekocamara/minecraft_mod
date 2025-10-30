package net.enhalo.tutorialmod.mixin;

import net.enhalo.tutorialmod.DummyBiomeSource;
import net.enhalo.tutorialmod.worldgen.TutorialModChunkGenerator;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;

import java.util.Map;


@Mixin(targets = "net.minecraft.world.gen.WorldPresets")
public class WorldPresetsMixin {

    /**
     * Inject at the tail of WorldPresets.bootstrap(Registerable<WorldPreset>).
     * This method will be called during registry bootstrap, so `presetRegisterable`
     * is the same Registerable that vanilla used to register all vanilla presets.
     */
    @Inject(method = "bootstrap", at = @At("TAIL"), remap = true)
    private static void onBootstrap(Registerable<WorldPreset> presetRegisterable, CallbackInfo ci) {
        // Build the RegistryKey for your new preset: tutorialmod:custom_world
        RegistryKey<WorldPreset> myKey = RegistryKey.of(RegistryKeys.WORLD_PRESET, new Identifier("tutorialmod", "custom_world"));

        // --- Build a DimensionOptions for overworld ---
        // We need a RegistryEntry<DimensionType> for vanilla overworld type.
        // The Registerable passed in has access to other registries via lookups; try to obtain it:

        // Try to get the built-in DimensionType entry for overworld:
        RegistryEntry<DimensionType> overworldTypeEntry;
        try {
            overworldTypeEntry = presetRegisterable.getRegistryLookup(RegistryKeys.DIMENSION_TYPE).getOrThrow(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier("minecraft:overworld")));
        } catch (Throwable t) {
            // Fallback: if mapping differs, try to acquire by name using reflection or fail loudly.
            throw new RuntimeException("Failed to obtain overworld DimensionType entry during WorldPresets.bootstrap injection", t);
        }

        // Create a biome source — simplest: a dummy one you control (no need to pull vanilla Biomes).
        // DummyBiomeSource should implement BiomeSource and return a RegistryEntry<Biome> (you provide this).
        Biome dummyBiomeObj = new Biome.Builder().build();
        RegistryEntry<Biome> dummyBiome = RegistryEntry.of(dummyBiomeObj);
        BiomeSource biomeSource = new DummyBiomeSource(dummyBiome); // implement separately

        // Create your chunk generator instance (constructor must match your class).
        ChunkGenerator generator = new TutorialModChunkGenerator(biomeSource, 64, 384, new Identifier("minecraft:stone"));

        // Build DimensionOptions using the found DimensionType entry and your generator
        DimensionOptions overworldOptions = new DimensionOptions(overworldTypeEntry, generator);

        // Build the WorldPreset map: only overworld is required
        Map<RegistryKey<DimensionOptions>, DimensionOptions> dims = Map.of(DimensionOptions.OVERWORLD, overworldOptions);
        WorldPreset myPreset = new WorldPreset(dims);

        // Finally register it into the Registerable (which is populating the WORLD_PRESET registry)
        // Registerable has a register method. Use it:
        presetRegisterable.register(myKey, myPreset);
        // If your mappings show register(name, supplier) instead, adapt accordingly.
    }
}

/*
@Mixin(BuiltinRegistries.class)
public class BuiltinRegistriesMixin {

    // Shadow the private WORLD_PRESET registry
    @Shadow @Final @Mutable
    private static Registry<WorldPreset> WORLD_PRESET;

    @Shadow @Final private static Registry<DimensionType> DIMENSION_TYPE;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void injectCustomOverworldPreset(CallbackInfo ci) {

        RegistryKey<WorldPreset> CUSTOM = RegistryKey.of(RegistryKeys.WORLD_PRESET, new Identifier("tutorialmod", "custom_world"));

        WORLD_PRESET;

        Biome dummyBiomeObj = new Biome.Builder().build();
        RegistryEntry<Biome> dummyBiome = RegistryEntry.of(dummyBiomeObj);
        BiomeSource biomeSource = new DummyBiomeSource(dummyBiome);

        // Your generator (must be registered in Registries.CHUNK_GENERATOR)
        TutorialModChunkGenerator generator = new TutorialModChunkGenerator(
                biomeSource,
                64,
                384,
                new Identifier("minecraft:stone")
        );

        RegistryEntry<DimensionType> overworldTypeEntry = BuiltinRegistries.DIMENSION_TYPE.getEntry(DimensionType.OVERWORLD)
                .orElseThrow(() -> new IllegalStateException("Overworld DimensionType missing"));

        // Build DimensionOptions
        DimensionOptions overworldOptions = new DimensionOptions(

                generator,

        );

        // Registry key for the overworld dimension options
        RegistryKey<DimensionOptions> overworldKey = RegistryKey.of(
                RegistryKeys.DIMENSION,
                new Identifier("minecraft:overworld")
        );

        Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions = Map.of(overworldKey, overworldOptions);
        WorldPreset customPreset = new WorldPreset(dimensions);


        Registry<WorldPreset> worldPresetRegistry = (Registry<WorldPreset>) ObfuscationReflectionHelper.getPrivateStaticField(BuiltinRegistries.class, "WORLD_PRESET");
        Map<RegistryKey<WorldPreset>, WorldPreset> map = (Map<RegistryKey<WorldPreset>, WorldPreset>) ObfuscationReflectionHelper.getPrivateField(Registry.class, worldPresetRegistry, "map");
        map.put(DimensionOptions.OVERWORLD, customPreset);

        WORLD_PRESET.(
                RegistryKey.of(RegistryKeys.WORLD_PRESET, new Identifier("minecraft:overworld")),
                customPreset
        );// Replace the overworld preset with your generator


    }
}
*/
