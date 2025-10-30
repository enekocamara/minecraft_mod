package net.enhalo.tutorialmod;


import net.enhalo.tutorialmod.screen.NoisePreviewScreen;
import net.enhalo.tutorialmod.worldgen.WorldManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.WorldPreset;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.enhalo.tutorialmod.worldgen.TutorialModChunkGenerator;


public class TutorialMod implements ModInitializer {
	public static final String MOD_ID = "tutorialmod";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static  KeyBinding openShaderScreen;

    public static final RegistryKey<Biome> EMPTY_BIOME_KEY =
            RegistryKey.of(RegistryKeys.BIOME, new Identifier("tutorialmod", "empty"));



	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
/*
        Registry.register(Registries.BIOME_SOURCE, EMPTY_BIOME_KEY.getValue(),
                new Biome.Builder()
                        .precipitation(Biome.Precipitation.NONE)
                        .temperature(1.0f)
                        .downfall(0.0f)
                        .effects(new BiomeEffects.Builder()
                                .skyColor(8103167)
                                .waterColor(4159204)
                                .waterFogColor(329011)
                                .fogColor(12638463)
                                .build())
                        .spawnSettings(new SpawnSettings.Builder().build())
                        .generationSettings(new GenerationSettings.Builder().build())
                        .build()
        );*/

        Registry.register(Registries.CHUNK_GENERATOR,
            new Identifier("tutorialmod", "tutorialmodworldgenerator"),
                TutorialModChunkGenerator.CODEC);

        openShaderScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tutorialmod.openshader", // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P, // example key
                "category.tutorialmod.main"
        ));

        // 2. In your client tick event


        LOGGER.info("Hello Fabric world!");
	}

    public static class TutorialModClient {
    }
}