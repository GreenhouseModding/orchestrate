package dev.greenhouseteam.orchestrate.util;

import dev.greenhouseteam.orchestrate.block.CompositionTableBlockEntity;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class CompositionExtendedMenuFactory implements ExtendedScreenHandlerFactory<BlockPos> {
    private final BlockPos pos;

    public CompositionExtendedMenuFactory(BlockPos pos) {
        this.pos = pos;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new CompositionMenu(syncId, inventory, pos);
    }

    @Override
    public Component getDisplayName() {
        return CompositionTableBlockEntity.DEFAULT_NAME;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return pos;
    }
}
