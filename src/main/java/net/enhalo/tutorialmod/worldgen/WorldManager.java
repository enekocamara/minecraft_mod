package net.enhalo.tutorialmod.worldgen;


import com.mojang.blaze3d.systems.RenderSystem;
import net.enhalo.tutorialmod.TutorialMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public class WorldManager {
    public CustomWorld world = null;
    public final DimensionType dimensionType = ContinentalDimensionType.CONTINENTAL_DIMENSION_TYPE_CLASS;

    public WorldManager(){
    }

    public void create_world(long seed){
        RenderSystem.assertOnRenderThreadOrInit();
        TutorialMod.LOGGER.info("Creating world");
        world = new CustomWorld(dimensionType, seed);
    }

}
