package dev.greenhouseteam.orchestrate.client.screen;

import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompositionScreen extends AbstractContainerScreen<CompositionMenu> {
    private static final ResourceLocation BACKGROUND_LOCATION = Orchestrate.asResource("textures/gui/container/composition_table.png");

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
    protected void renderBg(GuiGraphics graphics, float tickDelta, int x, int y) {
        graphics.blit(BACKGROUND_LOCATION, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        for (int i = 0; i < CompositionMenu.INPUT_SLOT_SIZE; ++i) {
            if (menu.slots.get(i).isActive()) {
                graphics.blitSprite(Orchestrate.asResource("container/composition_table/layer_" + (i + 1) + "_icon"), leftPos + 6, topPos + 14 + (i * 20), 7, 9);
                graphics.blitSprite(Orchestrate.asResource("container/composition_table/slot"), leftPos + 14, topPos + 14 + (i * 20), 18, 18);
            }
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
        public void renderWidget(GuiGraphics graphics, int $$1, int $$2, float $$3) {
            if (!menu.slots.get(index).isActive())
                return;

            ResourceLocation spriteLoc;
            if (this.isHovered() && menu.slots.get(index).hasItem())
                spriteLoc = Orchestrate.asResource("container/composition_table/layer_" + (index + 1) + "_button_highlighted");
            else if (menu.getActiveSlot() != index)
                spriteLoc = Orchestrate.asResource("container/composition_table/layer_" + (index + 1) + "_button_disabled");
            else
                spriteLoc = Orchestrate.asResource("container/composition_table/layer_" + (index + 1) + "_button");

            graphics.blitSprite(spriteLoc, this.getX(), this.getY(), this.width, this.height);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
