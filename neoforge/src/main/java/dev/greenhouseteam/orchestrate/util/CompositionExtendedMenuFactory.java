package dev.greenhouseteam.orchestrate.util;

import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.IContainerFactory;

public class CompositionExtendedMenuFactory implements IContainerFactory<CompositionMenu> {
    public CompositionExtendedMenuFactory() {}

    @Override
    public CompositionMenu create(int syncId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new CompositionMenu(syncId, inventory, buf.readBlockPos());
    }
}
