package net.enhalo.tutorialmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.enhalo.tutorialmod.TutorialMod;
import net.enhalo.tutorialmod.worldgen.IChunkGenerator;
import net.enhalo.tutorialmod.worldgen.TutorialModChunkGenerator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class MixinMinecraftServer {
    /*
    @Inject(method = "setupSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/level/ServerWorldProperties;ZZ)V",
            at = @At("HEAD"))
    private static void setupSpawn(ServerWorld world, ServerWorldProperties worldProperties, boolean bonusChest, boolean debugWorld,CallbackInfo ci) {
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();

        if (chunkGenerator instanceof TutorialModChunkGenerator){
            long seed = world.getSeed();
            TutorialMod.LOGGER.info("Tutorialmod chunk generator detected " + seed);
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                ((TutorialModChunkGenerator) (Object) chunkGenerator).create_custom_world(seed);
            });
            TutorialMod.LOGGER.info("custom world generation excheduled");
        }

    }
    @Shadow
    protected final SaveProperties saveProperties;

    @Inject(method = "createWorlds", at = @At("HEAD"))
    private void beforeWorldsCreation(CallbackInfo ci) {
        // This runs **before worlds are created**
        // Only run on your custom preset
        //ServerWorld world = null; // we don't have worlds yet

        ServerWorldProperties properties = saveProperties.getMainWorldProperties();

        GeneratorOptions options = saveProperties.getGeneratorOptions();


    }*/
    private long seed;
    @Shadow
    private ServerChunkManager chunkManager;

    public long get_seed(){
        return seed;
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onServerWorldInit(
            MinecraftServer server,
            Executor workerExecutor,
            LevelStorage.Session session,
            ServerWorldProperties properties,
            RegistryKey<World> worldKey,
            DimensionOptions dimensionOptions,
            WorldGenerationProgressListener worldGenerationProgressListener,
            boolean debugWorld,
            long seed,
            List<Spawner> spawners,
            boolean shouldTickTime,
            @Nullable RandomSequencesState randomSequencesState,
            CallbackInfo ci) {

        ChunkGenerator chunkGenerator = chunkManager.getChunkGenerator();
        if (chunkGenerator instanceof TutorialModChunkGenerator){
            ((IChunkGenerator) chunkGenerator).set_seed(seed);
            TutorialMod.LOGGER.info("Tutorialmod chunk generator seed set to: " + seed);
            MinecraftClient client = MinecraftClient.getInstance();
            this.seed = seed;
            //CompletableFuture<Void> dataReady = new CompletableFuture<>();
            /*client.execute(() -> {
                RenderSystem.assertOnRenderThreadOrInit();
                TutorialMod.LOGGER.info("creating world");
                ((TutorialModChunkGenerator) (Object) chunkGenerator).create_custom_world(seed);
                dataReady.complete(null);

            });*/
            //dataReady.join();
            TutorialMod.LOGGER.info("custom world generation finished");
        }
    }
}