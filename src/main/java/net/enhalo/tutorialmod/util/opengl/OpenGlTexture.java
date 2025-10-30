package net.enhalo.tutorialmod.util.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_RG;
import static org.lwjgl.opengl.GL30.GL_RG32F;

public class OpenGlTexture {
    private final int textureID;
    private final int width;
    private final int height;

    public int getTextureID(){return textureID;}
    public int getHeightID(){return height;}
    public int getWidthID(){return width;}

    public OpenGlTexture(int width, int height, int internal_format, int passed_format, int passed_type){
        this.width = width;
        this.height = height;

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internal_format, width, height, 0, passed_format, passed_type, 0);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }
}
