package net.enhalo.tutorialmod.worldgen;

import net.enhalo.tutorialmod.util.opengl.OpenGlTexture;
import net.enhalo.tutorialmod.util.opengl.OpenglShaderProgram;

import static net.enhalo.tutorialmod.TutorialMod.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_RG;
import static org.lwjgl.opengl.GL30.GL_RG32F;

import net.enhalo.tutorialmod.util.opengl.ProgramRunner;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.function.BiFunction;

public class ContinentalTexture {
    private OpenGlTexture plate_id_text;
    private final OpenglShaderProgram plate_id_program;
    private final Map<Vec2, Plate> plates;
    private final static int WIDTH = 2000;
    private final static int HEIGHT = 2000;
    private final ProgramRunner programRunner = new ProgramRunner();
    record Vec2(float x, float y){}
    class Plate{
        //public final Vec2 pos;
        public final Set<Vec2> neighbours;

        Plate() {
            //this.pos = pos;
            this.neighbours = new HashSet<>();
        }
    }

    void fill_plates(){
        FloatBuffer buffer = BufferUtils.createFloatBuffer(WIDTH * HEIGHT * 2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, plate_id_text.getTextureID());

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA, GL11.GL_FLOAT, buffer);
        LOGGER.info("heightmap numbers: ");

        for (int y = 0; y < HEIGHT; y++){
            for (int x = 0; x < WIDTH; x += 2){
                Vec2 pos = new Vec2(buffer.get(y * WIDTH + x),buffer.get(y * WIDTH + x + 1));
                Plate plate = plates.computeIfAbsent(pos, p -> new Plate());
                // Check the 4 neighbors: up, down, left, right
                Vec2[] neighbors = new Vec2[] {
                        new Vec2(pos.x, pos.y - 1), // up
                        new Vec2(pos.x, pos.y + 1), // down
                        new Vec2(pos.x - 1, pos.y), // left
                        new Vec2(pos.x + 1, pos.y)  // right
                };
                for (Vec2 nPos : neighbors) {
                    // bounce/check bounds
                    if (nPos.x >= 0 && nPos.x < WIDTH && nPos.y >= 0 && nPos.y < HEIGHT) {
                        Plate neighborPlate = plates.get(nPos);
                        if (neighborPlate != null) {
                            plate.neighbours.add(nPos);
                            neighborPlate.neighbours.add(pos); // add back-reference
                        }
                    }
                }
            }
        }
    }

    ContinentalTexture(long seed){
        plate_id_text = new OpenGlTexture(WIDTH,HEIGHT,GL_RG32F, GL_R, GL_FLOAT);
        plate_id_program = new OpenglShaderProgram("continental_step_1");
        programRunner.runProgram(plate_id_program.program,plate_id_text.getTextureID(), WIDTH,HEIGHT,(prog) -> {
            int loc = GL20.glGetUniformLocation(prog, "iSeed");
            GL20.glUniform1f(loc, 31.0f);

            int loc2 = GL20.glGetUniformLocation(prog, "iTextSize");
            GL20.glUniform2f(loc2, WIDTH, HEIGHT);
        });
        plates = new HashMap<>();
        fill_plates();
    }
}
