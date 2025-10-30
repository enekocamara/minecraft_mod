package net.enhalo.tutorialmod;

import net.enhalo.tutorialmod.datagen.ModWorldGenerator;
import net.enhalo.tutorialmod.worldgen.ContinentalDimensionType;
import net.enhalo.tutorialmod.worldgen.EmptyBiome;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class TutorialModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        TutorialMod.LOGGER.info("Data generator init");
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModWorldGenerator::new);
	}

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder){
        TutorialMod.LOGGER.info("building registry");
        registryBuilder.addRegistry(RegistryKeys.DIMENSION_TYPE, ContinentalDimensionType::bootstrapType);
        registryBuilder.addRegistry(RegistryKeys.BIOME, EmptyBiome::bootstrapType);
    }
}
