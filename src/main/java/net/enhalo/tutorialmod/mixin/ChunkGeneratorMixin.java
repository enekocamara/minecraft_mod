package net.enhalo.tutorialmod.mixin;

import net.enhalo.tutorialmod.worldgen.IChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin implements IChunkGenerator {
    private long seed;
    public long get_seed(){
        return seed;
    }
    public void set_seed(long seed){
        this.seed = seed;
    }
}
