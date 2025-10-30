package net.enhalo.tutorialmod.worldgen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noise.FastNoiseLite;
import net.enhalo.tutorialmod.TutorialMod;
import net.enhalo.tutorialmod.TutorialModClient;
import net.enhalo.tutorialmod.screen.NoisePreviewScreen;
import net.enhalo.tutorialmod.util.opengl.OpenglShaderProgram;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL20;

import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import java.nio.FloatBuffer;

import static net.enhalo.tutorialmod.TutorialMod.LOGGER;
import static net.enhalo.tutorialmod.TutorialMod.openShaderScreen;

public class CustomWorld {

    private final long seed;
    private final int cont_textureId;
    private final OpenglShaderProgram continental_program;
    private final  int width = 2000;
    private final int height = 2000;
    private final DimensionType dimension_type;
    private FastNoiseLite noise = new FastNoiseLite();
    //
    int[] heightMap = new int[width * height];
    private NoisePreviewScreen noiseScreen = null;

    public CustomWorld(DimensionType dimension_type, long seed) {
        RenderSystem.assertOnRenderThreadOrInit();
        LOGGER.info("Custom world constructor");
        this.seed = seed;
        this.dimension_type = dimension_type;
        /*for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                // Generate noise at each coordinate
                noiseMap[x][z] =( noise.GetNoise(x, z) + 1) / 2 * worldHeight;
            }
        }*/

        continental_program = new OpenglShaderProgram("/assets/tutorialmod/shaders/continental_map");

        //generatePlateTexture(seed);

    //}

    //private void generatePlateTexture(long seed) {

        // --- 2. Create fullscreen quad ---


        float vertices[] = {
                // positions    // texcoords
                -1.0f, -1.0f,   0.0f, 0.0f, // bottom-left
                1.0f, -1.0f,   1.0f, 0.0f, // bottom-right
                -1.0f,  1.0f,   0.0f, 1.0f, // top-left

                -1.0f,  1.0f,   0.0f, 1.0f, // top-left
                1.0f, -1.0f,   1.0f, 0.0f, // bottom-right
                1.0f,  1.0f,   1.0f, 1.0f  // top-right
        };

        int vaoId = GL30.glGenVertexArrays();
        int vboId = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        int stride = 4 * Float.BYTES;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2*Float.BYTES);


        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        // --- 3. Create texture and framebuffer ---
        this.cont_textureId = GL11.glGenTextures();
        if (cont_textureId == 0) {
            throw new RuntimeException("Failed to generate OpenGL texture ID");
        }
        LOGGER.info("texture ID: " + cont_textureId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, cont_textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, cont_textureId, 0);

        // --- 4. Render shader to texture ---
        GL11.glViewport(0, 0, width, height);
        GL20.glUseProgram(continental_program.program);

        // Set uniform
        int loc = GL20.glGetUniformLocation(continental_program.program, "iSeed");
        GL20.glUniform1f(loc, 31.0f);

        int loc2 = GL20.glGetUniformLocation(continental_program.program, "iTextSize");
        GL20.glUniform2f(loc2, width, height);

        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        TutorialMod.LOGGER.info("Texture rendered:" + cont_textureId);


        FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * 4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, cont_textureId);

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA, GL11.GL_FLOAT, buffer);
        LOGGER.info("heightmap numbers: ");
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                float height = buffer.get((y * width + x) * 4);
                heightMap[y * width + x] = (int)(height * (float)dimension_type.height());

            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RenderSystem.assertOnRenderThreadOrInit();
            if (openShaderScreen.wasPressed()) {
                if (noiseScreen == null) {
                    noiseScreen = new NoisePreviewScreen(width, height, cont_textureId, "continental_noise");
                    LOGGER.info("Creating NoisePreviewScreen");
                }
                /*if (was_created){
                    LOGGER.error("recreating the screen");
                }
                client.setScreen(new NoisePreviewScreen(width, height, 50, "continental_noise"));
                was_created = true;*/
                client.setScreen(noiseScreen);
                TutorialMod.LOGGER.info("rendering texture: " + this.cont_textureId);
            }
        });
        TutorialMod.LOGGER.info("render texture keybing registered");
    }

    public int getTextureId() {
        return cont_textureId;
    }

    public Chunk populateNoise(Chunk chunk){
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        LOGGER.info("populate noise: bottomY = " + chunk.getBottomY() + "; dimension_height = " + dimension_type.height() + "; chunk height = " + chunk.getHeight());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getBottomY(); y < dimension_type.height(); y++) {
                    pos.set(chunkPos.getStartX() + x, y, chunkPos.getStartZ() + z);
                    if (pos.getZ() > 2000 || pos.getZ() < 0 || pos.getX() > 2000 || pos.getX() < 0)
                        continue;
                    if (pos.getY() < 0){
                        LOGGER.error("height negative:" + pos.getY());
                    }
                    int height = heightMap[pos.getZ() * width + pos.getX()];
                    //if (pos.getY() > height)
                    //    continue;
                    if (pos.getY() >= dimension_type.height() / 2 && pos.getY() == height)
                        chunk.setBlockState(pos, Blocks.GRASS_BLOCK.getDefaultState(), false);
                    else if (pos.getY() < height)
                        chunk.setBlockState(pos, Blocks.STONE.getDefaultState(), false);
                    else if (pos.getY() < dimension_type.height() / 2)
                        chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
                }
            }
        }
        return chunk;
    }
}
