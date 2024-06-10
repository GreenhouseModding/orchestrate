package dev.greenhouseteam.orchestrate.datagen;

import dev.greenhouseteam.orchestrate.registry.OrchestrateItems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;

import java.util.concurrent.CompletableFuture;

public class OrchestrateDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(RecipeGenerator::new);
    }

    private static class RecipeGenerator extends FabricRecipeProvider {

        public RecipeGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        public void buildRecipes(RecipeOutput exporter) {
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, OrchestrateItems.COMPOSITION_TABLE)
                    .pattern("@@")
                    .pattern("##")
                    .pattern("##")
                    .define('@', ConventionalItemTags.AMETHYST_GEMS)
                    .define('#', ItemTags.PLANKS)
                    .unlockedBy("has_amethyst_shard", has(ConventionalItemTags.AMETHYST_GEMS))
                    .save(exporter);
        }
    }
}
