package dev.greenhouseteam.orchestrate.registry;

import dev.greenhouseteam.mib.registry.internal.RegistrationCallback;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.block.CompositionTableBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class OrchestrateBlocks {
    public static final CompositionTableBlock COMPOSITION_TABLE = new CompositionTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARTOGRAPHY_TABLE));

    public static void registerAll(RegistrationCallback<Block> callback) {
        callback.register(BuiltInRegistries.BLOCK, Orchestrate.asResource("composition_table"), COMPOSITION_TABLE);
    }
}
