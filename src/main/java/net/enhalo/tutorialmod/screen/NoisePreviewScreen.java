package net.enhalo.tutorialmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.enhalo.tutorialmod.TutorialMod;
import net.enhalo.tutorialmod.util.opengl.OpenglLinkedTexture;
import net.enhalo.tutorialmod.util.opengl.OpenglShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NoisePreviewScreen extends Screen {

    private final Identifier textId;
    private final int openpltextId;

    public NoisePreviewScreen(int width, int height,int textureID, String name) {
        super(Text.literal("World Preview"));
        RenderSystem.assertOnRenderThreadOrInit();
        this.textId = OpenglLinkedTexture.wrapExistingGLTexture(textureID, name);
        this.openpltextId = textureID;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        RenderSystem.assertOnRenderThreadOrInit();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        //int texId = tex; // your texture handle (e.g. generated from GL11.glGenTextures)
        //TutorialMod.LOGGER.info("texture id" + this.openpltextId);
        RenderSystem.setShaderTexture(0, this.textId);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        // set white color (no tint)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // x, y, width, height — screen position and size
        context.drawTexture(
                this.textId,  // OpenGL texture ID
                10, 10, // x, y position on screen
                0, 0,   // u, v start
                256, 256, // draw width/height
                256, 256  // texture width/height
        );

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }


}
