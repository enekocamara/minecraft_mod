package net.enhalo.tutorialmod.worldgen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.enhalo.tutorialmod.TutorialMod;
import net.enhalo.tutorialmod.TutorialModClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.noise.FastNoiseLite;


import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.MinecraftClient;

// get the client instance

public class TutorialModChunkGenerator extends ChunkGenerator {
    private final BiomeSource biomeSource;
    private final int seaLevel;
    private final Identifier customBlockID;
    private final BlockState customBlock;
    private final int worldHeight;
    private final int width = 2000;
    private final int height = 2000;
    private CustomWorld world = null;
    AtomicBoolean world_generating = new AtomicBoolean(false);

    public BiomeSource getBiomeSource() {
        //return this.biomeSource;
        return this.biomeSource;
    }

    public Identifier getCustomBlockID(){
        return this.customBlockID;
    }
    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    /*public void  create_custom_world(long seed){
        TutorialMod.LOGGER.info("create custom world running");
        RenderSystem.assertOnRenderThreadOrInit();
        this.world = new CustomWorld(, seed);
    }*/
    /*
    public int compileShader(String source, int type) {
        TutorialMod.LOGGER.info("compile shader starting");
        int shader = GL20.glCreateShader(type); // GL20.GL_VERTEX_SHADER or GL20.GL_FRAGMENT_SHADER
        TutorialMod.LOGGER.info("   shader created starting");
        GL20.glShaderSource(shader, source);
        TutorialMod.LOGGER.info("   source added starting");
        GL20.glCompileShader(shader);
        TutorialMod.LOGGER.info("   shader compiled");

        // Check compilation
        IntBuffer status = IntBuffer.allocate(1);
        GL20.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, status);
        if (status.get(0) == GL20.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            TutorialMod.LOGGER.error(log);
            throw new RuntimeException("Shader compilation failed: " + log);
        }

        return shader;
    }
    public int createShaderProgram(String vertexSrc, String fragmentSrc) {
        TutorialMod.LOGGER.info("create shader program starting");
        int program = GL20.glCreateProgram();
        TutorialMod.LOGGER.info("   program created");
        int vertShader = compileShader(vertexSrc, GL20.GL_VERTEX_SHADER);
        int fragShader = compileShader(fragmentSrc, GL20.GL_FRAGMENT_SHADER);
        TutorialMod.LOGGER.info("shader compiled");
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        GL20.glLinkProgram(program);

        // Check linking
        IntBuffer status = IntBuffer.allocate(1);
        GL20.glGetProgramiv(program, GL20.GL_LINK_STATUS, status);
        if (status.get(0) == GL20.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program);
            throw new RuntimeException("Program linking failed: " + log);
        }

        // Shaders can be deleted after linking
        GL20.glDeleteShader(vertShader);
        GL20.glDeleteShader(fragShader);

        return program;
    }
    */

    /*
    private void world_shader(){
        TutorialMod.LOGGER.info("world_shader starting");
        int width = 2000;
        int height = 2000;

         // --- 1. Create shader program ---
        String vertexSrc = """
    #version 330
    layout(location=0) in vec2 pos;
    out vec2 uv;
    void main() {
        uv = pos * 0.5 + 0.5;
        gl_Position = vec4(pos, 0.0, 1.0);
    }
""";

        String fragmentSrc = """
    #version 330
    in vec2 uv;
    out vec4 fragColor;
    uniform float iSeed;
    void main() {
        float value = fract(sin(dot(uv * iSeed, vec2(12.9898,78.233))) * 43758.5453);
        fragColor = vec4(vec3(value), 1.0);
    }
""";

        int program = createShaderProgram(vertexSrc, fragmentSrc);

        // --- 2. Create fullscreen quad ---
        float[] quadVertices = {
                -1, -1,
                1, -1,
                -1,  1,
                -1,  1,
                1, -1,
                1,  1
        };

        int vaoId = GL30.glGenVertexArrays();
        int vboId = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quadVertices, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        // --- 3. Create texture and framebuffer ---
        int texId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texId, 0);

        // --- 4. Render shader to texture ---
        GL11.glViewport(0, 0, width, height);
        GL20.glUseProgram(program);

        // Set uniform
        int loc = GL20.glGetUniformLocation(program, "iSeed");
        GL20.glUniform1f(loc, 42.0f);

        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);

        GL20.glUseProgram(0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

    }*/

    public TutorialModChunkGenerator(BiomeSource biomeSource, int seaLevel, int worldHeight, Identifier customBlockID) {
        super(biomeSource);
        TutorialMod.LOGGER.info("chunk generator starting");
        //MinecraftClient client = MinecraftClient.getInstance();
        //client.execute(this::world_shader);

        this.seaLevel = seaLevel;
        this.worldHeight = worldHeight;
        this.biomeSource = biomeSource;
        this.customBlock = Registries.BLOCK.getOrThrow(RegistryKey.of(RegistryKeys.BLOCK, customBlockID)).getDefaultState();
        this.customBlockID = customBlockID; // this line is included because we need to have a getter for the ID specifically
    }

    public static final Codec<TutorialModChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(TutorialModChunkGenerator::getBiomeSource),
                    Codec.INT.fieldOf("sea_level").forGetter(TutorialModChunkGenerator::getSeaLevel),
                    Codec.INT.fieldOf("world_height").forGetter(TutorialModChunkGenerator::getWorldHeight),
                    Identifier.CODEC.fieldOf("custom_block").forGetter(TutorialModChunkGenerator::getCustomBlockID)
            ).apply(instance, TutorialModChunkGenerator::new)
    );
    @Override
    protected Codec<TutorialModChunkGenerator> getCodec() {
        return this.CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {

    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {

    }

    @Override
    public void populateEntities(ChunkRegion region) {

    }

    @Override
    public int getWorldHeight() {
        return this.worldHeight;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        TutorialMod.LOGGER.info("populate noise called");
        if (world == null){
            boolean updated = world_generating.compareAndSet(false, true);
            if (updated) {
                world = TutorialModClient.world_manager.world;
            }
        }
        int bottomY = chunk.getBottomY();
        if (bottomY != 0){
            TutorialMod.LOGGER.error("bottomY is not 0");
            throw new RuntimeException("bottomY is not 0");
        }
        //if (world == null)
         //   return CompletableFuture.completedFuture(chunk);
        return CompletableFuture.supplyAsync(() -> {
            // Busy wait replaced by blocking synchronization
            synchronized (this) {
                while (this.world == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            }
            return this.world.populateNoise(chunk);
            }, executor);
        //return CompletableFuture.completedFuture(this.world.populateNoise(chunk));
        /*ChunkPos chunkPos = chunk.getPos();
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getBottomY(); y < worldHeight; y++) {
                    pos.set(chunkPos.getStartX() + x, y, chunkPos.getStartZ() + z);
                    if (pos.getZ() > 2000 || pos.getZ() < 0 || pos.getX() > 2000 || pos.getX() < 0)
                        continue;
                    if (pos.getY() < noiseMap[pos.getX()][pos.getZ()])
                        chunk.setBlockState(pos, customBlock, false);
                }
            }
        }*/
        //return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[]{Blocks.STONE.getDefaultState()};
        return new VerticalBlockSample(world.getBottomY(), states);
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {

    }
}
