package dev.greenhouseteam.orchestrate.registry;

import dev.greenhouseteam.mib.registry.internal.RegistrationCallback;
import dev.greenhouseteam.orchestrate.Orchestrate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class OrchestrateItems {
    public static final BlockItem COMPOSITION_TABLE = new BlockItem(OrchestrateBlocks.COMPOSITION_TABLE, new Item.Properties());

    public static void registerAll(RegistrationCallback<Item> callback) {
        callback.register(BuiltInRegistries.ITEM, Orchestrate.asResource("composition_table"), COMPOSITION_TABLE);
    }
}
