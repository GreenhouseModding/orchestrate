package dev.greenhouseteam.orchestrate.mixin.client;

import dev.greenhouseteam.orchestrate.client.screen.CompositionScreen;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow @Final private List<Renderable> renderables;

    @ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<Renderable> orchestrate$orderRenderablesInCompositionTable(Iterator<Renderable> iterator) {
        if ((Screen)(Object)this instanceof CompositionScreen)
            return renderables.stream().sorted(Comparator.comparingInt(value -> value instanceof TabOrderedElement tabOrdered ? tabOrdered.getTabOrderGroup() : 0)).iterator();
        return iterator;
    }
}
