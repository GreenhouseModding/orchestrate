package dev.greenhouseteam.orchestrate.menu;

import dev.greenhouseteam.mib.item.MibInstrumentItem;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.block.CompositionTableBlockEntity;
import dev.greenhouseteam.orchestrate.network.clientbound.UpdateSongClientboundPacket;
import dev.greenhouseteam.orchestrate.registry.OrchestrateBlocks;
import dev.greenhouseteam.orchestrate.registry.OrchestrateComponents;
import dev.greenhouseteam.orchestrate.registry.OrchestrateMenuTypes;
import dev.greenhouseteam.orchestrate.song.Note;
import dev.greenhouseteam.orchestrate.song.Song;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositionMenu extends AbstractContainerMenu {
    protected Container inputContainer = new SimpleContainer(INPUT_SLOT_SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (previousFilledInputs != getHighestInputSlotWithItem())
                filledInputs.set(Mth.clamp(getHighestInputSlotWithItem() + 1, 0, 4));

            if (getActiveSlot() == -1 && slots.getFirst().hasItem()) {
                setActiveSlot(0);
                createResult();
            } else if (getActiveSlot() > -1 && !slots.get(getActiveSlot()).hasItem()) {
                if (!player.level().isClientSide()) {
                    Orchestrate.getHelper().sendClientboundPacket(new UpdateSongClientboundPacket(getActiveSlot(), Song.DEFAULT), (ServerPlayer) player);
                    createResult();
                }
                setActiveSlot(Mth.clamp(getHighestInputSlotWithItem(), -1, 4));
            }
        }
    };
    protected Container resultContainer = new SimpleContainer(1);

    private final Player player;

    public static final int INPUT_SLOT_SIZE = 5;
    public static final int OUTPUT_SLOT = 5;
    public static final int TOTAL_SLOTS = 6;

    private final CompositionTableBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private int previousFilledInputs = 0;

    @Nullable
    private String songName = "";
    @Nullable
    private String songAuthor = "";
    private final Map<Integer, List<Note>> notes = new HashMap<>();

    private final DataSlot filledInputs = DataSlot.standalone();
    private final DataSlot activeSlot = DataSlot.standalone();

    public CompositionMenu(int syncId, Inventory inventory, BlockPos pos) {
        this(getBlockEntity(inventory, pos), inventory, syncId);
    }

    public CompositionMenu(CompositionTableBlockEntity blockEntity, Inventory inventory, int syncId) {
        super(OrchestrateMenuTypes.COMPOSITION_TABLE, syncId);
        this.blockEntity = blockEntity;
        this.player = inventory.player;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        for (int i = 0; i < INPUT_SLOT_SIZE; ++i) {
            int finalI = i;
            addSlot(new Slot(inputContainer, i, 15, 15 + (i * 20)) {
                @Override
                public boolean isActive() {
                    return getFilledInputs() > finalI - 1;
                }

                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof MibInstrumentItem;
                }
            });
        }
        addSlot(new Slot(resultContainer, 0, 156, 108) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                inputContainer.removeItem(getActiveSlot(), 1);
                inputContainer.setChanged();
                super.onTake(player, stack);
            }

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
        activeSlot.set(-1);
        addDataSlot(activeSlot);
        filledInputs.set(0);
        addDataSlot(filledInputs);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> this.clearContainer(player, inputContainer));
    }

    private int getHighestInputSlotWithItem() {
        int value = -1;
        for (int i = 0; i < inputContainer.getContainerSize(); ++i)
            if (!inputContainer.getItem(i).isEmpty())
                value = i;
        return value;
    }

    public static CompositionTableBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity be = inventory.player.level().getBlockEntity(pos);
        if (!(be instanceof CompositionTableBlockEntity composition))
            throw new IllegalStateException("Expected Composition block entity but got the wrong block entity.");
        return composition;
    }

    public BlockPos getPos() {
        return blockEntity.getBlockPos();
    }

    public int getFilledInputs() {
        return filledInputs.get();
    }

    public void setActiveSlot(int slot) {
        if (getFilledInputs() < slot)
            return;
        activeSlot.set(slot);

        if (inputContainer.getItem(slot).has(OrchestrateComponents.SONG)) {
            notes.put(slot, inputContainer.getItem(slot).get(OrchestrateComponents.SONG).notes());
            if (!player.level().isClientSide()) {
                Orchestrate.getHelper().sendClientboundPacket(new UpdateSongClientboundPacket(slot, inputContainer.getItem(slot).get(OrchestrateComponents.SONG)), (ServerPlayer)player);
                createResult();
            }
        }
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
        int minPlayerInventory = TOTAL_SLOTS;
        int maxPlayerInventory = minPlayerInventory + 36;
        ItemStack stack = slots.get(slot).getItem();
        ItemStack copy = stack.copy();
        if (slots.get(slot).hasItem()) {
            if (slot == output && !moveItemStackTo(stack, minPlayerInventory, maxPlayerInventory, true))
                return ItemStack.EMPTY;
            else if (slot > output) {
                if (!moveItemStackTo(stack, 0, 5, false))
                    return ItemStack.EMPTY;
            } else if (slot < output && !moveItemStackTo(stack, minPlayerInventory, maxPlayerInventory, false))
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

    public void setName(String name) {
        songName = name;
        createResult();
    }

    public void setAuthor(String author) {
        songAuthor = author;
        createResult();
    }

    public void setNotes(int channel, List<Note> notes) {
        this.notes.put(channel, notes);
        createResult();
    }

    public void createResult() {
        if (getActiveSlot() == -1) {
            resultContainer.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        ItemStack stack = inputContainer.getItem(getActiveSlot()).copy();
        if (notes.getOrDefault(getActiveSlot(), List.of()).isEmpty()) {
            if (stack.has(DataComponents.ITEM_NAME) && stack.has(OrchestrateComponents.SONG) && stack.get(DataComponents.ITEM_NAME).equals(stack.get(OrchestrateComponents.SONG).name()))
                stack.remove(DataComponents.ITEM_NAME);
            stack.remove(OrchestrateComponents.SONG);
            broadcastChanges();
            return;
        }

        if (songName != null)
            stack.set(DataComponents.ITEM_NAME, Component.literal(songName));
        Song.Builder builder = new Song.Builder();
        if (songName != null)
            builder.named(songName);
        if (songAuthor != null)
            builder.author(songAuthor);
        for (Note note : notes.getOrDefault(getActiveSlot(), List.of()))
            builder.add(note);
        stack.set(OrchestrateComponents.SONG, builder.build());
        resultContainer.setItem(0, stack);
        broadcastChanges();
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
