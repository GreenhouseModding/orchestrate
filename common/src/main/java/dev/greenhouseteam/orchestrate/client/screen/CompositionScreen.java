package dev.greenhouseteam.orchestrate.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.greenhouseteam.mib.client.sound.MibSoundInstance;
import dev.greenhouseteam.mib.data.ExtendedSound;
import dev.greenhouseteam.mib.data.KeyWithOctave;
import dev.greenhouseteam.mib.registry.MibComponents;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
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

import java.util.ArrayList;
import java.util.List;

public class CompositionScreen extends AbstractContainerScreen<CompositionMenu> {
    private static final ResourceLocation BACKGROUND_LOCATION = Orchestrate.asResource("textures/gui/container/composition_table.png");

    private boolean previousFullscreen;
    private final List<NoteWidget> notes = new ArrayList<>();
    private NoteWidget selectedNote;
    private MibSoundInstance playedSound;
    private int scroll = 0;
    private int bottomKeyOctave = KeyWithOctave.DEFAULT.getValue();
    private List<AbstractWidget> widgetsToRemove = new ArrayList<>();

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

    @Override
    protected void containerTick() {
        boolean previousFullscreen = Minecraft.getInstance().options.fullscreen().get();
        if (this.previousFullscreen != previousFullscreen)
            notes.forEach(this::addRenderableWidget);
        this.previousFullscreen = previousFullscreen;
        widgetsToRemove.forEach(this::removeWidget);
        widgetsToRemove.clear();
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
                if (button == 0) {
                    setFocused(widget);
                    setDragging(true);
                }
                return true;
            }
        }

        if (isInNoteSection(mouseX, mouseY) && button == 0 && menu.getActiveSlot() > -1) {
            KeyWithOctave keyWithOctave = KeyWithOctave.fromInt((int) (bottomKeyOctave + Mth.clamp(((86 + topPos - mouseY) / 8), 0, 7)));
            var noteWidget = new NoteWidget((int) (scroll + (mouseX - leftPos - 60)), (int) mouseX,  Mth.clamp(topPos + 86 - (int)((86 + topPos - mouseY) / 8) * 8 - 7, topPos + 22, topPos + 86), menu.getActiveSlot(), keyWithOctave.getValue());
            addRenderableWidget(noteWidget);
            notes.add(noteWidget);
            setFocused(noteWidget);
            setDragging(true);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (playedSound != null && button == 0) {
            playedSound.stopOrFadeOut();
            selectedNote = null;
            playedSound = null;
        }
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
        if (keyIndex == 32)
            return true;
        return super.keyPressed(keyIndex, scanCode, modifiers);
    }

    protected boolean isInNoteSection(double mouseX, double mouseY) {
        return mouseX >= leftPos + 60 && mouseX <= leftPos + 208 && mouseY >= topPos + 22 && mouseY <= topPos + 86;
    }

    public class NoteWidget extends AbstractWidget {
        private final int channelIndex;
        private int start;
        private int keyWithOctave;

        public NoteWidget(int noteX, int x, int y, int channelIndex, int keyWithOctave) {
            super(x, y, 1, 7, CommonComponents.EMPTY);
            this.start = noteX;
            this.channelIndex = channelIndex;
            this.keyWithOctave = keyWithOctave;
        }

        public int getStart() {
            return start;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
            if (start < scroll || start > scroll + 149)
                return;
            if (keyWithOctave < bottomKeyOctave || keyWithOctave > bottomKeyOctave + 7)
                return;
            RenderSystem.enableBlend();
            for (int i = 0; i < width; ++i) {
                if (start + i > scroll + 149)
                    continue;
                int w = i == 0 || i == width - 1 ? 1 : 3;
                if (w > 1 && i > width - 4 && i != width - 1)
                    continue;
                if (menu.getActiveSlot() != channelIndex)
                    graphics.setColor(1.0F, 1.0F, 1.0F, 0.4F);
                graphics.blitSprite(getTextureForSegment(i), w, 7, 0, 0, getX() + i, getY(), i > width - 5 && i != width - 1 ? 3 : 1, 7);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            RenderSystem.disableBlend();
        }

        private ResourceLocation getTextureForSegment(int i) {
            if (i == 0 || i == width - 1)
                return Orchestrate.asResource("container/composition_table/note/layer_" + (channelIndex + 1) + "_note_end");
            return Orchestrate.asResource("container/composition_table/note/layer_" + (channelIndex + 1) + "_note");
        }

        private boolean resizing = false;
        private boolean moving = false;
        private int originalStart = 0;

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (menu.getActiveSlot() == channelIndex) {
                boolean bl = clicked(mouseX, mouseY);
                if (bl) {
                    if (button == 0) {
                        this.onClick(mouseX, mouseY);
                        selectedNote = this;
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
                        resizing = false;
                        moving = false;
                        originalStart = getX();
                        return true;
                    } else if (button == 1) {
                        selectedNote = null;
                        notes.remove(this);
                        widgetsToRemove.add(this);
                        return false;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (menu.getActiveSlot() == channelIndex && visible) {
                // TODO: Allow resizing and moving to scroll.
                if (isValidClickButton(button) && isFocused()) {
                    if (mouseX > getX() + Math.max(width - 6, width - (width / 2)) && !moving) {
                        if (mouseX >= originalStart) {
                            width = (int) Math.clamp(width + dragX, 1, 149 - start);
                        } else if (start < scroll + 149) {
                            int widthDiff = width;
                            width = (int) Math.clamp(width + dragX, 1, 149 - start);
                            widthDiff = width - widthDiff;
                            start = Math.clamp(start + widthDiff, scroll, scroll + 149 - width);
                            setX(Math.clamp(getX() + widthDiff, leftPos + 60, leftPos + 208 - width));
                        }
                        resizing = true;
                    } else if (mouseX < getX() + Math.max(6, width / 2) && !moving) {
                        if (mouseX >= originalStart) {
                            width = (int) Math.clamp(width - dragX, 1, 149 - start);
                        } else if (start > scroll) {
                            int widthDiff = width;
                            width = (int) Math.clamp(width - dragX, 1, 149 - start);
                            widthDiff = width - widthDiff;
                            start = Math.clamp(start - widthDiff, scroll, scroll + 149 - width);
                            setX(Math.clamp(getX() - widthDiff, leftPos + 60, leftPos + 208 - width));
                        }
                        resizing = true;
                    } else if (!resizing) {
                        start = (int) Math.clamp(start + dragX, scroll, scroll + 149 - width);
                        setX((int) Math.clamp(getX() + dragX, leftPos + 60, leftPos + 208 - width));
                        moving = true;
                    }
                    return true;
                } else if (button == 1 && clicked(mouseX, mouseY)) {
                    selectedNote = null;
                    notes.remove(this);
                    widgetsToRemove.add(this);
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput var1) {

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
