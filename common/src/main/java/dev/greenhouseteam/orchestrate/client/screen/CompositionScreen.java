package dev.greenhouseteam.orchestrate.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.greenhouseteam.mib.client.sound.MibSoundInstance;
import dev.greenhouseteam.mib.data.ExtendedSound;
import dev.greenhouseteam.mib.data.KeyWithOctave;
import dev.greenhouseteam.mib.registry.MibComponents;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import dev.greenhouseteam.orchestrate.network.serverbound.UpdateNotesServerboundPacket;
import dev.greenhouseteam.orchestrate.song.Note;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class CompositionScreen extends AbstractContainerScreen<CompositionMenu> {
    private static final ResourceLocation BACKGROUND_LOCATION = Orchestrate.asResource("textures/gui/container/composition_table.png");

    private boolean previousFullscreen;
    private final Map<Integer, List<NoteWidget>> notes = new HashMap<>();
    private MibSoundInstance playedSound;
    private int scroll = 0;
    private int bottomKeyOctave = KeyWithOctave.DEFAULT.getValue();
    private int previousNoteWidth = 4;
    private int duration = 0;
    private final List<AbstractWidget> widgetsToRemove = new ArrayList<>();

    public CompositionScreen(CompositionMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        imageWidth = 230;
        imageHeight = 219;
        titleLabelY = 5;
        inventoryLabelY = 127;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < CompositionMenu.INPUT_SLOT_SIZE; ++i)
            addRenderableWidget(new ToggleButton(i));
    }

    public void fromNotes(int channel, List<Note> notes) {
        this.notes.getOrDefault(channel, List.of()).forEach(this::removeWidget);
        this.notes.remove(channel);

        List<NoteWidget> converted = notes.stream().map(note -> new NoteWidget(note.startTime(), leftPos + 60 + note.startTime() - scroll, topPos + 22 - (((note.key().getValue() - bottomKeyOctave - 7) * 8 - 1)), note.duration(), channel, note.key().getValue())).toList();
        this.notes.put(channel, new ArrayList<>(converted));
        this.notes.get(channel).forEach(this::addRenderableWidget);
    }

    @Override
    protected void containerTick() {
        boolean previousFullscreen = Minecraft.getInstance().options.fullscreen().get();
        if (this.previousFullscreen != previousFullscreen)
            notes.values().forEach(widgets -> widgets.forEach(this::addRenderableWidget));
        this.previousFullscreen = previousFullscreen;

        widgetsToRemove.forEach(widget -> {
            if (getFocused() == widget) {
                setFocused(null);
                widget.setFocused(false);
            }
            removeWidget(widget);
        });
        widgetsToRemove.clear();
    }

    protected void removeNote(int channel, NoteWidget widget) {
        if (!notes.containsKey(channel))
            return;
        notes.get(channel).remove(widget);
        if (notes.get(channel).isEmpty())
            notes.remove(channel);
    }

    protected void updateNotes(int channel) {
        duration = notes.getOrDefault(channel, List.of()).stream().map(value -> value.start + value.getWidth()).max(Comparator.comparingInt(value -> value)).orElse(0);
        Orchestrate.getHelper().sendServerboundPacket(new UpdateNotesServerboundPacket(channel, notes.getOrDefault(channel, List.of()).stream().map(noteWidget -> new Note(KeyWithOctave.fromInt(noteWidget.keyWithOctave), 1.0F, noteWidget.start, noteWidget.getWidth())).toList()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float tickDelta, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND_LOCATION, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        for (int i = 0; i < CompositionMenu.INPUT_SLOT_SIZE; ++i) {
            if (menu.slots.get(i).isActive()) {
                graphics.blitSprite(Orchestrate.asResource("container/composition_table/layer/layer_" + (i + 1) + "_icon"), leftPos + 6, topPos + 14 + (i * 20), 7, 9);
                graphics.blitSprite(Orchestrate.asResource("container/composition_table/slot"), leftPos + 14, topPos + 14 + (i * 20), 18, 18);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener widget : this.children()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                if (button == 0)
                    setFocused(widget);
                if (button == 1)
                    setDragging(true);
                return true;
            }
        }

        if (isInNoteSection(mouseX, mouseY) && button == 0 && menu.getActiveSlot() > -1) {
            KeyWithOctave keyWithOctave = KeyWithOctave.fromInt((int) (bottomKeyOctave + Mth.clamp((topPos + 86 - mouseY) / 8, 0, 7)));
            var noteWidget = new NoteWidget((int) (scroll + (mouseX - leftPos - 60)), (int) mouseX, Mth.clamp(topPos + 86 - (int)((topPos + 86 - mouseY) / 8) * 8 - 7, topPos + 22, topPos + 86), previousNoteWidth, menu.getActiveSlot(), keyWithOctave.getValue());
            noteWidget.storeValues(mouseX);
            addRenderableWidget(noteWidget);
            notes.computeIfAbsent(menu.getActiveSlot(), i -> new ArrayList<>()).add(noteWidget);
            noteWidget.mouseClicked(mouseX, mouseY, button);
            setFocused(noteWidget);
            noteWidget.moving = true;
            return true;
        }

        if (isInNoteSection(mouseX, mouseY) && button == 1)
            setDragging(true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (playedSound != null && button == 0) {
            playedSound.stopOrFadeOut();
            playedSound = null;
        }

        if (getFocused() != null)
            getFocused().mouseReleased(mouseX, mouseY, button);

        setFocused(null);
        setDragging(false);
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (GuiEventListener widget : this.children()) {
            if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY))
                return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyIndex, int scanCode, int modifiers) {
        // TODO: Space -> play shortcut.
        if (keyIndex == 32)
            return true;
        return super.keyPressed(keyIndex, scanCode, modifiers);
    }

    protected boolean isInNoteSection(double mouseX, double mouseY) {
        return mouseX >= leftPos + 60 && mouseX <= leftPos + 209 && mouseY >= topPos + 22 && mouseY <= topPos + 86;
    }

    public class NoteWidget extends AbstractWidget {
        private final int channelIndex;
        private int start;
        private int keyWithOctave;

        public NoteWidget(int noteX, int x, int y, int width, int channelIndex, int keyWithOctave) {
            super(x, y, width, 7, CommonComponents.EMPTY);
            this.start = noteX;
            this.channelIndex = channelIndex;
            this.keyWithOctave = keyWithOctave;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
            if (start < scroll || start > scroll + 208)
                return;
            if (keyWithOctave < bottomKeyOctave || keyWithOctave > bottomKeyOctave + 7)
                return;
            RenderSystem.enableBlend();
            for (int i = 0; i < width; ++i) {
                if (start + i > scroll + 208)
                    continue;
                int w = i == 0 || i == width - 1 ? 1 : 3;
                if (w > 1 && i > width - Math.min(4, width - 1) && i != width - 1)
                    continue;
                if (menu.getActiveSlot() != channelIndex)
                    graphics.setColor(1.0F, 1.0F, 1.0F, 0.4F);
                int displayX = i > width - 5 && i != width - 1 ? Math.min(3, width - 2) : 1;
                graphics.blitSprite(getTextureForSegment(i), w, 7, 0, 0, getX() + i, getY(), displayX, 7);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            RenderSystem.disableBlend();
        }

        private ResourceLocation getTextureForSegment(int i) {
            if (i == 0 || i == width - 1)
                return Orchestrate.asResource("container/composition_table/note/layer_" + (channelIndex + 1) + "_note_end");
            return Orchestrate.asResource("container/composition_table/note/layer_" + (channelIndex + 1) + "_note");
        }

        private int resizeDir = 0;
        private boolean resizing = false;
        private boolean moving = false;
        private int originalStart = 0;
        private int originalWidth = 0;

        private double originalMouseX;
        private double mouseOrigin;

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (menu.getActiveSlot() == channelIndex) {
                boolean bl = clicked(mouseX, mouseY);
                if (bl) {
                    if (button == 0) {
                        onClick(mouseX, mouseY);
                        ItemStack instrument = menu.getSlot(menu.getActiveSlot()).getItem();
                        KeyWithOctave keyWithOctave = KeyWithOctave.fromInt(this.keyWithOctave);
                        if (instrument.has(MibComponents.INSTRUMENT)) {
                            var sounds = instrument.get(MibComponents.INSTRUMENT).sound().unwrap(Minecraft.getInstance().level.registryAccess());
                            if (sounds.isPresent()) {
                                ExtendedSound sound = sounds.get().value().getSound(keyWithOctave, 1.0F);
                                if (sound != null) {
                                    playedSound = MibSoundInstance.createPosDependent(Minecraft.getInstance().cameraEntity.position(), sound, 1.0F, keyWithOctave.getPitchFromNote(sounds.get().value().getClosestDefined(keyWithOctave)));
                                    Minecraft.getInstance().getSoundManager().play(playedSound);
                                }
                            }
                        }
                        setDragging(true);
                        setFocused(true);
                        storeValues(mouseX);
                        return true;
                    } else if (button == 1) {
                        removeNote(channelIndex, this);
                        widgetsToRemove.add(this);
                        setDragging(true);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            return menu.getActiveSlot() == channelIndex && visible && mouseX >= (double)getX() && mouseY >= (double)getY() - 1 && mouseX < (double)(getX() + getWidth()) && mouseY < (double)(getY() + getHeight() + 1);
        }

        public void storeValues(double mouseX) {
            resizeDir = 0;
            resizing = false;
            moving = false;
            originalWidth = width;
            originalStart = start;
            originalMouseX = mouseX;
            mouseOrigin = mouseX - getX();
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            setFocused(false);
            updateNotes(channelIndex);
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (menu.getActiveSlot() == channelIndex && visible && isFocused()) {
                // TODO: Allow resizing and moving to scroll.
                if (isValidClickButton(button)) {
                    if (resizing && (mouseX < leftPos + 60 + start - scroll && resizeDir > 0 || mouseX > leftPos + 60 + start - scroll + width && resizeDir < 0)) {
                        originalWidth = 1;
                        start = originalStart;
                        originalMouseX = leftPos + 60 + originalStart - scroll;
                        setX((int) originalMouseX);
                        resizeDir = 0;
                    }

                    double mouseDiff = mouseX - originalMouseX;
                    double resizeBounds = width < 8 ? (double) width / 4 : (double) width / 8;
                    // TODO: Clip to beat size whilst not pressing shift.
                    if ((mouseX > leftPos + 60 + start + width - resizeBounds && (isInNoteSection(mouseX, mouseY) && mouseY >= (double)getY() && mouseY < (double)(getY() + getHeight() + 1) || resizeDir == 0 && mouseDiff >= 1) || resizeDir == 1) && !moving) {
                        width = Math.clamp(originalWidth + Mth.floor(mouseDiff), 1, 149 - originalStart);
                        resizing = true;
                        resizeDir = 1;
                        previousNoteWidth = width;
                    } else if ((mouseX < leftPos + 60 + start + resizeBounds && (isInNoteSection(mouseX, mouseY) && mouseY >= (double)getY() && mouseY < (double)(getY() + getHeight() + 1) || resizeDir == 0 && mouseDiff <= -1) || resizeDir == -1) && !moving) {
                        width = Math.clamp(originalWidth - Mth.floor(mouseDiff), 1, originalWidth + originalStart);
                        start = Math.clamp(originalStart + Mth.floor(mouseDiff), scroll, scroll + 149 - width);
                        setX((int) Math.clamp(originalMouseX + Mth.floor(mouseDiff), leftPos + 60, leftPos + 209 - width));
                        resizing = true;
                        resizeDir = -1;
                        previousNoteWidth = width;
                    } else if (!resizing && (isInNoteSection(mouseX, mouseY) || moving)) {
                        start = (int) Math.clamp(originalStart + mouseDiff, scroll, scroll + 149 - width);
                        setX((int) Math.clamp(originalMouseX + mouseDiff - mouseOrigin, leftPos + 60, leftPos + 209 - width));
                        moving = true;
                    }

                    if (!resizing) {
                        int newKey = (int) (bottomKeyOctave + Mth.clamp(((86 + topPos - mouseY) / 8), 0, 7));
                        if (newKey != keyWithOctave) {
                            keyWithOctave = newKey;
                            setY(Mth.clamp(topPos + 86 - (int)((86 + topPos - mouseY) / 8) * 8 - 7, topPos + 22, topPos + 79));
                            playedSound.stopAndClear();
                            ItemStack instrument = menu.getSlot(menu.getActiveSlot()).getItem();
                            KeyWithOctave keyWithOctave = KeyWithOctave.fromInt(this.keyWithOctave);
                            if (instrument.has(MibComponents.INSTRUMENT)) {
                                var sounds = instrument.get(MibComponents.INSTRUMENT).sound().unwrap(Minecraft.getInstance().level.registryAccess());
                                if (sounds.isPresent()) {
                                    ExtendedSound sound = sounds.get().value().getSound(keyWithOctave, 1.0F);
                                    if (sound != null) {
                                        playedSound = MibSoundInstance.createPosDependent(Minecraft.getInstance().cameraEntity.position(), sound, 1.0F, keyWithOctave.getPitchFromNote(sounds.get().value().getClosestDefined(keyWithOctave)));
                                        Minecraft.getInstance().getSoundManager().play(playedSound);
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
            } else if (button == 1 && clicked(mouseX, mouseY)) {
                removeNote(channelIndex, this);
                widgetsToRemove.add(this);
                return true;
            }
            return false;
        }

        @Override
        public int getTabOrderGroup() {
            return menu.getActiveSlot() == channelIndex ? 5 : 0;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {

        }
    }

    public class ToggleButton extends AbstractButton {
        private final int index;

        protected ToggleButton(int index) {
            super(leftPos + 7, topPos + 24 + (index * 20), 5, 5, CommonComponents.EMPTY);
            this.index = index;
        }

        @Override
        public void onPress() {
            if (!menu.slots.get(index).isActive() || !menu.slots.get(index).hasItem() || menu.getActiveSlot() == index)
                return;

            menu.setActiveSlot(index);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
            if (!menu.slots.get(index).isActive())
                return;

            ResourceLocation spriteLoc;
            if (this.isHovered() && menu.slots.get(index).hasItem())
                spriteLoc = Orchestrate.asResource("container/composition_table/layer/layer_" + (index + 1) + "_button_highlighted");
            else if (menu.getActiveSlot() != index)
                spriteLoc = Orchestrate.asResource("container/composition_table/layer/layer_" + (index + 1) + "_button_disabled");
            else
                spriteLoc = Orchestrate.asResource("container/composition_table/layer/layer_" + (index + 1) + "_button");

            graphics.blitSprite(spriteLoc, this.getX(), this.getY(), this.width, this.height);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
        }

        @Override
        protected boolean clicked(double x, double y) {
            return menu.slots.get(index).isActive() && menu.slots.get(index).hasItem() && menu.getActiveSlot() != index && super.clicked(x, y);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
