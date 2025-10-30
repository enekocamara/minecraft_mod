package net.enhalo.tutorialmod.mixin;

import net.enhalo.tutorialmod.TutorialMod;
import net.enhalo.tutorialmod.TutorialModClient;
import net.enhalo.tutorialmod.worldgen.ContinentalDimensionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static net.minecraft.world.gen.GeneratorOptions.parseSeed;

//@Mixin(CreateWorldScreen.WorldTab.class)
@Mixin(targets = "net.minecraft.client.gui.screen.world.CreateWorldScreen$WorldTab")
public abstract class WorldTabMixin extends GridScreenTab{
    @Shadow
    private TextFieldWidget seedField;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        TutorialMod.LOGGER.info("Changing the menu");
        this.grid.refreshPositions();

        // Get the dimension type from the registry
        ButtonWidget button = ButtonWidget.builder(
                Text.literal("Generate"), action -> {
                    String seed = seedField.getText();
                    long seedLong = seed.isEmpty() ? new Random().nextLong() : parseSeed(seed).orElseThrow();
                    onButtonClick(seedLong);
                })
                .width(150)
                .build();



        this.grid.add(button,4,0);

    }

    public WorldTabMixin(Text title) {
        super(title);
    }


    private void onButtonClick(long seed) {
        // Your custom logic here
        System.out.println("Custom button clicked!");
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            TutorialModClient.world_manager.create_world(seed);
        });

        // Example: print or modify seed text field, etc.
        // You can access or modify world options here.
    }
    /*
    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addCustomPresets(CallbackInfo ci) {
        // Grab all entries from the WORLD_PRESET registry
        List<RegistryKey<WorldPreset>> allPresets = BuiltinRegistries.WORLD_PRESET.stream()
                .map(entry -> entry.getKey().orElse(null))
                .filter(key -> key != null)
                .collect(Collectors.toList());

        // Example: iterate them and print for debug
        allPresets.forEach(key -> System.out.println("Preset found: " + key.getValue()));*/

        // TODO: Replace the CycleButton or Dropdown widget with a new one containing allPresets
        // This is pseudocode, adapt depending on your mappings:

        // CycleButton<RegistryKey<WorldPreset>> presetDropdown = this.worldPresetButton;
        // presetDropdown.setValues(allPresets);
        // presetDropdown.setCurrentValue(allPresets.get(0));
}
