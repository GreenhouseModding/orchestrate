package dev.greenhouseteam.orchestrate.registry;

import dev.greenhouseteam.mib.registry.internal.RegistrationCallback;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class OrchestrateMenuTypes {
    public static final MenuType<CompositionMenu> COMPOSITION_TABLE = Orchestrate.getHelper().createCompositionMenu();

    public static void registerAll(RegistrationCallback<MenuType<?>> callback) {
        callback.register(BuiltInRegistries.MENU, Orchestrate.asResource("composition_table"), COMPOSITION_TABLE);
    }
}
