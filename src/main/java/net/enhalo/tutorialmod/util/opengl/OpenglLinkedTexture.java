package net.enhalo.tutorialmod.util.opengl;

import net.enhalo.tutorialmod.TutorialMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public class OpenglLinkedTexture extends AbstractTexture {
    private final int existingTexId;

    public OpenglLinkedTexture(int existingTexId) {
        this.existingTexId = existingTexId;
        this.glId = existingTexId;
        TutorialMod.LOGGER.info("glId set to " + this.glId);
        TutorialMod.LOGGER.info("glId set to " + this.getGlId());
    }

    @Override
    public void load(ResourceManager manager) {
        // Do nothing — texture already exists on GPU
    }

    @Override
    public void close() {
        // Do NOT delete texture, since you manage it
    }
    @Override
    public void bindTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, existingTexId);
    }

    public static Identifier wrapExistingGLTexture(int texId, String name) {
        Identifier id = new Identifier("tutorialmod", name);
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, new OpenglLinkedTexture(texId));
        return id;
    }
}
