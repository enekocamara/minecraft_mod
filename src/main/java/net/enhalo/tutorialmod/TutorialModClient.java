package net.enhalo.tutorialmod;

import net.enhalo.tutorialmod.worldgen.WorldManager;
import net.fabricmc.api.ClientModInitializer;

public class TutorialModClient implements ClientModInitializer {
    public static final WorldManager world_manager = new WorldManager();
    @Override
    public void onInitializeClient(){

    }
}
