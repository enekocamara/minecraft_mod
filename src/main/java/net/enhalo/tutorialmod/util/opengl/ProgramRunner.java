package net.enhalo.tutorialmod.util.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.enhalo.tutorialmod.TutorialMod;

public class ProgramRunner {
    private static final float[] vertices = {
            // positions    // texcoords
            -1.0f, -1.0f,   0.0f, 0.0f, // bottom-left
            1.0f, -1.0f,   1.0f, 0.0f, // bottom-right
            -1.0f,  1.0f,   0.0f, 1.0f, // top-left

            -1.0f,  1.0f,   0.0f, 1.0f, // top-left
            1.0f, -1.0f,   1.0f, 0.0f, // bottom-right
            1.0f,  1.0f,   1.0f, 1.0f  // top-right
    };

    private final int vaoId;
    private final int vboId;
    private int fbo;

    public ProgramRunner() {

        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        int stride = 4 * Float.BYTES;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2 * Float.BYTES);


        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    @FunctionalInterface
    public interface UniformsSetter {
        void apply(int program);
    }

    public void runProgram(int program, int texture, int width, int height, UniformsSetter uniformsSetter){

        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);

        // --- 4. Render shader to texture ---
        GL11.glViewport(0, 0, width, height);
        GL20.glUseProgram(program);

        uniformsSetter.apply(program);

        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }
}
