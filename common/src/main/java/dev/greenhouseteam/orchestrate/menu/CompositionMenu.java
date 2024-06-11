package dev.greenhouseteam.orchestrate.menu;

import dev.greenhouseteam.mib.item.MibInstrumentItem;
import dev.greenhouseteam.orchestrate.block.CompositionTableBlockEntity;
import dev.greenhouseteam.orchestrate.registry.OrchestrateBlocks;
import dev.greenhouseteam.orchestrate.registry.OrchestrateMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CompositionMenu extends AbstractContainerMenu {
    protected Container itemContainer = new SimpleContainer(MAX_SLOT_SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (previousFilledInputs != getHighestInputSlotWithItem())
                filledInputs.set(Mth.clamp(getHighestInputSlotWithItem() + 1, 0, 4));
            if (!slots.get(getActiveSlot()).hasItem())
                setActiveSlot(Mth.clamp(getHighestInputSlotWithItem(), 0, 4));
        }
    };
    public static final int MAX_SLOT_SIZE = 6;
    public static final int INPUT_SLOT_SIZE = 5;
    public static final int OUTPUT_SLOT = 5;

    private final ContainerLevelAccess access;
    private int previousFilledInputs = 0;

    private final DataSlot filledInputs = DataSlot.standalone();
    private final DataSlot activeSlot = DataSlot.standalone();

    public CompositionMenu(int syncId, Inventory inventory, BlockPos pos) {
        this(getBlockEntity(inventory, pos), inventory, syncId);
    }

    public CompositionMenu(CompositionTableBlockEntity blockEntity, Inventory inventory, int syncId) {
        super(OrchestrateMenuTypes.COMPOSITION_TABLE, syncId);
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        for (int i = 0; i < INPUT_SLOT_SIZE; ++i) {
            int finalI = i;
            addSlot(new Slot(itemContainer, i, 15, 15 + (i * 20)) {

                @Override
                public boolean isActive() {
                    return getFilledInputs() > finalI - 1;
                }

                @Override
                public void set(ItemStack stack) {
                    if (getItem().isEmpty() && !stack.isEmpty()) {
                        int value = Mth.clamp(finalI + 1, 0, 4);
                        filledInputs.set(value);
                        previousFilledInputs = value;
                    }
                    super.set(stack);
                }

                // TODO: Add an instrument tag to the base mod. It likely won't be used here, but it'd be good to have.
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof MibInstrumentItem;
                }
            });
        }
        addSlot(new Slot(itemContainer, 5, 156, 107) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean isFake() {
                return true;
            }
        });
        createInventorySlots(inventory);
        activeSlot.set(0);
        addDataSlot(activeSlot);
        filledInputs.set(0);
        addDataSlot(filledInputs);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> this.clearContainer(player, itemContainer));
    }

    private int getHighestInputSlotWithItem() {
        int value = -1;
        for (int i = 0; i < itemContainer.getContainerSize(); ++i)
            if (!itemContainer.getItem(i).isEmpty())
                value = i;
        return value;
    }

    public static CompositionTableBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity be = inventory.player.level().getBlockEntity(pos);
        if (!(be instanceof CompositionTableBlockEntity composition))
            throw new IllegalStateException("Expected Composition block entity but got the wrong block entity.");
        return composition;
    }

    public int getFilledInputs() {
        return filledInputs.get();
    }

    public void setActiveSlot(int slot) {
        if (getFilledInputs() < slot + 1)
            return;
        activeSlot.set(slot);
    }

    public int getActiveSlot() {
        return activeSlot.get();
    }

    /*
     This is basically a rip from Farmer's Delight, licensed under the MIT license.
     A link to the license can be found here: https://github.com/vectorwing/FarmersDelight/blob/1.19/LICENSE
     */
    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        int output = OUTPUT_SLOT;
        int minPlayerInventory = MAX_SLOT_SIZE;
        int maxPlayerInventory = minPlayerInventory + 36;
        ItemStack stack = slots.get(slot).getItem();
        ItemStack copy = stack.copy();
        if (slots.get(slot).hasItem()) {
            if (slot == output && !moveItemStackTo(stack, minPlayerInventory, maxPlayerInventory, true))
                return ItemStack.EMPTY;
            else if (slot > output) {
                if (!moveItemStackTo(stack, 0, 5, false))
                    return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, minPlayerInventory, maxPlayerInventory, false))
                return ItemStack.EMPTY;

            if (stack.isEmpty())
                slots.get(slot).set(ItemStack.EMPTY);
            else
                slots.get(slot).setChanged();

            if (stack.getCount() == copy.getCount())
                return ItemStack.EMPTY;

            slots.get(slot).onTake(player, stack);
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, OrchestrateBlocks.COMPOSITION_TABLE);
    }

    private void createInventorySlots(Inventory inventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 36 + j * 18, 137 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, 36 + i * 18, 195));
        }
    }
}
