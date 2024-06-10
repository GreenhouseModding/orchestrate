package dev.greenhouseteam.orchestrate.block;

import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import dev.greenhouseteam.orchestrate.registry.OrchestrateBlockEntityTypes;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CompositionTableBlockEntity extends BlockEntity implements MenuProvider, Nameable {
    public static final Component DEFAULT_NAME = Component.translatable("container.orchestrate.composition");

    @Nullable
    private Component name;

    public CompositionTableBlockEntity(BlockPos pos, BlockState state) {
        super(OrchestrateBlockEntityTypes.COMPOSITION_TABLE, pos, state);
    }

    @Override
    public Component getName() {
        return name != null ? name : getDisplayName();
    }

    @Override
    public Component getDisplayName() {
        return DEFAULT_NAME;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        this.name = input.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("CustomName", 8))
            this.name = parseCustomNameSafe(tag.getString("CustomName"), provider);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (tag.contains("CustomName", 8))
            this.name = parseCustomNameSafe(tag.getString("CustomName"), provider);
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new CompositionMenu(this, inventory, syncId);
    }
}
