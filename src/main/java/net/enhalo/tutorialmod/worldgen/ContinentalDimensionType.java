package net.enhalo.tutorialmod.worldgen;

import net.enhalo.tutorialmod.TutorialMod;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.OptionalLong;

public class ContinentalDimensionType {
    public static final RegistryKey<DimensionType> CONTINENTAL_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            new Identifier(TutorialMod.MOD_ID, "dimension/continental"));
    public static final DimensionType CONTINENTAL_DIMENSION_TYPE_CLASS = make_dimension_type();

    public static void bootstrapType(Registerable<DimensionType> constext){
        constext.register(CONTINENTAL_DIMENSION_TYPE, make_dimension_type());

    }
    private static DimensionType make_dimension_type(){
        return new DimensionType(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                true, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                0, // minY
                256, // height
                256, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                DimensionTypes.OVERWORLD_ID, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, UniformIntProvider.create(0, 0), 0));
    }
}
