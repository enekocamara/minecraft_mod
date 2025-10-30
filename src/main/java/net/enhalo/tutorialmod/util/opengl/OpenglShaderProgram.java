package net.enhalo.tutorialmod.util.opengl;

import net.enhalo.tutorialmod.TutorialMod;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL11;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenglShaderProgram {

    public int program;

    public OpenglShaderProgram(String shader_path) {
        TutorialMod.LOGGER.info("OpenglShaderProgram constructor");

        String vertexSrc;
        String fragmentSrc;
        program = GL20.glCreateProgram();
        TutorialMod.LOGGER.info("   program created");
        try{
            vertexSrc = loadShaderSource(shader_path + "/shader.vert");
            fragmentSrc = loadShaderSource(shader_path + "/shader.frag");
        }
        catch(IOException except){
            TutorialMod.LOGGER.error("Failed to load shader " + except.getMessage());
            throw new RuntimeException("Failed to load shader " + except.getMessage());
        }
        TutorialMod.LOGGER.info("   shader sources read");
        int vertShader = compileShader(vertexSrc, GL20.GL_VERTEX_SHADER);
        int fragShader = compileShader(fragmentSrc, GL20.GL_FRAGMENT_SHADER);
        TutorialMod.LOGGER.info("shaders compiled");
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        TutorialMod.LOGGER.info("shaders attached");
        GL20.glLinkProgram(program);
        TutorialMod.LOGGER.info("shaders linked waiting status");


        // Check linking
        //IntBuffer status = IntBuffer.allocate(1);
        IntBuffer status = BufferUtils.createIntBuffer(1);
        GL20.glGetProgramiv(program, GL20.GL_LINK_STATUS, status);
        if (status.get(0) == GL20.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program);
            TutorialMod.LOGGER.error("failed to link shaders" + log);
            throw new RuntimeException("Program linking failed: " + log);
        }
        TutorialMod.LOGGER.info("shaders linked");

        // Shaders can be deleted after linking
        GL20.glDeleteShader(vertShader);
        GL20.glDeleteShader(fragShader);
        TutorialMod.LOGGER.info("shaders cleanedup");
    }

    private int compileShader(String source, int type) {
        TutorialMod.LOGGER.info("compile shader starting");
        int shader = GL20.glCreateShader(type); // GL20.GL_VERTEX_SHADER or GL20.GL_FRAGMENT_SHADER
        TutorialMod.LOGGER.info("   shader created starting");
        GL20.glShaderSource(shader, source);
        TutorialMod.LOGGER.info("   source added starting");
        GL20.glCompileShader(shader);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            TutorialMod.LOGGER.error("Error " + error);

        }
        TutorialMod.LOGGER.info("   checking status");
        // Check compilation
        //IntBuffer status = IntBuffer.allocate(1);
        IntBuffer status = BufferUtils.createIntBuffer(1);
        TutorialMod.LOGGER.info("   int buffered");
        GL20.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, status);
        TutorialMod.LOGGER.info("   got status");
        if (status.get(0) == GL20.GL_FALSE) {
            TutorialMod.LOGGER.info("   getting log");
            String log = GL20.glGetShaderInfoLog(shader);
            TutorialMod.LOGGER.error(log);
            throw new RuntimeException("Shader compilation failed: " + log);
        }
        TutorialMod.LOGGER.info("   shader compiled");

        return shader;
    }

    private String loadShaderSource(String path) throws IOException {
        InputStream stream = TutorialMod.class.getResourceAsStream(path);
        if (stream == null) throw new FileNotFoundException("Shader not found: " + path);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
