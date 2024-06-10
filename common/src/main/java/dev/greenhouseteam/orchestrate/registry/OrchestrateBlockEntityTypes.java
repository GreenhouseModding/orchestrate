package dev.greenhouseteam.orchestrate.registry;

import dev.greenhouseteam.mib.registry.internal.RegistrationCallback;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.block.CompositionTableBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class OrchestrateBlockEntityTypes {
    public static final BlockEntityType<CompositionTableBlockEntity> COMPOSITION_TABLE = BlockEntityType.Builder.of(CompositionTableBlockEntity::new, OrchestrateBlocks.COMPOSITION_TABLE).build(Util.fetchChoiceType(References.BLOCK_ENTITY, "orchestrate:composition_table"));

    public static void registerAll(RegistrationCallback<BlockEntityType<?>> callback) {
        callback.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Orchestrate.asResource("composition_table"), COMPOSITION_TABLE);
    }
}
